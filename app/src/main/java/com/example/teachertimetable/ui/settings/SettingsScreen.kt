package com.example.teachertimetable.ui.settings

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teachertimetable.data.ReminderType
import com.example.teachertimetable.ui.AppViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: AppViewModel) {
    val ctx = LocalContext.current
    val state by vm.uiState.collectAsState()
    val s = state.settings

    var showDatePicker by remember { mutableStateOf(false) }

    var totalWeeks by remember(s) { mutableStateOf(s.totalWeeks) }
    var reminderType by remember(s) { mutableStateOf(s.defaultReminderType) }
    var advance by remember(s) { mutableStateOf(s.defaultAdvance) }
    var vibrateOnly by remember(s) { mutableStateOf(s.defaultVibrateOnly) }
    var termStart by remember(s) { mutableStateOf(LocalDate.ofEpochDay(s.termStartEpochDay)) }
    var classesText by remember(s) { mutableStateOf(s.myClasses.joinToString("\n")) }
    var subjectsText by remember(s) { mutableStateOf(s.mySubjects.joinToString("\n")) }

    Scaffold(topBar = { TopAppBar(title = { Text("设置") }) }) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ========= 学期设置 =========
            Card {
                Column(
                    Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("学期设置", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = totalWeeks.toString(),
                        onValueChange = { totalWeeks = it.toIntOrNull() ?: totalWeeks },
                        label = { Text("学期总周数") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        "第 1 周开始于：" + termStart.toString() + "（周一）",
                        fontSize = 13.sp
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.weight(1f)
                        ) { Text("选择第 1 周日期") }

                        OutlinedButton(
                            onClick = {
                                val today = LocalDate.now()
                                termStart = today.minusDays((today.dayOfWeek.value - 1).toLong())
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("设为本周") }
                    }

                    Text(
                        "提示：如果学期已进行到一半，请手动选择学期开始的那一周的周一，" +
                                "然后告诉 App 现在进入第几周。",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // ========= 提醒设置 =========
            Card {
                Column(
                    Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("默认提醒", fontWeight = FontWeight.Bold)
                    Text("未被课程单独覆盖时使用", fontSize = 12.sp, color = Color.Gray)

                    Text("方式", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            ReminderType.NOTIFICATION to "通知",
                            ReminderType.ALARM to "闹钟",
                            ReminderType.NONE to "关闭"
                        ).forEach { (t, l) ->
                            FilterChip(
                                selected = reminderType == t,
                                onClick = { reminderType = t },
                                label = { Text(l) }
                            )
                        }
                    }

                    Text("提前时间（分钟）", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0, 5, 10, 15, 30, 60).forEach { m ->
                            FilterChip(
                                selected = advance == m,
                                onClick = { advance = m },
                                label = { Text(if (m == 0) "准点" else m.toString()) }
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = vibrateOnly,
                            onCheckedChange = { vibrateOnly = it }
                        )
                        Text("闹钟提醒仅震动")
                    }
                }
            }

            // ========= OCR 预设 =========
            Card {
                Column(
                    Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("OCR 预设", fontWeight = FontWeight.Bold)
                    Text(
                        "拍照识别课程表时，会用这些关键词区分班级和学科，避免混淆。",
                        fontSize = 12.sp, color = Color.Gray
                    )

                    OutlinedTextField(
                        value = classesText,
                        onValueChange = { classesText = it },
                        label = { Text("执教班级（每行一个）") },
                        placeholder = { Text("高一(1)班\n高一(2)班\n高二(3)班") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = subjectsText,
                        onValueChange = { subjectsText = it },
                        label = { Text("执教学科（每行一个）") },
                        placeholder = { Text("语文\n数学\n英语") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // ========= 权限 =========
            Card {
                Column(
                    Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("权限与系统", fontWeight = FontWeight.Bold)

                    Button(
                        onClick = { vm.reschedule() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("重新排定全部提醒") }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Button(
                            onClick = {
                                runCatching {
                                    ctx.startActivity(
                                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("申请精确闹钟权限") }
                    }

                    val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        am.canScheduleExactAlarms() else true

                    Text(
                        if (canExact) "精确闹钟：已授权"
                        else "精确闹钟：未授权（提醒可能延迟）",
                        fontSize = 12.sp,
                        color = if (canExact) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }

            Button(
                onClick = {
                    val classes = classesText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                    val subjects = subjectsText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                    vm.saveSettings(
                        s.copy(
                            termStartEpochDay = termStart.toEpochDay(),
                            totalWeeks = totalWeeks,
                            defaultReminderType = reminderType,
                            defaultAdvance = advance,
                            defaultVibrateOnly = vibrateOnly,
                            myClasses = classes,
                            mySubjects = subjects
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text("保存设置") }

            Spacer(Modifier.height(30.dp))
        }
    }

    if (showDatePicker) {
        val dpState = rememberDatePickerState(
            initialSelectedDateMillis = termStart.toEpochDay() * 86_400_000L
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let {
                        termStart = LocalDate.ofEpochDay(it / 86_400_000L)
                    }
                    showDatePicker = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = dpState)
        }
    }
}
