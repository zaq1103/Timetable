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

    var totalWeeks by remember(s) { mutableStateOf(s.totalWeeks) }
    var reminderType by remember(s) { mutableStateOf(s.defaultReminderType) }
    var advance by remember(s) { mutableStateOf(s.defaultAdvance) }
    var vibrateOnly by remember(s) { mutableStateOf(s.defaultVibrateOnly) }

    Scaffold(topBar = { TopAppBar(title = { Text("设置") }) }) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
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
                        "学期起始（周一）：" + LocalDate.ofEpochDay(s.termStartEpochDay).toString(),
                        fontSize = 13.sp
                    )
                    OutlinedButton(onClick = {
                        val today = LocalDate.now()
                        val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
                        vm.saveSettings(
                            s.copy(
                                termStartEpochDay = monday.toEpochDay(),
                                totalWeeks = totalWeeks
                            )
                        )
                    }) { Text("把本周设为第 1 周") }
                }
            }

            Card {
                Column(
                    Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("默认提醒", fontWeight = FontWeight.Bold)
                    Text(
                        "未被课程单独覆盖时使用",
                        fontSize = 12.sp, color = Color.Gray
                    )

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
                        Button(onClick = {
                            runCatching {
                                ctx.startActivity(
                                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text("申请精确闹钟权限") }
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
                    vm.saveSettings(
                        s.copy(
                            totalWeeks = totalWeeks,
                            defaultReminderType = reminderType,
                            defaultAdvance = advance,
                            defaultVibrateOnly = vibrateOnly
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text("保存设置") }

            Spacer(Modifier.height(30.dp))
        }
    }
}
