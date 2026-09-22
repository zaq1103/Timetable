package com.example.teachertimetable.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TITLE = "title"
        const val EXTRA_CLASS = "className"
        const val EXTRA_LOCATION = "location"
        const val EXTRA_TIME = "time"
        const val EXTRA_ADVANCE = "advance"
        const val EXTRA_TYPE = "type"
        const val EXTRA_VIBRATE = "vibrate"
        const val EXTRA_SOUND = "sound"
        const val EXTRA_COURSE_ID = "courseId"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "上课提醒"
        val className = intent.getStringExtra(EXTRA_CLASS).orEmpty()
        val location = intent.getStringExtra(EXTRA_LOCATION).orEmpty()
        val time = intent.getStringExtra(EXTRA_TIME).orEmpty()
        val advance = intent.getIntExtra(EXTRA_ADVANCE, 0)
        val type = intent.getStringExtra(EXTRA_TYPE) ?: "NOTIFICATION"
        val vibrateOnly = intent.getBooleanExtra(EXTRA_VIBRATE, false)
        val sound = intent.getStringExtra(EXTRA_SOUND)
        val courseId = intent.getLongExtra(EXTRA_COURSE_ID, -1L)

        NotificationHelper.showCourseReminder(
            context = context,
            title = title,
            className = className,
            location = location,
            time = time,
            advance = advance,
            isAlarm = type == "ALARM",
            vibrateOnly = vibrateOnly,
            soundUri = sound,
            courseId = courseId
        )
    }
}
