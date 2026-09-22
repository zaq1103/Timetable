package com.example.teachertimetable.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.teachertimetable.data.ClassSession
import com.example.teachertimetable.data.GlobalSettings
import com.example.teachertimetable.data.ReminderType
import com.example.teachertimetable.util.fmtTime
import java.time.ZoneId

class ReminderScheduler(private val context: Context) {

    private val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun canScheduleExact(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) am.canScheduleExactAlarms() else true

    fun scheduleAll(sessions: List<ClassSession>, s: GlobalSettings) {
        val now = System.currentTimeMillis()
        val horizon = now + 7L * 24 * 3600 * 1000

        sessions.forEach { ses ->
            val type = if (ses.useGlobalReminder) s.defaultReminderType else ses.reminderType
            if (type == ReminderType.NONE) return@forEach

            val advance = if (ses.useGlobalReminder) s.defaultAdvance else ses.reminderAdvance
            val vibrate = if (ses.useGlobalReminder) s.defaultVibrateOnly else ses.vibrateOnly

            val startAt = ses.date.atStartOfDay(ZoneId.systemDefault())
                .plusMinutes(ses.startMinute.toLong())
                .toInstant().toEpochMilli()
            val trigger = startAt - advance * 60_000L

            if (trigger <= now || trigger > horizon) return@forEach

            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra(AlarmReceiver.EXTRA_TITLE, ses.name)
                putExtra(AlarmReceiver.EXTRA_CLASS, ses.className)
                putExtra(AlarmReceiver.EXTRA_LOCATION, ses.location)
                putExtra(
                    AlarmReceiver.EXTRA_TIME,
                    fmtTime(ses.startMinute) + " - " + fmtTime(ses.endMinute)
                )
                putExtra(AlarmReceiver.EXTRA_ADVANCE, advance)
                putExtra(AlarmReceiver.EXTRA_TYPE, type.name)
                putExtra(AlarmReceiver.EXTRA_VIBRATE, vibrate)
                putExtra(AlarmReceiver.EXTRA_SOUND, ses.alarmSound)
                putExtra(AlarmReceiver.EXTRA_COURSE_ID, ses.courseId)
            }

            val reqCode = (ses.date.toEpochDay() * 1440 + ses.startMinute).toInt()
            val pi = PendingIntent.getBroadcast(
                context, reqCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (canScheduleExact()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pi)
            } else {
                am.set(AlarmManager.RTC_WAKEUP, trigger, pi)
            }
        }
    }
}
