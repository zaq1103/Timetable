package com.example.teachertimetable.ui

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
