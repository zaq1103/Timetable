package com.example.teachertimetable.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Light = lightColorScheme(
    primary = Color(0xFF3F51B5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E3FF),
    secondary = Color(0xFF00897B),
    surface = Color(0xFFFDFDFD),
    background = Color(0xFFF5F6FA)
)

private val Dark = darkColorScheme(
    primary = Color(0xFF9FA8DA),
    secondary = Color(0xFF80CBC4)
)

@Composable
fun TeacherTimetableTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Dark else Light,
        content = content
    )
}
