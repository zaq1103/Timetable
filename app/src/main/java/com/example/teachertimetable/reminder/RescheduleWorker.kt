package com.example.teachertimetable.reminder

import android.content.Context
import androidx.work.*
import com.example.teachertimetable.TimetableApp
import com.example.teachertimetable.data.ScheduleEngine
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class RescheduleWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as TimetableApp
        val courses = app.repository.courseDao.getAll()
        val overrides = app.repository.overrideDao.getAll()
        val s = app.settings.flow.first()
        val termStart = LocalDate.ofEpochDay(s.termStartEpochDay)
        val sessions = ScheduleEngine.buildSessions(courses, overrides, termStart, s.totalWeeks)
        app.scheduler.scheduleAll(sessions, s)
        return Result.success()
    }

    companion object {
        const val NAME = "reschedule_worker"
        fun enqueue(context: Context) {
            val req = PeriodicWorkRequestBuilder<RescheduleWorker>(6, TimeUnit.HOURS)
                .setInitialDelay(1, TimeUnit.MINUTES)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(NAME, ExistingPeriodicWorkPolicy.REPLACE, req)
        }
    }
}
