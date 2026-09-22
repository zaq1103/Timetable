package com.example.teachertimetable.data

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
