package com.example.teachertimetable.ui.memo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.teachertimetable.data.MemoEntity
import com.example.teachertimetable.ui.AppViewModel
import com.example.teachertimetable.util.fmtDateFull
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoScreen(
    vm: AppViewModel,
    onEditMemo: (Long) -> Unit,
    onNewClassMemo: (String) -> Unit
) {
    val state by vm.uiState.collectAsState()
    val grouped = state.memos.groupBy { it.className.ifBlank { "未分班" } }

    Scaffold(
        topBar = { TopAppBar(title = { Text("教学备忘录") }) }
    ) { pad ->
        if (grouped.isEmpty()) {
            Box(Modifier.padding(pad).fillMaxSize(), Alignment.Center) {
                Text(
                    "还没有备忘录，去「课表」里点一节课开始记录吧",
                    color = Color.Gray
                )
            }
            return@Scaffold
        }

        LazyColumn(
            Modifier.padding(pad).fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            grouped.forEach { (cls, memos) ->
                item(key = "header_" + cls) {
                    ClassMemoCard(
                        className = cls,
                        memos = memos,
                        onEditMemo = onEditMemo,
                        onAddClassMemo = { onNewClassMemo(cls) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassMemoCard(
    className: String,
    memos: List<MemoEntity>,
    onEditMemo: (Long) -> Unit,
    onAddClassMemo: () -> Unit
) {
    val classMemos = memos.filter { it.isClassLevel }
    val sessionMemos = memos.filter { !it.isClassLevel }
    var expanded by remember { mutableStateOf(false) }

    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    className,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onAddClassMemo) {
                    Icon(Icons.Filled.Add, "新增班级备忘")
                }
            }

            if (classMemos.isEmpty()) {
                Text("暂无班级总备忘录", fontSize = 13.sp, color = Color.Gray)
            } else {
                classMemos.take(3).forEach { m -> MemoRow(m, onEditMemo) }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "单次课程备忘 (" + sessionMemos.size + ")",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    null
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(top = 6.dp)) {
                    if (sessionMemos.isEmpty()) {
                        Text("暂无", fontSize = 13.sp, color = Color.Gray)
                    } else {
                        sessionMemos.sortedByDescending { it.createdAt }.forEach { m ->
                            Column(Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    buildString {
                                        append(m.courseName.ifBlank { "课程" })
                                        m.week?.let { append("  ·  第").append(it).append("周") }
                                        m.epochDay?.let {
                                            append("  ·  ")
                                            append(fmtDateFull(LocalDate.ofEpochDay(it)))
                                        }
                                    },
                                    fontSize = 12.sp,
                                    color = Color(0xFF5C6BC0)
                                )
                                MemoRow(m, onEditMemo, compact = true)
                                HorizontalDivider(Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoRow(m: MemoEntity, onEdit: (Long) -> Unit, compact: Boolean = false) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable { onEdit(m.id) }
            .padding(vertical = 6.dp)
    ) {
        if (m.title.isNotBlank()) {
            Text(
                m.title,
                fontWeight = FontWeight.Medium,
                fontSize = if (compact) 13.sp else 15.sp
            )
        }
        if (m.content.isNotBlank()) {
            Text(
                m.content,
                fontSize = if (compact) 12.sp else 14.sp,
                maxLines = if (compact) 2 else 4,
                color = Color.DarkGray
            )
        }
        if (m.imageList.isNotEmpty()) {
            Row(
                Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                m.imageList.take(3).forEach { p ->
                    AsyncImage(
                        model = File(p),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(if (compact) 44.dp else 64.dp)
                    )
                }
                if (m.imageList.size > 3) {
                    Text(
                        "+" + (m.imageList.size - 3),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}
