#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
教师课程表 Android 工程生成器
运行方式：python gen_project.py
生成目录：脚本所在目录
"""

from pathlib import Path

ROOT = Path(__file__).resolve().parent
FILES = {}


def add(path: str, content: str):
    FILES[path] = content


# ============================================================
# 1. Gradle 工程配置
# ============================================================

add("settings.gradle.kts", r"""pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "TeacherTimetable"
include(":app")
""")

add("build.gradle.kts", r"""// Top-level build file
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}
""")

add("gradle.properties", r"""android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
""")

add("gradle/wrapper/gradle-wrapper.properties", r"""distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.4-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
""")

add(".gitignore", r"""*.iml
.gradle
/local.properties
/.idea
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
""")

add("app/build.gradle.kts", r"""plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.teachertimetable"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.teachertimetable"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.10" }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
""")

add("app/proguard-rules.pro", r"""# 保留 Room 生成类
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**
""")

# ============================================================
# 2. AndroidManifest 与资源
# ============================================================

add("app/src/main/AndroidManifest.xml", r"""<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.USE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
    <uses-feature android:name="android.hardware.camera" android:required="false" />

    <application
        android:name=".TimetableApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.TeacherTimetable">

        <activity
            android:name=".ui.MainActivity"
            android:exported="true"
            android:showWhenLocked="true"
            android:turnScreenOn="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <receiver android:name=".reminder.AlarmReceiver" android:exported="false" />

        <receiver android:name=".reminder.BootReceiver" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
            </intent-filter>
        </receiver>

        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.fileprovider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" />
        </provider>
    </application>
</manifest>
""")

add("app/src/main/res/values/strings.xml", r"""<resources>
    <string name="app_name">教师课程表</string>
</resources>
""")

add("app/src/main/res/values/colors.xml", r"""<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="ic_launcher_background">#3F51B5</color>
</resources>
""")

add("app/src/main/res/values/themes.xml", r"""<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.TeacherTimetable" parent="@android:style/Theme.Material.Light.NoActionBar">
        <item name="android:windowBackground">@android:color/white</item>
        <item name="android:statusBarColor">#3F51B5</item>
    </style>
</resources>
""")

add("app/src/main/res/xml/file_paths.xml", r"""<?xml version="1.0" encoding="utf-8"?>
<paths>
    <files-path name="memo_images" path="memo_images/" />
    <cache-path name="cache" path="." />
</paths>
""")

add("app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml", r"""<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
""")

add("app/src/main/res/drawable/ic_launcher_foreground.xml", r"""<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M34,32h40v8h-40z" />
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M34,48h40v8h-40z" />
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M34,64h26v8h-26z" />
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M22,26h8v56h-8z" />
</vector>
""")

# ============================================================
# 3. Kotlin 源码 —— Application
# ============================================================

add("app/src/main/java/com/example/teachertimetable/TimetableApp.kt", r"""package com.example.teachertimetable

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
""")

# ============================================================
# 4. Kotlin 源码 —— data 层
# ============================================================

add("app/src/main/java/com/example/teachertimetable/data/Entities.kt", r"""package com.example.teachertimetable.data

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
""")

add("app/src/main/java/com/example/teachertimetable/data/Daos.kt", r"""package com.example.teachertimetable.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY dayOfWeek, startMinute")
    fun observeAll(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses")
    suspend fun getAll(): List<CourseEntity>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getById(id: Long): CourseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(course: CourseEntity): Long

    @Delete
    suspend fun delete(course: CourseEntity)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface OverrideDao {
    @Query("SELECT * FROM session_overrides")
    fun observeAll(): Flow<List<SessionOverrideEntity>>

    @Query("SELECT * FROM session_overrides")
    suspend fun getAll(): List<SessionOverrideEntity>

    @Query("SELECT * FROM session_overrides WHERE courseId=:c AND week=:w AND originDayOfWeek=:d LIMIT 1")
    suspend fun find(c: Long, w: Int, d: Int): SessionOverrideEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(o: SessionOverrideEntity): Long

    @Query("DELETE FROM session_overrides WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface MemoDao {
    @Query("SELECT * FROM memos ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<MemoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(m: MemoEntity): Long

    @Query("SELECT * FROM memos WHERE id = :id")
    suspend fun getById(id: Long): MemoEntity?

    @Query("DELETE FROM memos WHERE id = :id")
    suspend fun deleteById(id: Long)
}
""")

add("app/src/main/java/com/example/teachertimetable/data/AppDatabase.kt", r"""package com.example.teachertimetable.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class Converters {
    @TypeConverter fun courseTypeToString(v: CourseType): String = v.name
    @TypeConverter fun stringToCourseType(v: String): CourseType =
        runCatching { CourseType.valueOf(v) }.getOrDefault(CourseType.NORMAL)

    @TypeConverter fun reminderTypeToString(v: ReminderType): String = v.name
    @TypeConverter fun stringToReminderType(v: String): ReminderType =
        runCatching { ReminderType.valueOf(v) }.getOrDefault(ReminderType.NONE)
}

@Database(
    entities = [CourseEntity::class, SessionOverrideEntity::class, MemoEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun overrideDao(): OverrideDao
    abstract fun memoDao(): MemoDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun get(ctx: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                ctx.applicationContext, AppDatabase::class.java, "timetable.db"
            ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
        }
    }
}
""")

add("app/src/main/java/com/example/teachertimetable/data/Repository.kt", r"""package com.example.teachertimetable.data

class Repository(private val db: AppDatabase) {
    val courseDao = db.courseDao()
    val overrideDao = db.overrideDao()
    val memoDao = db.memoDao()
}
""")

add("app/src/main/java/com/example/teachertimetable/data/ScheduleEngine.kt", r"""package com.example.teachertimetable.data

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
""")

add("app/src/main/java/com/example/teachertimetable/data/SettingsStore.kt", r"""package com.example.teachertimetable.data

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
    val defaultVibrateOnly: Boolean = false
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
    }

    val flow: Flow<GlobalSettings> = context.dataStore.data.map { p ->
        GlobalSettings(
            termStartEpochDay = p[K.TERM_START] ?: defaultMondayEpoch(),
            totalWeeks = p[K.TOTAL_WEEKS] ?: 20,
            defaultReminderType = p[K.DEF_TYPE]?.let {
                runCatching { ReminderType.valueOf(it) }.getOrNull()
            } ?: ReminderType.NOTIFICATION,
            defaultAdvance = p[K.DEF_ADVANCE] ?: 10,
            defaultVibrateOnly = p[K.DEF_VIBRATE] ?: false
        )
    }

    suspend fun update(s: GlobalSettings) {
        context.dataStore.edit { p ->
            p[K.TERM_START] = s.termStartEpochDay
            p[K.TOTAL_WEEKS] = s.totalWeeks
            p[K.DEF_TYPE] = s.defaultReminderType.name
            p[K.DEF_ADVANCE] = s.defaultAdvance
            p[K.DEF_VIBRATE] = s.defaultVibrateOnly
        }
    }
}
""")

# ============================================================
# 5. Kotlin 源码 —— reminder 层
# ============================================================

add("app/src/main/java/com/example/teachertimetable/reminder/ReminderScheduler.kt", r"""package com.example.teachertimetable.reminder

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
""")

add("app/src/main/java/com/example/teachertimetable/reminder/AlarmReceiver.kt", r"""package com.example.teachertimetable.reminder

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
""")

add("app/src/main/java/com/example/teachertimetable/reminder/NotificationHelper.kt", r"""package com.example.teachertimetable.reminder

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
""")

add("app/src/main/java/com/example/teachertimetable/reminder/BootReceiver.kt", r"""package com.example.teachertimetable.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        RescheduleWorker.enqueue(context)
    }
}
""")

add("app/src/main/java/com/example/teachertimetable/reminder/RescheduleWorker.kt", r"""package com.example.teachertimetable.reminder

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
""")

# ============================================================
# 6. Kotlin 源码 —— util 层
# ============================================================

add("app/src/main/java/com/example/teachertimetable/util/TimeUtils.kt", r"""package com.example.teachertimetable.util

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
""")

add("app/src/main/java/com/example/teachertimetable/util/ImageStore.kt", r"""package com.example.teachertimetable.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ImageStore {

    fun memoDir(context: Context): File =
        File(context.filesDir, "memo_images").apply { if (!exists()) mkdirs() }

    fun newImageFile(context: Context): File =
        File(memoDir(context), "IMG_" + System.currentTimeMillis() + ".jpg")

    fun uriFor(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)

    /** 把外部 Uri 拷贝进内部存储，返回绝对路径 */
    fun importFromUri(context: Context, uri: Uri): String? = runCatching {
        val dest = newImageFile(context)
        context.contentResolver.openInputStream(uri)!!.use { input ->
            FileOutputStream(dest).use { output -> input.copyTo(output) }
        }
        dest.absolutePath
    }.getOrNull()

    fun decodeScaled(path: String, maxSide: Int = 1600): Bitmap? = runCatching {
        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, opts)
        var scale = 1
        while (opts.outWidth / scale > maxSide || opts.outHeight / scale > maxSide) scale *= 2
        val opts2 = BitmapFactory.Options().apply { inSampleSize = scale }
        BitmapFactory.decodeFile(path, opts2)
    }.getOrNull()

    fun saveBitmap(bitmap: Bitmap, path: String): Boolean = runCatching {
        FileOutputStream(path).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 92, it) }
        true
    }.getOrDefault(false)

    fun saveBitmapAsNew(context: Context, bitmap: Bitmap): String? {
        val f = File(memoDir(context), "EDIT_" + System.currentTimeMillis() + ".jpg")
        return if (saveBitmap(bitmap, f.absolutePath)) f.absolutePath else null
    }
}
""")

# ============================================================
# 7. Kotlin 源码 —— UI 层
# ============================================================

add("app/src/main/java/com/example/teachertimetable/ui/theme/Theme.kt", r"""package com.example.teachertimetable.ui.theme

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
""")

add("app/src/main/java/com/example/teachertimetable/ui/AppViewModel.kt", r"""package com.example.teachertimetable.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teachertimetable.TimetableApp
import com.example.teachertimetable.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TimetableUiState(
    val courses: List<CourseEntity> = emptyList(),
    val overrides: List<SessionOverrideEntity> = emptyList(),
    val memos: List<MemoEntity> = emptyList(),
    val settings: GlobalSettings = GlobalSettings(),
    val sessions: List<ClassSession> = emptyList(),
    val week: Int = 1,
    val day: Int = 1
) {
    val weekSessions: List<ClassSession> get() = sessions.filter { it.week == week }
    fun daySessions(d: Int) =
        weekSessions.filter { it.dayOfWeek == d }.sortedBy { it.startMinute }

    val classNames: List<String>
        get() = courses.map { it.className }.filter { it.isNotBlank() }.distinct().sorted()
}

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val application = app as TimetableApp
    private val repo = application.repository
    private val settingsStore = application.settings

    private val _week = MutableStateFlow(1)
    private val _day = MutableStateFlow(LocalDate.now().dayOfWeek.value)

    val uiState: StateFlow<TimetableUiState> = combine(
        repo.courseDao.observeAll(),
        repo.overrideDao.observeAll(),
        repo.memoDao.observeAll(),
        settingsStore.flow,
        combine(_week, _day) { w, d -> w to d }
    ) { courses, overrides, memos, settings, wd ->
        val (w, d) = wd
        val termStart = LocalDate.ofEpochDay(settings.termStartEpochDay)
        val sessions = ScheduleEngine.buildSessions(
            courses, overrides, termStart, settings.totalWeeks
        )
        TimetableUiState(
            courses = courses,
            overrides = overrides,
            memos = memos,
            settings = settings,
            sessions = sessions,
            week = w,
            day = d
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimetableUiState())

    init {
        viewModelScope.launch {
            val s = settingsStore.flow.first()
            val termStart = LocalDate.ofEpochDay(s.termStartEpochDay)
            val w = ScheduleEngine.weekOf(LocalDate.now(), termStart)
                .coerceIn(1, s.totalWeeks)
            _week.value = w
        }
    }

    fun setWeek(w: Int) { _week.value = w.coerceAtLeast(1) }
    fun setDay(d: Int) { _day.value = d }
    fun nextWeek() = setWeek(_week.value + 1)
    fun prevWeek() = setWeek(_week.value - 1)

    // ---------- 课程 ----------
    fun saveCourse(course: CourseEntity, onDone: () -> Unit = {}) = viewModelScope.launch {
        repo.courseDao.upsert(course)
        rescheduleInternal()
        onDone()
    }

    fun deleteCourse(course: CourseEntity) = viewModelScope.launch {
        repo.courseDao.delete(course)
        rescheduleInternal()
    }

    suspend fun getCourse(id: Long) = repo.courseDao.getById(id)

    // ---------- 调课 ----------
    fun applyOverride(o: SessionOverrideEntity) = viewModelScope.launch {
        repo.overrideDao.upsert(o)
        rescheduleInternal()
    }

    fun clearOverride(courseId: Long, week: Int, day: Int) = viewModelScope.launch {
        repo.overrideDao.find(courseId, week, day)?.let { repo.overrideDao.deleteById(it.id) }
        rescheduleInternal()
    }

    // ---------- 备忘 ----------
    fun saveMemo(m: MemoEntity, onDone: () -> Unit = {}) = viewModelScope.launch {
        repo.memoDao.upsert(m)
        onDone()
    }

    fun deleteMemo(id: Long) = viewModelScope.launch { repo.memoDao.deleteById(id) }

    suspend fun getMemo(id: Long) = repo.memoDao.getById(id)

    // ---------- 设置 ----------
    fun saveSettings(s: GlobalSettings) = viewModelScope.launch {
        settingsStore.update(s)
        rescheduleInternal()
    }

    fun reschedule() = viewModelScope.launch { rescheduleInternal() }

    private suspend fun rescheduleInternal() {
        val s = settingsStore.flow.first()
        val termStart = LocalDate.ofEpochDay(s.termStartEpochDay)
        val sessions = ScheduleEngine.buildSessions(
            repo.courseDao.getAll(), repo.overrideDao.getAll(), termStart, s.totalWeeks
        )
        application.scheduler.scheduleAll(sessions, s)
    }
}
""")

add("app/src/main/java/com/example/teachertimetable/ui/MainActivity.kt", r"""package com.example.teachertimetable.ui

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.teachertimetable.ui.course.EditCourseScreen
import com.example.teachertimetable.ui.memo.EditMemoScreen
import com.example.teachertimetable.ui.memo.ImageEditorScreen
import com.example.teachertimetable.ui.memo.MemoScreen
import com.example.teachertimetable.ui.settings.SettingsScreen
import com.example.teachertimetable.ui.theme.TeacherTimetableTheme
import com.example.teachertimetable.ui.timetable.TimetableScreen

class MainActivity : ComponentActivity() {

    private val notifPermLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestExactAlarmIfNeeded()

        setContent {
            TeacherTimetableTheme {
                AppRoot()
            }
        }
    }

    private fun requestExactAlarmIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!am.canScheduleExactAlarms()) {
                runCatching {
                    startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                }
            }
        }
    }
}

@Composable
fun AppRoot() {
    val nav = rememberNavController()
    val vm: AppViewModel = viewModel()

    val tabs = listOf(
        Triple("timetable", "课表", Icons.Filled.DateRange),
        Triple("memo", "备忘", Icons.Filled.EditNote),
        Triple("settings", "设置", Icons.Filled.Settings)
    )

    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
        ?.substringBefore("?")?.substringBefore("/")
    val showBar = tabs.any { it.first == current }

    Scaffold(
        bottomBar = {
            if (showBar) {
                NavigationBar {
                    tabs.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            selected = current == route,
                            onClick = {
                                nav.navigate(route) {
                                    popUpTo("timetable") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, label) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = "timetable",
            modifier = Modifier.padding(padding)
        ) {
            composable("timetable") {
                TimetableScreen(
                    vm = vm,
                    onAddCourse = { nav.navigate("course?id=-1") },
                    onEditCourse = { id -> nav.navigate("course?id=" + id) },
                    onOpenMemo = { cls ->
                        nav.navigate("memoEdit?className=" + Uri.encode(cls))
                    }
                )
            }

            composable("memo") {
                MemoScreen(
                    vm = vm,
                    onEditMemo = { id -> nav.navigate("memoEdit?id=" + id) },
                    onNewClassMemo = { cls ->
                        nav.navigate("memoEdit?className=" + Uri.encode(cls))
                    }
                )
            }

            composable("settings") {
                SettingsScreen(vm = vm)
            }

            composable(
                "course?id={id}",
                arguments = listOf(navArgument("id") {
                    type = NavType.LongType; defaultValue = -1L
                })
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: -1L
                EditCourseScreen(
                    vm = vm,
                    courseId = id,
                    onBack = { nav.popBackStack() }
                )
            }

            composable(
                "memoEdit?id={id}&className={cls}&courseId={cid}&week={w}&day={d}",
                arguments = listOf(
                    navArgument("id") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("cls") { type = NavType.StringType; defaultValue = "" },
                    navArgument("cid") { type = NavType.LongType; defaultValue = -1L },
                    navArgument("w") { type = NavType.IntType; defaultValue = -1 },
                    navArgument("d") { type = NavType.IntType; defaultValue = -1 }
                )
            ) { entry ->
                val a = entry.arguments
                EditMemoScreen(
                    vm = vm,
                    memoId = a?.getLong("id") ?: -1L,
                    defaultClassName = a?.getString("cls").orEmpty(),
                    defaultCourseId = (a?.getLong("cid") ?: -1L).takeIf { it > 0 },
                    defaultWeek = (a?.getInt("w") ?: -1).takeIf { it > 0 },
                    defaultDay = (a?.getInt("d") ?: -1).takeIf { it > 0 },
                    savedStateHandle = entry.savedStateHandle,
                    onEditImage = { path ->
                        nav.navigate("imageEdit?path=" + Uri.encode(path))
                    },
                    onBack = { nav.popBackStack() }
                )
            }

            composable(
                "imageEdit?path={path}",
                arguments = listOf(navArgument("path") { type = NavType.StringType })
            ) { entry ->
                val path = Uri.decode(entry.arguments?.getString("path").orEmpty())
                ImageEditorScreen(
                    sourcePath = path,
                    onSaved = { newPath ->
                        nav.previousBackStackEntry?.savedStateHandle
                            ?.set("edited_image", newPath)
                        nav.popBackStack()
                    },
                    onBack = { nav.popBackStack() }
                )
            }
        }
    }
}
""")

add("app/src/main/java/com/example/teachertimetable/ui/timetable/TimetableScreen.kt", r"""package com.example.teachertimetable.ui.timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teachertimetable.data.ClassSession
import com.example.teachertimetable.data.CourseType
import com.example.teachertimetable.ui.AppViewModel
import com.example.teachertimetable.util.dayLabel
import com.example.teachertimetable.util.fmtTime
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    vm: AppViewModel,
    onAddCourse: () -> Unit,
    onEditCourse: (Long) -> Unit,
    onOpenMemo: (String) -> Unit
) {
    val state by vm.uiState.collectAsState()
    var selectedSession by remember { mutableStateOf<ClassSession?>(null) }

    val termStart = LocalDate.ofEpochDay(state.settings.termStartEpochDay)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("第 " + state.week + " 周") },
                navigationIcon = {
                    IconButton(onClick = { vm.prevWeek() }) {
                        Icon(Icons.Filled.ChevronLeft, "上一周")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.nextWeek() }) {
                        Icon(Icons.Filled.ChevronRight, "下一周")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCourse) {
                Icon(Icons.Filled.Add, "添加课程")
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {

            val weekStart = termStart.plusWeeks((state.week - 1).toLong())
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                (1..7).forEach { d ->
                    val date = weekStart.plusDays((d - 1).toLong())
                    val selected = state.day == d
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                            .clickable { vm.setDay(d) }
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(dayLabel(d), fontSize = 12.sp)
                        Text(
                            date.dayOfMonth.toString(),
                            fontSize = 15.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            HorizontalDivider(Modifier.padding(top = 4.dp))

            val list = state.daySessions(state.day)
            if (list.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("今天没有课，休息一下", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize().padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(list, key = { it.courseId.toString() + "-" + it.week + "-" + it.dayOfWeek }) { s ->
                        SessionCard(s) { selectedSession = s }
                    }
                }
            }
        }
    }

    selectedSession?.let { s ->
        SessionActionSheet(
            session = s,
            onDismiss = { selectedSession = null },
            onEdit = { onEditCourse(s.courseId); selectedSession = null },
            onMemo = { onOpenMemo(s.className); selectedSession = null }
        )
    }
}

@Composable
private fun SessionCard(s: ClassSession, onClick: () -> Unit) {
    val accent = Color(s.color)
    Card(
        onClick = onClick,
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(accent)
            )
            Column(Modifier.padding(12.dp).weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        s.name.ifBlank { s.className },
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (s.type != CourseType.NORMAL) {
                        AssistChip(
                            onClick = {},
                            label = { Text(s.type.label, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    fmtTime(s.startMinute) + " - " + fmtTime(s.endMinute) +
                            if (s.location.isNotBlank()) "  ·  " + s.location else "",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                if (s.className.isNotBlank()) {
                    Text(s.className, fontSize = 13.sp, color = accent)
                }
                if (s.isOverride) {
                    Text(
                        "已调课" + (s.overrideRemark?.let { "：" + it } ?: ""),
                        fontSize = 11.sp,
                        color = Color(0xFFE65100)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionActionSheet(
    session: ClassSession,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onMemo: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp)) {
            Text(
                session.name + "  ·  第" + session.week + "周 " + dayLabel(session.dayOfWeek),
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
            Spacer(Modifier.height(12.dp))
            ListItem(
                headlineContent = { Text("编辑课程（含调课）") },
                supportingContent = { Text("修改本周这一次，或修改整个学期的排课") },
                modifier = Modifier.clickable { onEdit() }
            )
            ListItem(
                headlineContent = { Text("写这节课的备忘录") },
                supportingContent = { Text("记录本次课的教学进度") },
                modifier = Modifier.clickable { onMemo() }
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
""")

add("app/src/main/java/com/example/teachertimetable/ui/course/EditCourseScreen.kt", r"""package com.example.teachertimetable.ui.course

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teachertimetable.data.*
import com.example.teachertimetable.ui.AppViewModel
import com.example.teachertimetable.util.dayLabel
import com.example.teachertimetable.util.fmtTime
import kotlinx.coroutines.launch

private val palette = listOf(
    0xFF5C6BC0, 0xFF26A69A, 0xFFEF5350, 0xFFFFA726,
    0xFF66BB6A, 0xFFAB47BC, 0xFF29B6F6, 0xFF8D6E63
).map { it.toInt() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCourseScreen(
    vm: AppViewModel,
    courseId: Long,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var loaded by remember { mutableStateOf(courseId <= 0) }

    var name by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var dayOfWeek by remember { mutableStateOf(1) }
    var startMinute by remember { mutableStateOf(8 * 60) }
    var endMinute by remember { mutableStateOf(8 * 60 + 45) }
    var type by remember { mutableStateOf(CourseType.NORMAL) }
    var startWeek by remember { mutableStateOf(1) }
    var endWeek by remember { mutableStateOf(20) }
    var parity by remember { mutableStateOf(0) }
    var color by remember { mutableStateOf(palette[0]) }

    var useGlobalReminder by remember { mutableStateOf(true) }
    var reminderType by remember { mutableStateOf(ReminderType.NOTIFICATION) }
    var advance by remember { mutableStateOf(10) }
    var vibrateOnly by remember { mutableStateOf(false) }

    var showSwapDialog by remember { mutableStateOf(false) }

    LaunchedEffect(courseId) {
        if (courseId > 0) {
            vm.getCourse(courseId)?.let { c ->
                name = c.name; className = c.className; location = c.location
                dayOfWeek = c.dayOfWeek; startMinute = c.startMinute; endMinute = c.endMinute
                type = c.type; startWeek = c.startWeek; endWeek = c.endWeek
                parity = c.weekParity; color = c.color
                useGlobalReminder = c.useGlobalReminder
                reminderType = c.reminderType; advance = c.reminderAdvance
                vibrateOnly = c.vibrateOnly
            }
            loaded = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (courseId > 0) "编辑课程" else "添加课程") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "返回") }
                },
                actions = {
                    if (courseId > 0) {
                        IconButton(onClick = {
                            scope.launch {
                                vm.getCourse(courseId)?.let { vm.deleteCourse(it) }
                                onBack()
                            }
                        }) { Icon(Icons.Filled.Delete, "删除") }
                    }
                }
            )
        }
    ) { pad ->
        if (!loaded) {
            Box(Modifier.padding(pad).fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("课程名称") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = className, onValueChange = { className = it },
                label = { Text("班级") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = location, onValueChange = { location = it },
                label = { Text("上课地点（可选）") }, modifier = Modifier.fillMaxWidth()
            )

            SectionTitle("课程类型")
            FlowRowSimple(
                options = CourseType.entries.toList(),
                labelOf = { it.label },
                selected = type,
                onSelect = { type = it }
            )

            SectionTitle("上课时间")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = {
                    TimePickerDialog(ctx, { _, h, m -> startMinute = h * 60 + m },
                        startMinute / 60, startMinute % 60, true).show()
                }, modifier = Modifier.weight(1f)) { Text("开始 " + fmtTime(startMinute)) }

                OutlinedButton(onClick = {
                    TimePickerDialog(ctx, { _, h, m -> endMinute = h * 60 + m },
                        endMinute / 60, endMinute % 60, true).show()
                }, modifier = Modifier.weight(1f)) { Text("结束 " + fmtTime(endMinute)) }
            }

            SectionTitle("星期")
            FlowRowSimple(
                options = (1..7).toList(),
                labelOf = { dayLabel(it) },
                selected = dayOfWeek,
                onSelect = { dayOfWeek = it }
            )

            SectionTitle("周次范围（覆盖整个学期）")
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumberField("起", startWeek, Modifier.weight(1f)) { startWeek = it.coerceAtLeast(1) }
                NumberField("止", endWeek, Modifier.weight(1f)) { endWeek = it.coerceAtLeast(startWeek) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf(0 to "每周", 1 to "单周", 2 to "双周").forEach { (v, l) ->
                    FilterChip(selected = parity == v, onClick = { parity = v }, label = { Text(l) })
                }
            }

            SectionTitle("颜色")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                palette.forEach { c ->
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(c))
                            .clickable { color = c }
                    ) {
                        if (color == c) {
                            Text(
                                "OK", color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }

            HorizontalDivider()
            SectionTitle("课前提醒")

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = useGlobalReminder, onCheckedChange = { useGlobalReminder = it })
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("使用全局默认提醒", fontWeight = FontWeight.Medium)
                    Text("在「设置」中统一配置，适合大多数课程", fontSize = 12.sp, color = Color.Gray)
                }
            }

            if (!useGlobalReminder) {
                SectionTitle("提醒方式")
                FlowRowSimple(
                    options = listOf(
                        ReminderType.NOTIFICATION, ReminderType.ALARM, ReminderType.NONE
                    ),
                    labelOf = {
                        when (it) {
                            ReminderType.NOTIFICATION -> "通知"
                            ReminderType.ALARM -> "闹钟"
                            ReminderType.NONE -> "不提醒"
                        }
                    },
                    selected = reminderType,
                    onSelect = { reminderType = it }
                )

                SectionTitle("提前时间（分钟）")
                FlowRowSimple(
                    options = listOf(0, 5, 10, 15, 30, 60),
                    labelOf = { if (it == 0) "准点" else it.toString() },
                    selected = advance,
                    onSelect = { advance = it }
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = vibrateOnly, onCheckedChange = { vibrateOnly = it })
                    Text("仅震动（静音，适合在教室/会议中）")
                }
            }

            if (courseId > 0) {
                HorizontalDivider()
                SectionTitle("调课 / 单次调整")
                Button(
                    onClick = { showSwapDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("只调整某一次课") }
                Text(
                    "例如：本周三第 3 节的课调到周五第 5 节，或临时取消一次。",
                    fontSize = 12.sp, color = Color.Gray
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val entity = CourseEntity(
                        id = if (courseId > 0) courseId else 0,
                        name = name.ifBlank { className.ifBlank { "未命名课程" } },
                        className = className,
                        location = location,
                        dayOfWeek = dayOfWeek,
                        startMinute = startMinute,
                        endMinute = endMinute,
                        type = type,
                        startWeek = startWeek,
                        endWeek = endWeek,
                        weekParity = parity,
                        color = color,
                        useGlobalReminder = useGlobalReminder,
                        reminderType = reminderType,
                        reminderAdvance = advance,
                        vibrateOnly = vibrateOnly
                    )
                    vm.saveCourse(entity) { onBack() }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text("保存") }

            Spacer(Modifier.height(30.dp))
        }
    }

    if (showSwapDialog && courseId > 0) {
        SwapDialog(
            vm = vm,
            courseId = courseId,
            defaultDay = dayOfWeek,
            defaultStart = startMinute,
            defaultEnd = endMinute,
            onDismiss = { showSwapDialog = false }
        )
    }
}

@Composable
private fun SectionTitle(t: String) {
    Text(
        t,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.primary
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> FlowRowSimple(
    options: List<T>,
    labelOf: (T) -> String,
    selected: T,
    onSelect: (T) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { o ->
            FilterChip(
                selected = o == selected,
                onClick = { onSelect(o) },
                label = { Text(labelOf(o)) }
            )
        }
    }
}

@Composable
private fun NumberField(label: String, value: Int, modifier: Modifier, onChange: (Int) -> Unit) {
    OutlinedTextField(
        value = value.toString(),
        onValueChange = { s -> s.toIntOrNull()?.let(onChange) },
        label = { Text(label) },
        singleLine = true,
        modifier = modifier
    )
}

@Composable
private fun SwapDialog(
    vm: AppViewModel,
    courseId: Long,
    defaultDay: Int,
    defaultStart: Int,
    defaultEnd: Int,
    onDismiss: () -> Unit
) {
    val ctx = LocalContext.current
    var week by remember { mutableStateOf(1) }
    var originDay by remember { mutableStateOf(defaultDay) }
    var newDay by remember { mutableStateOf(defaultDay) }
    var newStart by remember { mutableStateOf(defaultStart) }
    var newEnd by remember { mutableStateOf(defaultEnd) }
    var cancelled by remember { mutableStateOf(false) }
    var remark by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("单次调课") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                NumberField("第几周", week, Modifier.fillMaxWidth()) { week = it.coerceAtLeast(1) }

                Text("原定星期：" + dayLabel(originDay), fontSize = 13.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..7).forEach { d ->
                        FilterChip(
                            selected = originDay == d,
                            onClick = { originDay = d },
                            label = { Text(dayLabel(d).removePrefix("周"), fontSize = 11.sp) }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = cancelled, onCheckedChange = { cancelled = it })
                    Text("本次取消（不上课）")
                }

                if (!cancelled) {
                    Text("调整到：" + dayLabel(newDay), fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..7).forEach { d ->
                            FilterChip(
                                selected = newDay == d,
                                onClick = { newDay = d },
                                label = { Text(dayLabel(d).removePrefix("周"), fontSize = 11.sp) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            TimePickerDialog(ctx, { _, h, m -> newStart = h * 60 + m },
                                newStart / 60, newStart % 60, true).show()
                        }, modifier = Modifier.weight(1f)) { Text(fmtTime(newStart)) }
                        OutlinedButton(onClick = {
                            TimePickerDialog(ctx, { _, h, m -> newEnd = h * 60 + m },
                                newEnd / 60, newEnd % 60, true).show()
                        }, modifier = Modifier.weight(1f)) { Text(fmtTime(newEnd)) }
                    }
                }

                OutlinedTextField(
                    value = remark, onValueChange = { remark = it },
                    label = { Text("备注（可选）") }, modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                vm.applyOverride(
                    SessionOverrideEntity(
                        courseId = courseId,
                        week = week,
                        originDayOfWeek = originDay,
                        newDayOfWeek = if (cancelled) null else newDay,
                        newStartMinute = if (cancelled) null else newStart,
                        newEndMinute = if (cancelled) null else newEnd,
                        cancelled = cancelled,
                        remark = remark.ifBlank { null }
                    )
                )
                onDismiss()
            }) { Text("确定") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
""")

add("app/src/main/java/com/example/teachertimetable/ui/memo/MemoScreen.kt", r"""package com.example.teachertimetable.ui.memo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.teachertimetable.data.MemoEntity
import com.example.teachertimetable.ui.AppViewModel
import com.example.teachertimetable.util.fmtDateFull
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoScreen(
    vm: AppViewModel,
    onEditMemo: (Long) -> Unit,
    onNewClassMemo: (String) -> Unit
) {
    val state by vm.uiState.collectAsState()
    val grouped = state.memos.groupBy { it.className.ifBlank { "未分班" } }

    Scaffold(
        topBar = { TopAppBar(title = { Text("教学备忘录") }) }
    ) { pad ->
        if (grouped.isEmpty()) {
            Box(Modifier.padding(pad).fillMaxSize(), Alignment.Center) {
                Text(
                    "还没有备忘录，去「课表」里点一节课开始记录吧",
                    color = Color.Gray
                )
            }
            return@Scaffold
        }

        LazyColumn(
            Modifier.padding(pad).fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            grouped.forEach { (cls, memos) ->
                item(key = "header_" + cls) {
                    ClassMemoCard(
                        className = cls,
                        memos = memos,
                        onEditMemo = onEditMemo,
                        onAddClassMemo = { onNewClassMemo(cls) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassMemoCard(
    className: String,
    memos: List<MemoEntity>,
    onEditMemo: (Long) -> Unit,
    onAddClassMemo: () -> Unit
) {
    val classMemos = memos.filter { it.isClassLevel }
    val sessionMemos = memos.filter { !it.isClassLevel }
    var expanded by remember { mutableStateOf(false) }

    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(14.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    className,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onAddClassMemo) {
                    Icon(Icons.Filled.Add, "新增班级备忘")
                }
            }

            if (classMemos.isEmpty()) {
                Text("暂无班级总备忘录", fontSize = 13.sp, color = Color.Gray)
            } else {
                classMemos.take(3).forEach { m -> MemoRow(m, onEditMemo) }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "单次课程备忘 (" + sessionMemos.size + ")",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    null
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(top = 6.dp)) {
                    if (sessionMemos.isEmpty()) {
                        Text("暂无", fontSize = 13.sp, color = Color.Gray)
                    } else {
                        sessionMemos.sortedByDescending { it.createdAt }.forEach { m ->
                            Column(Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    buildString {
                                        append(m.courseName.ifBlank { "课程" })
                                        m.week?.let { append("  ·  第").append(it).append("周") }
                                        m.epochDay?.let {
                                            append("  ·  ")
                                            append(fmtDateFull(LocalDate.ofEpochDay(it)))
                                        }
                                    },
                                    fontSize = 12.sp,
                                    color = Color(0xFF5C6BC0)
                                )
                                MemoRow(m, onEditMemo, compact = true)
                                HorizontalDivider(Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoRow(m: MemoEntity, onEdit: (Long) -> Unit, compact: Boolean = false) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable { onEdit(m.id) }
            .padding(vertical = 6.dp)
    ) {
        if (m.title.isNotBlank()) {
            Text(
                m.title,
                fontWeight = FontWeight.Medium,
                fontSize = if (compact) 13.sp else 15.sp
            )
        }
        if (m.content.isNotBlank()) {
            Text(
                m.content,
                fontSize = if (compact) 12.sp else 14.sp,
                maxLines = if (compact) 2 else 4,
                color = Color.DarkGray
            )
        }
        if (m.imageList.isNotEmpty()) {
            Row(
                Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                m.imageList.take(3).forEach { p ->
                    AsyncImage(
                        model = File(p),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(if (compact) 44.dp else 64.dp)
                    )
                }
                if (m.imageList.size > 3) {
                    Text(
                        "+" + (m.imageList.size - 3),
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }
        }
    }
}
""")

add("app/src/main/java/com/example/teachertimetable/ui/memo/EditMemoScreen.kt", r"""package com.example.teachertimetable.ui.memo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import coil.compose.AsyncImage
import com.example.teachertimetable.data.MemoEntity
import com.example.teachertimetable.ui.AppViewModel
import com.example.teachertimetable.util.ImageStore
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMemoScreen(
    vm: AppViewModel,
    memoId: Long,
    defaultClassName: String,
    defaultCourseId: Long?,
    defaultWeek: Int?,
    defaultDay: Int?,
    savedStateHandle: SavedStateHandle,
    onEditImage: (String) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current

    var className by remember { mutableStateOf(defaultClassName) }
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var images by remember { mutableStateOf(listOf<String>()) }
    var courseId by remember { mutableStateOf(defaultCourseId) }
    var courseName by remember { mutableStateOf("") }
    var week by remember { mutableStateOf(defaultWeek) }
    var day by remember { mutableStateOf(defaultDay) }
    var loaded by remember { mutableStateOf(memoId <= 0) }

    var pendingCameraPath by remember { mutableStateOf<String?>(null) }

    val pickLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        ImageStore.importFromUri(ctx, uri)?.let { images = images + it }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok ->
        if (ok) pendingCameraPath?.let { images = images + it }
    }

    LaunchedEffect(memoId) {
        if (memoId > 0) {
            vm.getMemo(memoId)?.let { m ->
                className = m.className
                title = m.title
                content = m.content
                images = m.imageList
                courseId = m.courseId
                courseName = m.courseName
                week = m.week
                day = m.dayOfWeek
            }
            loaded = true
        }
    }

    // 接收图片编辑器返回
    LaunchedEffect(Unit) {
        savedStateHandle.getStateFlow("edited_image", "").collect { p ->
            if (p.isNotBlank()) {
                images = images + p
                savedStateHandle["edited_image"] = ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (courseId == null) "班级备忘录" else "课程备忘录") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "返回") }
                }
            )
        }
    ) { pad ->
        if (!loaded) {
            Box(Modifier.padding(pad).fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = className, onValueChange = { className = it },
                label = { Text("班级") }, modifier = Modifier.fillMaxWidth()
            )

            if (courseId != null) {
                OutlinedTextField(
                    value = courseName, onValueChange = { courseName = it },
                    label = { Text("课程名称") }, modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = week?.toString() ?: "",
                        onValueChange = { week = it.toIntOrNull() },
                        label = { Text("第几周") }, modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = day?.toString() ?: "",
                        onValueChange = { day = it.toIntOrNull() },
                        label = { Text("星期") }, modifier = Modifier.weight(1f)
                    )
                }
            }

            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("标题（可选）") }, modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = content, onValueChange = { content = it },
                label = { Text("教学进度 / 备注") },
                minLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "图片",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    val f = ImageStore.newImageFile(ctx)
                    pendingCameraPath = f.absolutePath
                    val uri = FileProvider.getUriForFile(
                        ctx, ctx.packageName + ".fileprovider", f
                    )
                    cameraLauncher.launch(uri)
                }) { Icon(Icons.Filled.AddAPhoto, "拍照") }

                IconButton(onClick = {
                    pickLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) { Icon(Icons.Filled.Add, "从相册选择") }
            }

            if (images.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(images) { p ->
                        Box {
                            AsyncImage(
                                model = File(p),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(96.dp)
                            )
                            IconButton(
                                onClick = { images = images - p },
                                modifier = Modifier.align(Alignment.TopEnd).size(24.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Close, "删除",
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = { onEditImage(p) },
                                modifier = Modifier.align(Alignment.BottomEnd).size(28.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Edit, "编辑",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val now = System.currentTimeMillis()
                    val entity = MemoEntity(
                        id = if (memoId > 0) memoId else 0,
                        className = className.ifBlank { "未分班" },
                        courseId = courseId,
                        courseName = courseName,
                        week = week,
                        dayOfWeek = day,
                        title = title,
                        content = content,
                        images = images.joinToString(","),
                        createdAt = now,
                        updatedAt = now
                    )
                    vm.saveMemo(entity) { onBack() }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text("保存") }

            Spacer(Modifier.height(30.dp))
        }
    }
}
""")

add("app/src/main/java/com/example/teachertimetable/ui/memo/ImageEditorScreen.kt", r"""package com.example.teachertimetable.ui.memo

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path as ComposePath
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.teachertimetable.util.ImageStore

private data class DrawStroke(
    val points: MutableList<Offset> = mutableListOf(),
    val color: Color,
    val width: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageEditorScreen(
    sourcePath: String,
    onSaved: (String) -> Unit,
    onBack: () -> Unit
) {
    val ctx = LocalContext.current

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val strokes = remember { mutableStateListOf<DrawStroke>() }
    var color by remember { mutableStateOf(Color.Red) }
    var strokeWidth by remember { mutableStateOf(0.008f) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(sourcePath) {
        bitmap = ImageStore.decodeScaled(sourcePath)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("图片编辑") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex)
                    }) { Icon(Icons.Filled.Undo, "撤销") }

                    IconButton(onClick = {
                        val bmp = bitmap ?: return@IconButton
                        val out = bmp.copy(Bitmap.Config.ARGB_8888, true)
                        val canvas = Canvas(out)
                        val paint = Paint().apply {
                            isAntiAlias = true
                            style = Paint.Style.STROKE
                            strokeCap = Paint.Cap.ROUND
                            strokeJoin = Paint.Join.ROUND
                        }
                        strokes.forEach { s ->
                            paint.color = android.graphics.Color.argb(
                                (s.color.alpha * 255).toInt(),
                                (s.color.red * 255).toInt(),
                                (s.color.green * 255).toInt(),
                                (s.color.blue * 255).toInt()
                            )
                            paint.strokeWidth = s.width * out.width
                            val path = Path()
                            s.points.forEachIndexed { i, p ->
                                val x = p.x * out.width
                                val y = p.y * out.height
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            canvas.drawPath(path, paint)
                        }
                        val saved = ImageStore.saveBitmapAsNew(ctx, out)
                        if (saved != null) onSaved(saved)
                    }) { Icon(Icons.Filled.Check, "保存") }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad).fillMaxSize()) {

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black)
                    .onSizeChanged { canvasSize = it }
            ) {
                val bmp = bitmap
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(bmp, color, strokeWidth) {
                                detectDragGestures(
                                    onDragStart = { off ->
                                        val rect = fitRect(bmp, size)
                                        if (rect.contains(off)) {
                                            val nx = (off.x - rect.left) / rect.width
                                            val ny = (off.y - rect.top) / rect.height
                                            strokes.add(
                                                DrawStroke(
                                                    mutableListOf(Offset(nx, ny)),
                                                    color,
                                                    strokeWidth
                                                )
                                            )
                                        }
                                    },
                                    onDrag = { change, _ ->
                                        val rect = fitRect(bmp, size)
                                        val p = change.position
                                        if (rect.contains(p) && strokes.isNotEmpty()) {
                                            val nx = (p.x - rect.left) / rect.width
                                            val ny = (p.y - rect.top) / rect.height
                                            strokes.last().points.add(Offset(nx, ny))
                                        }
                                        change.consume()
                                    }
                                )
                            }
                    )

                    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                        val rect = fitRect(bmp, canvasSize)
                        if (rect.width <= 0f) return@Canvas
                        strokes.forEach { s ->
                            val path = ComposePath()
                            s.points.forEachIndexed { i, p ->
                                val x = rect.left + p.x * rect.width
                                val y = rect.top + p.y * rect.height
                                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                            }
                            drawPath(
                                path = path,
                                color = s.color,
                                style = Stroke(
                                    width = s.width * rect.width,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                } else {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }

            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                Text("画笔颜色", style = MaterialTheme.typography.labelMedium)
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val colors = listOf(
                        Color.Red, Color.Blue, Color.Green,
                        Color.Yellow, Color.White, Color.Black
                    )
                    colors.forEach { c ->
                        Box(
                            Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(c)
                                .clickable { color = c }
                        ) {
                            if (color == c) {
                                Icon(
                                    Icons.Filled.Check, null,
                                    tint = if (c == Color.White || c == Color.Yellow)
                                        Color.Black else Color.White,
                                    modifier = Modifier.size(18.dp).align(Alignment.Center)
                                )
                            }
                        }
                    }
                }

                Text("粗细", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = strokeWidth,
                    onValueChange = { strokeWidth = it },
                    valueRange = 0.002f..0.03f
                )
            }
        }
    }
}

private fun fitRect(bmp: Bitmap, size: IntSize): Rect {
    if (size.width == 0 || size.height == 0) return Rect.Zero
    val scale = minOf(
        size.width.toFloat() / bmp.width,
        size.height.toFloat() / bmp.height
    )
    val w = bmp.width * scale
    val h = bmp.height * scale
    val left = (size.width - w) / 2f
    val top = (size.height - h) / 2f
    return Rect(left, top, left + w, top + h)
}
""")

add("app/src/main/java/com/example/teachertimetable/ui/settings/SettingsScreen.kt", r"""package com.example.teachertimetable.ui.settings

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teachertimetable.data.ReminderType
import com.example.teachertimetable.ui.AppViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: AppViewModel) {
    val ctx = LocalContext.current
    val state by vm.uiState.collectAsState()
    val s = state.settings

    var totalWeeks by remember(s) { mutableStateOf(s.totalWeeks) }
    var reminderType by remember(s) { mutableStateOf(s.defaultReminderType) }
    var advance by remember(s) { mutableStateOf(s.defaultAdvance) }
    var vibrateOnly by remember(s) { mutableStateOf(s.defaultVibrateOnly) }

    Scaffold(topBar = { TopAppBar(title = { Text("设置") }) }) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card {
                Column(
                    Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("学期设置", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = totalWeeks.toString(),
                        onValueChange = { totalWeeks = it.toIntOrNull() ?: totalWeeks },
                        label = { Text("学期总周数") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "学期起始（周一）：" + LocalDate.ofEpochDay(s.termStartEpochDay).toString(),
                        fontSize = 13.sp
                    )
                    OutlinedButton(onClick = {
                        val today = LocalDate.now()
                        val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
                        vm.saveSettings(
                            s.copy(
                                termStartEpochDay = monday.toEpochDay(),
                                totalWeeks = totalWeeks
                            )
                        )
                    }) { Text("把本周设为第 1 周") }
                }
            }

            Card {
                Column(
                    Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("默认提醒", fontWeight = FontWeight.Bold)
                    Text(
                        "未被课程单独覆盖时使用",
                        fontSize = 12.sp, color = Color.Gray
                    )

                    Text("方式", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            ReminderType.NOTIFICATION to "通知",
                            ReminderType.ALARM to "闹钟",
                            ReminderType.NONE to "关闭"
                        ).forEach { (t, l) ->
                            FilterChip(
                                selected = reminderType == t,
                                onClick = { reminderType = t },
                                label = { Text(l) }
                            )
                        }
                    }

                    Text("提前时间（分钟）", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0, 5, 10, 15, 30, 60).forEach { m ->
                            FilterChip(
                                selected = advance == m,
                                onClick = { advance = m },
                                label = { Text(if (m == 0) "准点" else m.toString()) }
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = vibrateOnly,
                            onCheckedChange = { vibrateOnly = it }
                        )
                        Text("闹钟提醒仅震动")
                    }
                }
            }

            Card {
                Column(
                    Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("权限与系统", fontWeight = FontWeight.Bold)

                    Button(
                        onClick = { vm.reschedule() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("重新排定全部提醒") }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Button(onClick = {
                            runCatching {
                                ctx.startActivity(
                                    Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text("申请精确闹钟权限") }
                    }

                    val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        am.canScheduleExactAlarms() else true

                    Text(
                        if (canExact) "精确闹钟：已授权"
                        else "精确闹钟：未授权（提醒可能延迟）",
                        fontSize = 12.sp,
                        color = if (canExact) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }

            Button(
                onClick = {
                    vm.saveSettings(
                        s.copy(
                            totalWeeks = totalWeeks,
                            defaultReminderType = reminderType,
                            defaultAdvance = advance,
                            defaultVibrateOnly = vibrateOnly
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) { Text("保存设置") }

            Spacer(Modifier.height(30.dp))
        }
    }
}
""")

# ============================================================
# 8. README
# ============================================================

add("README.md", r"""# 教师课程表 TeacherTimetable

一个面向教师的 Android 课程表 App，支持：

- 每周循环排课、早读 / 午练 / 晚自习等特殊时段、单双周、周次范围
- 单次调课 / 取消（只影响某一周）
- 班级总备忘录 + 单次课程备忘录（折叠展示），支持拍照 + 手绘标注
- 课前提醒：通知 / 闹钟（仅震动）、全局默认或单课程覆盖、提前时间自由设置
- 开机自动重排提醒（WorkManager + AlarmManager）

## 技术栈

Kotlin + Jetpack Compose + Room + DataStore + AlarmManager + WorkManager + Coil

## 快速开始

1. 用 Android Studio（Hedgehog 或更新版本）打开本目录
2. 等待 Gradle Sync 完成
3. 连接设备或启动模拟器，点击 Run

## 打包 APK

见项目根目录说明或 Android Studio 菜单：
Build -> Build Bundle(s) / APK(s) -> Build APK(s)
""")

# ============================================================
# 生成
# ============================================================

def main():
    print("=" * 60)
    print("  教师课程表 Android 工程生成器")
    print("=" * 60)
    print("输出目录：", ROOT)
    print()

    for rel, content in sorted(FILES.items()):
        target = ROOT / rel
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(content, encoding="utf-8")
        print("  [OK] " + rel)

    print()
    print("=" * 60)
    print("  共生成 {} 个文件".format(len(FILES)))
    print("=" * 60)
    print()
    print("下一步：")
    print("  1) 用 Android Studio 打开目录：", ROOT)
    print("  2) 等待 Gradle Sync 完成（首次会自动下载依赖）")
    print("  3) 连接设备 / 启动模拟器，点击 Run")
    print()
    print("打包 APK：")
    print("  Build -> Build Bundle(s) / APK(s) -> Build APK(s)")
    print("  或命令行：./gradlew assembleDebug")
    print()


if __name__ == "__main__":
    main()