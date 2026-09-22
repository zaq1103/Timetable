package com.example.teachertimetable.ui.memo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import coil.compose.AsyncImage
import com.example.teachertimetable.data.MemoEntity
import com.example.teachertimetable.ui.AppViewModel
import com.example.teachertimetable.util.ImageStore
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMemoScreen(
    vm: AppViewModel,
    memoId: Long,
    defaultClassName: String,
    defaultCourseId: Long?,
    defaultWeek: Int?,
    defaultDay: Int?,
    savedStateHandle: SavedStateHandle,
    onEditImage: (String) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current

    var className by remember { mutableStateOf(defaultClassName) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var images by remember { mutableStateOf(listOf<String>()) }
    var courseId by remember { mutableStateOf(defaultCourseId) }
    var courseName by remember { mutableStateOf("") }
    var week by remember { mutableStateOf(defaultWeek) }
    var day by remember { mutableStateOf(defaultDay) }
    var loaded by remember { mutableStateOf(memoId <= 0) }

    var pendingCameraPath by remember { mutableStateOf<String?>(null) }

    val pickLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        ImageStore.importFromUri(ctx, uri)?.let { images = images + it }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok) pendingCameraPath?.let { images = images + it }
    }

    LaunchedEffect(memoId) {
        if (memoId > 0) {
            vm.getMemo(memoId)?.let { m ->
                className = m.className
                title = m.title
                content = m.content
                images = m.imageList
                courseId = m.courseId
                courseName = m.courseName
                week = m.week
                day = m.dayOfWeek
            }
            loaded = true
        }
    }

    // 接收图片编辑器返回
    LaunchedEffect(Unit) {
        savedStateHandle.getStateFlow("edited_image", "").collect { p ->
            if (p.isNotBlank()) {
                images = images + p
                savedStateHandle["edited_image"] = ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (courseId == null) "班级备忘录" else "课程备忘录") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "返回") }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = className, onValueChange = { className = it },
                label = { Text("班级") }, modifier = Modifier.fillMaxWidth()
            )

            if (courseId != null) {
                OutlinedTextField(
                    value = courseName, onValueChange = { courseName = it },
                    label = { Text("课程名称") }, modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = week?.toString() ?: "",
                        onValueChange = { week = it.toIntOrNull() },
                        label = { Text("第几周") }, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = day?.toString() ?: "",
                        onValueChange = { day = it.toIntOrNull() },
                        label = { Text("星期") }, modifier = Modifier.weight(1f)
                    )
                }
            }

            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("标题（可选）") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = content, onValueChange = { content = it },
                label = { Text("教学进度 / 备注") },
                minLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "图片",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    val f = ImageStore.newImageFile(ctx)
                    pendingCameraPath = f.absolutePath
                    val uri = FileProvider.getUriForFile(
                        ctx, ctx.packageName + ".fileprovider", f
                    )
                    cameraLauncher.launch(uri)
                }) { Icon(Icons.Filled.AddAPhoto, "拍照") }

                IconButton(onClick = {
                    pickLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) { Icon(Icons.Filled.Add, "从相册选择") }
            }

            if (images.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(images) { p ->
                        Box {
                            AsyncImage(
                                model = File(p),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(96.dp)
                            )
                            IconButton(
                                onClick = { images = images - p },
                                modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Close, "删除",
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = { onEditImage(p) },
                                modifier = Modifier.align(Alignment.BottomEnd).size(28.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Edit, "编辑",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val now = System.currentTimeMillis()
                    val entity = MemoEntity(
                        id = if (memoId > 0) memoId else 0,
                        className = className.ifBlank { "未分班" },
                        courseId = courseId,
                        courseName = courseName,
                        week = week,
                        dayOfWeek = day,
                        title = title,
                        content = content,
                        images = images.joinToString(","),
                        createdAt = now,
                        updatedAt = now
                    )
                    vm.saveMemo(entity) { onBack() }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text("保存") }

            Spacer(Modifier.height(30.dp))
        }
    }
}
