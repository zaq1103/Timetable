package com.example.teachertimetable.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore("tt_settings")

data class GlobalSettings(
    val termStartEpochDay: Long = defaultMondayEpoch(),
    val totalWeeks: Int = 20,
    val defaultReminderType: ReminderType = ReminderType.NOTIFICATION,
    val defaultAdvance: Int = 10,
    val defaultVibrateOnly: Boolean = false,
    val myClasses: List<String> = emptyList(),   // 预设执教班级
    val mySubjects: List<String> = emptyList()   // 预设执教学科
)

fun defaultMondayEpoch(): Long {
    val today = LocalDate.now()
    return today.minusDays((today.dayOfWeek.value - DayOfWeek.MONDAY.value).toLong()).toEpochDay()
}

class SettingsStore(private val context: Context) {

    private object K {
        val TERM_START = longPreferencesKey("term_start")
        val TOTAL_WEEKS = intPreferencesKey("total_weeks")
        val DEF_TYPE = stringPreferencesKey("def_type")
        val DEF_ADVANCE = intPreferencesKey("def_advance")
        val DEF_VIBRATE = booleanPreferencesKey("def_vibrate")
        val MY_CLASSES = stringSetPreferencesKey("my_classes")
        val MY_SUBJECTS = stringSetPreferencesKey("my_subjects")
    }

    val flow: Flow<GlobalSettings> = context.dataStore.data.map { p ->
        GlobalSettings(
            termStartEpochDay = p[K.TERM_START] ?: defaultMondayEpoch(),
            totalWeeks = p[K.TOTAL_WEEKS] ?: 20,
            defaultReminderType = p[K.DEF_TYPE]?.let {
                runCatching { ReminderType.valueOf(it) }.getOrNull()
            } ?: ReminderType.NOTIFICATION,
            defaultAdvance = p[K.DEF_ADVANCE] ?: 10,
            defaultVibrateOnly = p[K.DEF_VIBRATE] ?: false,
            myClasses = p[K.MY_CLASSES]?.toList()?.sorted() ?: emptyList(),
            mySubjects = p[K.MY_SUBJECTS]?.toList()?.sorted() ?: emptyList()
        )
    }

    suspend fun update(s: GlobalSettings) {
        context.dataStore.edit { p ->
            p[K.TERM_START] = s.termStartEpochDay
            p[K.TOTAL_WEEKS] = s.totalWeeks
            p[K.DEF_TYPE] = s.defaultReminderType.name
            p[K.DEF_ADVANCE] = s.defaultAdvance
            p[K.DEF_VIBRATE] = s.defaultVibrateOnly
            p[K.MY_CLASSES] = s.myClasses.toSet()
            p[K.MY_SUBJECTS] = s.mySubjects.toSet()
        }
    }
}
