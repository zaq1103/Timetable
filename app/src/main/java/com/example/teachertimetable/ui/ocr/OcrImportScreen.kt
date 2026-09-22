package com.example.teachertimetable.ui.ocr

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.teachertimetable.data.CourseEntity
import com.example.teachertimetable.data.CourseType
import com.example.teachertimetable.ocr.ParsedCourse
import com.example.teachertimetable.ocr.TimetableOcrParser
import com.example.teachertimetable.ui.AppViewModel
import com.example.teachertimetable.util.ImageStore
import com.example.teachertimetable.util.dayLabel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrImportScreen(
    vm: AppViewModel,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val state by vm.uiState.collectAsState()

    val myClasses = state.settings.myClasses
    val mySubjects = state.settings.mySubjects

    var parsing by remember { mutableStateOf(false) }
    var rawText by remember { mutableStateOf("") }
    var parsed by remember { mutableStateOf<List<ParsedCourse>>(emptyList()) }
    var pendingCameraPath by remember { mutableStateOf<String?>(null) }

    val pickLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        ImageStore.importFromUri(ctx, uri)?.let { path ->
            parsing = true
            scope.launch {
                val t = runOcr(path)
                rawText = t
                parsed = TimetableOcrParser.parse(t, myClasses, mySubjects)
                parsing = false
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok) pendingCameraPath?.let { path ->
            parsing = true
            scope.launch {
                val t = runOcr(path)
                rawText = t
                parsed = TimetableOcrParser.parse(t, myClasses, mySubjects)
                parsing = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("扫描课程表") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "返回") }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        val f = ImageStore.newImageFile(ctx)
                        pendingCameraPath = f.absolutePath
                        val uri = FileProvider.getUriForFile(
                            ctx, ctx.packageName + ".fileprovider", f
                        )
                        cameraLauncher.launch(uri)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.AddAPhoto, null)
                    Spacer(Modifier.width(6.dp))
                    Text("拍摄")
                }
                OutlinedButton(
                    onClick = {
                        pickLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Image, null)
                    Spacer(Modifier.width(6.dp))
                    Text("从相册")
                }
            }

            if (myClasses.isEmpty() || mySubjects.isEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))) {
                    Column(Modifier.padding(12.dp)) {
                        Text("建议先去「设置」预设执教班级和学科", fontWeight = FontWeight.Medium)
                        Text(
                            "预设后，OCR 会把文本中出现的班级、学科自动区分，避免混淆。",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (parsing) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Text("正在识别…", fontSize = 13.sp)
            }

            if (parsed.isNotEmpty()) {
                Text(
                    "识别到 ${parsed.size} 条，可编辑后导入",
                    fontWeight = FontWeight.SemiBold
                )
                LazyColumn(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(parsed) { idx, p ->
                        ParsedCard(
                            index = idx,
                            data = p,
                            onUpdate = { newP ->
                                parsed = parsed.toMutableList().also { it[idx] = newP }
                            },
                            onRemove = {
                                parsed = parsed.toMutableList().also { it.removeAt(idx) }
                            }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = { parsed = emptyList() },
                        modifier = Modifier.weight(1f)
                    ) { Text("清空") }
                    Button(
                        onClick = {
                            val valid = parsed.filter { it.dayOfWeek != null }
                            val entities = valid.mapIndexed { i, p ->
                                val (s, e) = p.startMinute?.let { it to (p.endMinute ?: it + 45) }
                                    ?: p.periodIndex?.let { TimetableOcrParser.periodToTime(it) }
                                    ?: (8 * 60 to (8 * 60 + 45))
                                val palette = listOf(
                                    0xFF5C6BC0, 0xFF26A69A, 0xFFEF5350, 0xFFFFA726,
                                    0xFF66BB6A, 0xFFAB47BC, 0xFF29B6F6, 0xFF8D6E63
                                ).map { it.toInt() }
                                CourseEntity(
                                    name = p.subject.ifBlank { "课程" },
                                    className = p.className,
                                    location = p.location,
                                    dayOfWeek = p.dayOfWeek ?: 1,
                                    startMinute = s,
                                    endMinute = e,
                                    type = CourseType.NORMAL,
                                    color = palette[i % palette.size]
                                )
                            }
                            vm.saveCourses(entities) { onBack() }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("导入 ${parsed.count { it.dayOfWeek != null }} 门") }
                }
            } else if (!parsing && rawText.isNotBlank()) {
                Text("没有识别到有效课程行，请尝试更清晰的照片。", color = Color.Gray)
            } else if (!parsing) {
                Text("拍摄或选择一张课程表照片，系统会识别文字并解析。", color = Color.Gray)
            }
        }
    }
}

@Composable
private fun ParsedCard(
    index: Int,
    data: ParsedCourse,
    onUpdate: (ParsedCourse) -> Unit,
    onRemove: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "第 ${index + 1} 条 · " +
                            (data.dayOfWeek?.let { dayLabel(it) } ?: "未识别星期") +
                            (data.periodIndex?.let { " · 第${it}节" } ?: ""),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Delete, "删除")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = data.className,
                    onValueChange = { onUpdate(data.copy(className = it)) },
                    label = { Text("班级") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = data.subject,
                    onValueChange = { onUpdate(data.copy(subject = it)) },
                    label = { Text("学科/课程") },
                    modifier = Modifier.weight(1f)
                )
            }

            Text("原始：${data.rawLine}", fontSize = 11.sp, color = Color.Gray)
        }
    }
}

private suspend fun runOcr(path: String): String {
    val bmp = ImageStore.decodeScaled(path) ?: return ""
    val recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
    return try {
        val text = suspendCancellableCoroutine<com.google.mlkit.vision.text.Text> { cont ->
            recognizer.process(InputImage.fromBitmap(bmp, 0))
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }
        text.text
    } catch (_: Throwable) {
        ""
    } finally {
        recognizer.close()
    }
}
