package com.example.teachertimetable.ui.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teachertimetable.data.ClassSession
import com.example.teachertimetable.data.CourseType
import com.example.teachertimetable.ui.AppViewModel
import com.example.teachertimetable.util.dayLabel
import com.example.teachertimetable.util.fmtTime
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    vm: AppViewModel,
    onAddCourse: () -> Unit,
    onEditCourse: (Long) -> Unit,
    onOpenMemo: (String) -> Unit,
    onScanImport: () -> Unit
) {
    val state by vm.uiState.collectAsState()
    var selectedSession by remember { mutableStateOf<ClassSession?>(null) }

    val termStart = LocalDate.ofEpochDay(state.settings.termStartEpochDay)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("第 " + state.week + " 周") },
                navigationIcon = {
                    IconButton(onClick = { vm.prevWeek() }) {
                        Icon(Icons.Filled.ChevronLeft, "上一周")
                    }
                },
                actions = {
                    IconButton(onClick = onScanImport) {
                        Icon(Icons.Filled.CameraAlt, "扫描导入")
                    }
                    IconButton(onClick = { vm.nextWeek() }) {
                        Icon(Icons.Filled.ChevronRight, "下一周")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCourse) {
                Icon(Icons.Filled.Add, "添加课程")
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {

            val weekStart = termStart.plusWeeks((state.week - 1).toLong())
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                (1..7).forEach { d ->
                    val date = weekStart.plusDays((d - 1).toLong())
                    val selected = state.day == d
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                            .clickable { vm.setDay(d) }
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(dayLabel(d), fontSize = 12.sp)
                        Text(
                            date.dayOfMonth.toString(),
                            fontSize = 15.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            HorizontalDivider(Modifier.padding(top = 4.dp))

            val list = state.daySessions(state.day)
            if (list.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("今天没有课，休息一下", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(list, key = { it.courseId.toString() + "-" + it.week + "-" + it.dayOfWeek }) { s ->
                        SessionCard(s) { selectedSession = s }
                    }
                }
            }
        }
    }

    selectedSession?.let { s ->
        SessionActionSheet(
            session = s,
            onDismiss = { selectedSession = null },
            onEdit = { onEditCourse(s.courseId); selectedSession = null },
            onMemo = { onOpenMemo(s.className); selectedSession = null }
        )
    }
}

@Composable
private fun SessionCard(s: ClassSession, onClick: () -> Unit) {
    val accent = Color(s.color)
    Card(
        onClick = onClick,
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(accent)
            )
            Column(Modifier.padding(12.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        s.name.ifBlank { s.className },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (s.type != CourseType.NORMAL) {
                        AssistChip(
                            onClick = {},
                            label = { Text(s.type.label, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    fmtTime(s.startMinute) + " - " + fmtTime(s.endMinute) +
                            if (s.location.isNotBlank()) "  ·  " + s.location else "",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                if (s.className.isNotBlank()) {
                    Text(s.className, fontSize = 13.sp, color = accent)
                }
                if (s.isOverride) {
                    Text(
                        "已调课" + (s.overrideRemark?.let { "：" + it } ?: ""),
                        fontSize = 11.sp,
                        color = Color(0xFFE65100)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionActionSheet(
    session: ClassSession,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onMemo: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp)) {
            Text(
                session.name + "  ·  第" + session.week + "周 " + dayLabel(session.dayOfWeek),
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
            Spacer(Modifier.height(12.dp))
            ListItem(
                headlineContent = { Text("编辑课程（含调课）") },
                supportingContent = { Text("修改本周这一次，或修改整个学期的排课") },
                modifier = Modifier.clickable { onEdit() }
            )
            ListItem(
                headlineContent = { Text("写这节课的备忘录") },
                supportingContent = { Text("记录本次课的教学进度") },
                modifier = Modifier.clickable { onMemo() }
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
