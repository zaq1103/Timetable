package com.example.teachertimetable.data

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
