package com.example.teachertimetable.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun fmtTime(minute: Int): String =
    "%02d:%02d".format(minute / 60, minute % 60)

fun fmtDate(d: LocalDate): String =
    d.format(DateTimeFormatter.ofPattern("M月d日"))

fun fmtDateFull(d: LocalDate): String =
    d.format(DateTimeFormatter.ofPattern("yyyy年M月d日"))

val dayNames = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

fun dayLabel(dow: Int): String = dayNames.getOrElse(dow - 1) { "?" }

fun durationText(start: Int, end: Int): String {
    val m = end - start
    return if (m <= 0) "" else "${m}分钟"
}
