package com.example.teachertimetable

import android.app.Application
import com.example.teachertimetable.data.AppDatabase
import com.example.teachertimetable.data.Repository
import com.example.teachertimetable.data.SettingsStore
import com.example.teachertimetable.reminder.NotificationHelper
import com.example.teachertimetable.reminder.ReminderScheduler
import com.example.teachertimetable.reminder.RescheduleWorker

class TimetableApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.get(this) }
    val repository: Repository by lazy { Repository(database) }
    val settings: SettingsStore by lazy { SettingsStore(this) }
    val scheduler: ReminderScheduler by lazy { ReminderScheduler(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannels(this)
        RescheduleWorker.enqueue(this)
    }
}
