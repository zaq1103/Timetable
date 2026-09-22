package com.example.teachertimetable.data

class Repository(private val db: AppDatabase) {
    val courseDao = db.courseDao()
    val overrideDao = db.overrideDao()
    val memoDao = db.memoDao()
}
