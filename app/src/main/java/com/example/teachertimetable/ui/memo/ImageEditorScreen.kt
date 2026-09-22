package com.example.teachertimetable.ui.memo

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path as ComposePath
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.teachertimetable.util.ImageStore

private data class DrawStroke(
    val points: MutableList<Offset> = mutableListOf(),
    val color: Color,
    val width: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageEditorScreen(
    sourcePath: String,
    onSaved: (String) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val strokes = remember { mutableStateListOf<DrawStroke>() }
    var color by remember { mutableStateOf(Color.Red) }
    var strokeWidth by remember { mutableStateOf(0.008f) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(sourcePath) {
        bitmap = ImageStore.decodeScaled(sourcePath)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("图片编辑") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex)
                    }) { Icon(Icons.Filled.Undo, "撤销") }

                    IconButton(onClick = {
                        val bmp = bitmap ?: return@IconButton
                        val out = bmp.copy(Bitmap.Config.ARGB_8888, true)
                        val canvas = Canvas(out)
                        val paint = Paint().apply {
                            isAntiAlias = true
                            style = Paint.Style.STROKE
                            strokeCap = Paint.Cap.ROUND
                            strokeJoin = Paint.Join.ROUND
                        }
                        strokes.forEach { s ->
                            paint.color = android.graphics.Color.argb(
                                (s.color.alpha * 255).toInt(),
                                (s.color.red * 255).toInt(),
                                (s.color.green * 255).toInt(),
                                (s.color.blue * 255).toInt()
                            )
                            paint.strokeWidth = s.width * out.width
                            val path = Path()
                            s.points.forEachIndexed { i, p ->
                                val x = p.x * out.width
                                val y = p.y * out.height
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            canvas.drawPath(path, paint)
                        }
                        val saved = ImageStore.saveBitmapAsNew(ctx, out)
                        if (saved != null) onSaved(saved)
                    }) { Icon(Icons.Filled.Check, "保存") }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black)
                    .onSizeChanged { canvasSize = it }
            ) {
                val bmp = bitmap
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(bmp, color, strokeWidth) {
                                detectDragGestures(
                                    onDragStart = { off ->
                                        val rect = fitRect(bmp, size)
                                        if (rect.contains(off)) {
                                            val nx = (off.x - rect.left) / rect.width
                                            val ny = (off.y - rect.top) / rect.height
                                            strokes.add(
                                                DrawStroke(
                                                    mutableListOf(Offset(nx, ny)),
                                                    color,
                                                    strokeWidth
                                                )
                                            )
                                        }
                                    },
                                    onDrag = { change, _ ->
                                        val rect = fitRect(bmp, size)
                                        val p = change.position
                                        if (rect.contains(p) && strokes.isNotEmpty()) {
                                            val nx = (p.x - rect.left) / rect.width
                                            val ny = (p.y - rect.top) / rect.height
                                            strokes.last().points.add(Offset(nx, ny))
                                        }
                                        change.consume()
                                    }
                                )
                            }
                    )

                    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                        val rect = fitRect(bmp, canvasSize)
                        if (rect.width <= 0f) return@Canvas
                        strokes.forEach { s ->
                            val path = ComposePath()
                            s.points.forEachIndexed { i, p ->
                                val x = rect.left + p.x * rect.width
                                val y = rect.top + p.y * rect.height
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            drawPath(
                                path = path,
                                color = s.color,
                                style = Stroke(
                                    width = s.width * rect.width,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                } else {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }

            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                Text("画笔颜色", style = MaterialTheme.typography.labelMedium)
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val colors = listOf(
                        Color.Red, Color.Blue, Color.Green,
                        Color.Yellow, Color.White, Color.Black
                    )
                    colors.forEach { c ->
                        Box(
                            Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(c)
                                .clickable { color = c }
                        ) {
                            if (color == c) {
                                Icon(
                                    Icons.Filled.Check, null,
                                    tint = if (c == Color.White || c == Color.Yellow)
                                        Color.Black else Color.White,
                                    modifier = Modifier.size(18.dp).align(Alignment.Center)
                                )
                            }
                        }
                    }
                }

                Text("粗细", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = strokeWidth,
                    onValueChange = { strokeWidth = it },
                    valueRange = 0.002f..0.03f
                )
            }
        }
    }
}

private fun fitRect(bmp: Bitmap, size: IntSize): Rect {
    if (size.width == 0 || size.height == 0) return Rect.Zero
    val scale = minOf(
        size.width.toFloat() / bmp.width,
        size.height.toFloat() / bmp.height
    )
    val w = bmp.width * scale
    val h = bmp.height * scale
    val left = (size.width - w) / 2f
    val top = (size.height - h) / 2f
    return Rect(left, top, left + w, top + h)
}
