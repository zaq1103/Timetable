package com.example.teachertimetable.ui.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teachertimetable.data.ClassSession
import com.example.teachertimetable.ui.AppViewModel
import com.example.teachertimetable.util.dayLabel
import com.example.teachertimetable.util.fmtTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekGridScreen(
    vm: AppViewModel,
    onCellClick: (ClassSession) -> Unit
) {
    val state by vm.uiState.collectAsState()
    val sessions = state.weekSessions

    // 收集所有时间槽：按开始时间排序、去重
    val timeSlots = remember(sessions) {
        sessions.map { it.startMinute to it.endMinute }
            .distinct()
            .sortedBy { it.first }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("第 ${state.week} 周 · 周视图") },
                navigationIcon = {
                    IconButton(onClick = { vm.prevWeek() }) {
                        Icon(Icons.Filled.ChevronLeft, "上一周")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.nextWeek() }) {
                        Icon(Icons.Filled.ChevronRight, "下一周")
                    }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {

            // 表头
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = 8.dp)
            ) {
                Spacer(Modifier.width(56.dp))
                (1..7).forEach { d ->
                    Text(
                        dayLabel(d),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            HorizontalDivider()

            if (timeSlots.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("本周没有课程", color = Color.Gray)
                }
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(timeSlots, key = { it.first }) { (start, end) ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                            // 左侧时间
                            Column(
                                Modifier.width(56.dp).padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(fmtTime(start), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Text(fmtTime(end), fontSize = 10.sp, color = Color.Gray)
                            }

                            // 7 个格子
                            (1..7).forEach { d ->
                                val cell = sessions.firstOrNull {
                                    it.dayOfWeek == d && it.startMinute == start
                                }
                                Box(
                                    Modifier
                                        .weight(1f)
                                        .heightIn(min = 56.dp)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (cell != null)
                                                Color(cell.color).copy(alpha = 0.18f)
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .then(
                                            if (cell != null)
                                                Modifier.clickable { onCellClick(cell) }
                                            else Modifier
                                        )
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (cell != null) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                cell.name,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 2,
                                                textAlign = TextAlign.Center
                                            )
                                            if (cell.className.isNotBlank() && cell.className != cell.name) {
                                                Text(
                                                    cell.className,
                                                    fontSize = 10.sp,
                                                    color = Color(cell.color),
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
