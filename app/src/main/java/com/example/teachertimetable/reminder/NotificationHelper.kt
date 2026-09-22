package com.example.teachertimetable.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.teachertimetable.ui.MainActivity

object NotificationHelper {

    const val CH_ALARM = "ch_alarm"
    const val CH_NOTIFY = "ch_notify"
    const val CH_SILENT = "ch_silent"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = context.getSystemService(NotificationManager::class.java)

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

        val alarmAttr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        nm.createNotificationChannel(
            NotificationChannel(CH_ALARM, "课程闹钟", NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(alarmSound, alarmAttr)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 600, 400, 600, 400, 600)
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CH_NOTIFY, "课程通知", NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .build()
                )
                enableVibration(true)
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(CH_SILENT, "静音提醒（仅震动）", NotificationManager.IMPORTANCE_HIGH).apply {
                setSound(null, null)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 800, 400, 800)
            }
        )
    }

    fun showCourseReminder(
        context: Context,
        title: String,
        className: String,
        location: String,
        time: String,
        advance: Int,
        isAlarm: Boolean,
        vibrateOnly: Boolean,
        soundUri: String?,
        courseId: Long
    ) {
        ensureChannels(context)

        val channel = when {
            vibrateOnly -> CH_SILENT
            isAlarm -> CH_ALARM
            else -> CH_NOTIFY
        }

        val content = buildString {
            append(className.ifBlank { "" })
            if (location.isNotBlank()) append(" · ").append(location)
            if (time.isNotBlank()) append("\n时间：").append(time)
            if (advance > 0) append("\n（").append(advance).append(" 分钟后开始）")
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_course_id", courseId)
        }
        val pi = PendingIntent.getActivity(
            context, courseId.toInt().coerceAtLeast(1), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channel)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title.ifBlank { "上课提醒" })
            .setContentText(content.replace("\n", " "))
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)

        if (vibrateOnly) {
            builder.setVibrate(longArrayOf(0, 800, 400, 800))
            builder.setSilent(true)
        } else {
            builder.setVibrate(longArrayOf(0, 600, 400, 600))
        }

        try {
            NotificationManagerCompat.from(context)
                .notify((courseId.toInt() * 31 + time.hashCode()).coerceAtLeast(1), builder.build())
        } catch (_: SecurityException) {
        }
    }
}
