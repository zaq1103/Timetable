package com.example.teachertimetable.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class CourseType(val label: String) {
    NORMAL("正课"),
    MORNING_READING("早读"),
    NOON_PRACTICE("午练"),
    EVENING_STUDY("晚自习"),
    OTHER("其他")
}

enum class ReminderType { NONE, NOTIFICATION, ALARM }

/** 课程模板：一周中的固定排课，可覆盖整个学期 */
@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val className: String = "",
    val teacher: String = "",
    val location: String = "",
    val dayOfWeek: Int = 1,
    val startMinute: Int = 8 * 60,
    val endMinute: Int = 8 * 60 + 45,
    val type: CourseType = CourseType.NORMAL,
    val startWeek: Int = 1,
    val endWeek: Int = 20,
    val weekParity: Int = 0,
    val color: Int = 0xFF5C6BC0.toInt(),
    val enabled: Boolean = true,
    val useGlobalReminder: Boolean = true,
    val reminderType: ReminderType = ReminderType.NOTIFICATION,
    val reminderAdvance: Int = 10,
    val vibrateOnly: Boolean = false,
    val alarmSound: String? = null
)

/** 单次调课 / 取消：仅影响某一周的某一天 */
@Entity(tableName = "session_overrides", indices = [Index("courseId")])
data class SessionOverrideEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val week: Int,
    val originDayOfWeek: Int,
    val newDayOfWeek: Int? = null,
    val newStartMinute: Int? = null,
    val newEndMinute: Int? = null,
    val newLocation: String? = null,
    val cancelled: Boolean = false,
    val remark: String? = null
)

/**
 * 备忘录
 * - courseId == null  -> 班级总备忘录
 * - courseId != null  -> 针对某一次课程的备忘录
 */
@Entity(tableName = "memos", indices = [Index("className")])
data class MemoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val className: String,
    val courseId: Long? = null,
    val courseName: String = "",
    val week: Int? = null,
    val dayOfWeek: Int? = null,
    val epochDay: Long? = null,
    val title: String = "",
    val content: String = "",
    val images: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val imageList: List<String>
        get() = images.split(",").filter { it.isNotBlank() }

    val isClassLevel: Boolean get() = courseId == null
}
