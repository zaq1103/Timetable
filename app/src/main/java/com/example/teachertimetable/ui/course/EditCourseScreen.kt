package com.example.teachertimetable.ui.course

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teachertimetable.data.*
import com.example.teachertimetable.ui.AppViewModel
import com.example.teachertimetable.util.dayLabel
import com.example.teachertimetable.util.fmtTime
import kotlinx.coroutines.launch

private val palette = listOf(
    0xFF5C6BC0, 0xFF26A69A, 0xFFEF5350, 0xFFFFA726,
    0xFF66BB6A, 0xFFAB47BC, 0xFF29B6F6, 0xFF8D6E63
).map { it.toInt() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCourseScreen(
    vm: AppViewModel,
    courseId: Long,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var loaded by remember { mutableStateOf(courseId <= 0) }

    var name by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var dayOfWeek by remember { mutableStateOf(1) }
    var startMinute by remember { mutableStateOf(8 * 60) }
    var endMinute by remember { mutableStateOf(8 * 60 + 45) }
    var type by remember { mutableStateOf(CourseType.NORMAL) }
    var startWeek by remember { mutableStateOf(1) }
    var endWeek by remember { mutableStateOf(20) }
    var parity by remember { mutableStateOf(0) }
    var color by remember { mutableStateOf(palette[0]) }

    var useGlobalReminder by remember { mutableStateOf(true) }
    var reminderType by remember { mutableStateOf(ReminderType.NOTIFICATION) }
    var advance by remember { mutableStateOf(10) }
    var vibrateOnly by remember { mutableStateOf(false) }

    var showSwapDialog by remember { mutableStateOf(false) }

    LaunchedEffect(courseId) {
        if (courseId > 0) {
            vm.getCourse(courseId)?.let { c ->
                name = c.name; className = c.className; location = c.location
                dayOfWeek = c.dayOfWeek; startMinute = c.startMinute; endMinute = c.endMinute
                type = c.type; startWeek = c.startWeek; endWeek = c.endWeek
                parity = c.weekParity; color = c.color
                useGlobalReminder = c.useGlobalReminder
                reminderType = c.reminderType; advance = c.reminderAdvance
                vibrateOnly = c.vibrateOnly
            }
            loaded = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (courseId > 0) "编辑课程" else "添加课程") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "返回") }
                },
                actions = {
                    if (courseId > 0) {
                        IconButton(onClick = {
                            scope.launch {
                                vm.getCourse(courseId)?.let { vm.deleteCourse(it) }
                                onBack()
                            }
                        }) { Icon(Icons.Filled.Delete, "删除") }
                    }
                }
            )
        }
    ) { pad ->
        if (!loaded) {
            Box(Modifier.padding(pad).fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("课程名称") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = className, onValueChange = { className = it },
                label = { Text("班级") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = location, onValueChange = { location = it },
                label = { Text("上课地点（可选）") }, modifier = Modifier.fillMaxWidth()
            )

            SectionTitle("课程类型")
            FlowRowSimple(
                options = CourseType.entries.toList(),
                labelOf = { it.label },
                selected = type,
                onSelect = { type = it }
            )

            SectionTitle("上课时间")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = {
                    TimePickerDialog(ctx, { _, h, m -> startMinute = h * 60 + m },
                        startMinute / 60, startMinute % 60, true).show()
                }, modifier = Modifier.weight(1f)) { Text("开始 " + fmtTime(startMinute)) }

                OutlinedButton(onClick = {
                    TimePickerDialog(ctx, { _, h, m -> endMinute = h * 60 + m },
                        endMinute / 60, endMinute % 60, true).show()
                }, modifier = Modifier.weight(1f)) { Text("结束 " + fmtTime(endMinute)) }
            }

            SectionTitle("星期")
            FlowRowSimple(
                options = (1..7).toList(),
                labelOf = { dayLabel(it) },
                selected = dayOfWeek,
                onSelect = { dayOfWeek = it }
            )

            SectionTitle("周次范围（覆盖整个学期）")
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumberField("起", startWeek, Modifier.weight(1f)) { startWeek = it.coerceAtLeast(1) }
                NumberField("止", endWeek, Modifier.weight(1f)) { endWeek = it.coerceAtLeast(startWeek) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(0 to "每周", 1 to "单周", 2 to "双周").forEach { (v, l) ->
                    FilterChip(selected = parity == v, onClick = { parity = v }, label = { Text(l) })
                }
            }

            SectionTitle("颜色")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                palette.forEach { c ->
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(c))
                            .clickable { color = c }
                    ) {
                        if (color == c) {
                            Text(
                                "OK", color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }

            HorizontalDivider()
            SectionTitle("课前提醒")

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = useGlobalReminder, onCheckedChange = { useGlobalReminder = it })
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("使用全局默认提醒", fontWeight = FontWeight.Medium)
                    Text("在「设置」中统一配置，适合大多数课程", fontSize = 12.sp, color = Color.Gray)
                }
            }

            if (!useGlobalReminder) {
                SectionTitle("提醒方式")
                FlowRowSimple(
                    options = listOf(
                        ReminderType.NOTIFICATION, ReminderType.ALARM, ReminderType.NONE
                    ),
                    labelOf = {
                        when (it) {
                            ReminderType.NOTIFICATION -> "通知"
                            ReminderType.ALARM -> "闹钟"
                            ReminderType.NONE -> "不提醒"
                        }
                    },
                    selected = reminderType,
                    onSelect = { reminderType = it }
                )

                SectionTitle("提前时间（分钟）")
                FlowRowSimple(
                    options = listOf(0, 5, 10, 15, 30, 60),
                    labelOf = { if (it == 0) "准点" else it.toString() },
                    selected = advance,
                    onSelect = { advance = it }
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = vibrateOnly, onCheckedChange = { vibrateOnly = it })
                    Text("仅震动（静音，适合在教室/会议中）")
                }
            }

            if (courseId > 0) {
                HorizontalDivider()
                SectionTitle("调课 / 单次调整")
                Button(
                    onClick = { showSwapDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("只调整某一次课") }
                Text(
                    "例如：本周三第 3 节的课调到周五第 5 节，或临时取消一次。",
                    fontSize = 12.sp, color = Color.Gray
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val entity = CourseEntity(
                        id = if (courseId > 0) courseId else 0,
                        name = name.ifBlank { className.ifBlank { "未命名课程" } },
                        className = className,
                        location = location,
                        dayOfWeek = dayOfWeek,
                        startMinute = startMinute,
                        endMinute = endMinute,
                        type = type,
                        startWeek = startWeek,
                        endWeek = endWeek,
                        weekParity = parity,
                        color = color,
                        useGlobalReminder = useGlobalReminder,
                        reminderType = reminderType,
                        reminderAdvance = advance,
                        vibrateOnly = vibrateOnly
                    )
                    vm.saveCourse(entity) { onBack() }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text("保存") }

            Spacer(Modifier.height(30.dp))
        }
    }

    if (showSwapDialog && courseId > 0) {
        SwapDialog(
            vm = vm,
            courseId = courseId,
            defaultDay = dayOfWeek,
            defaultStart = startMinute,
            defaultEnd = endMinute,
            onDismiss = { showSwapDialog = false }
        )
    }
}

@Composable
private fun SectionTitle(t: String) {
    Text(
        t,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.primary
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> FlowRowSimple(
    options: List<T>,
    labelOf: (T) -> String,
    selected: T,
    onSelect: (T) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { o ->
            FilterChip(
                selected = o == selected,
                onClick = { onSelect(o) },
                label = { Text(labelOf(o)) }
            )
        }
    }
}

@Composable
private fun NumberField(label: String, value: Int, modifier: Modifier, onChange: (Int) -> Unit) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { s -> s.toIntOrNull()?.let(onChange) },
        label = { Text(label) },
        singleLine = true,
        modifier = modifier
    )
}

@Composable
private fun SwapDialog(
    vm: AppViewModel,
    courseId: Long,
    defaultDay: Int,
    defaultStart: Int,
    defaultEnd: Int,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    var week by remember { mutableStateOf(1) }
    var originDay by remember { mutableStateOf(defaultDay) }
    var newDay by remember { mutableStateOf(defaultDay) }
    var newStart by remember { mutableStateOf(defaultStart) }
    var newEnd by remember { mutableStateOf(defaultEnd) }
    var cancelled by remember { mutableStateOf(false) }
    var remark by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("单次调课") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                NumberField("第几周", week, Modifier.fillMaxWidth()) { week = it.coerceAtLeast(1) }

                Text("原定星期：" + dayLabel(originDay), fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..7).forEach { d ->
                        FilterChip(
                            selected = originDay == d,
                            onClick = { originDay = d },
                            label = { Text(dayLabel(d).removePrefix("周"), fontSize = 11.sp) }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = cancelled, onCheckedChange = { cancelled = it })
                    Text("本次取消（不上课）")
                }

                if (!cancelled) {
                    Text("调整到：" + dayLabel(newDay), fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..7).forEach { d ->
                            FilterChip(
                                selected = newDay == d,
                                onClick = { newDay = d },
                                label = { Text(dayLabel(d).removePrefix("周"), fontSize = 11.sp) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            TimePickerDialog(ctx, { _, h, m -> newStart = h * 60 + m },
                                newStart / 60, newStart % 60, true).show()
                        }, modifier = Modifier.weight(1f)) { Text(fmtTime(newStart)) }
                        OutlinedButton(onClick = {
                            TimePickerDialog(ctx, { _, h, m -> newEnd = h * 60 + m },
                                newEnd / 60, newEnd % 60, true).show()
                        }, modifier = Modifier.weight(1f)) { Text(fmtTime(newEnd)) }
                    }
                }

                OutlinedTextField(
                    value = remark, onValueChange = { remark = it },
                    label = { Text("备注（可选）") }, modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                vm.applyOverride(
                    SessionOverrideEntity(
                        courseId = courseId,
                        week = week,
                        originDayOfWeek = originDay,
                        newDayOfWeek = if (cancelled) null else newDay,
                        newStartMinute = if (cancelled) null else newStart,
                        newEndMinute = if (cancelled) null else newEnd,
                        cancelled = cancelled,
                        remark = remark.ifBlank { null }
                    )
                )
                onDismiss()
            }) { Text("确定") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
