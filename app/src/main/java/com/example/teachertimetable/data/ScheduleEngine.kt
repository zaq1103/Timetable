package com.example.teachertimetable.data

import java.time.LocalDate

/** 展开后的一节课（某周某天的具体一节课） */
data class ClassSession(
    val courseId: Long,
    val name: String,
    val className: String,
    val location: String,
    val type: CourseType,
    val week: Int,
    val dayOfWeek: Int,
    val date: LocalDate,
    val startMinute: Int,
    val endMinute: Int,
    val color: Int,
    val isOverride: Boolean,
    val overrideRemark: String?,
    val useGlobalReminder: Boolean,
    val reminderType: ReminderType,
    val reminderAdvance: Int,
    val vibrateOnly: Boolean,
    val alarmSound: String?
)

object ScheduleEngine {

    fun buildSessions(
        courses: List<CourseEntity>,
        overrides: List<SessionOverrideEntity>,
        termStartMonday: LocalDate,
        totalWeeks: Int
    ): List<ClassSession> {
        val ovMap = overrides.associateBy { Triple(it.courseId, it.week, it.originDayOfWeek) }
        val out = ArrayList<ClassSession>(courses.size * 16)

        for (c in courses) {
            if (!c.enabled) continue
            val from = maxOf(1, c.startWeek)
            val to = minOf(c.endWeek, totalWeeks)
            if (from > to) continue

            for (w in from..to) {
                if (c.weekParity == 1 && w % 2 == 0) continue
                if (c.weekParity == 2 && w % 2 == 1) continue

                val ov = ovMap[Triple(c.id, w, c.dayOfWeek)]
                if (ov?.cancelled == true) continue

                val dow = ov?.newDayOfWeek ?: c.dayOfWeek
                val date = termStartMonday
                    .plusWeeks((w - 1).toLong())
                    .plusDays((dow - 1).toLong())

                out += ClassSession(
                    courseId = c.id,
                    name = c.name.ifBlank { c.className },
                    className = c.className,
                    location = ov?.newLocation ?: c.location,
                    type = c.type,
                    week = w,
                    dayOfWeek = dow,
                    date = date,
                    startMinute = ov?.newStartMinute ?: c.startMinute,
                    endMinute = ov?.newEndMinute ?: c.endMinute,
                    color = c.color,
                    isOverride = ov != null,
                    overrideRemark = ov?.remark,
                    useGlobalReminder = c.useGlobalReminder,
                    reminderType = c.reminderType,
                    reminderAdvance = c.reminderAdvance,
                    vibrateOnly = c.vibrateOnly,
                    alarmSound = c.alarmSound
                )
            }
        }
        return out.sortedWith(compareBy({ it.date }, { it.startMinute }))
    }

    fun weekOf(date: LocalDate, termStartMonday: LocalDate): Int =
        ((date.toEpochDay() - termStartMonday.toEpochDay()) / 7).toInt() + 1
}
