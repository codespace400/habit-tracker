package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Habit
import com.example.data.HabitLog
import com.example.data.HabitRepository
import com.example.data.HabitWithStats
import com.example.data.StreakCalculator
import com.example.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    DETAIL,
    ADD_EDIT,
    STATS,
    SETTINGS
}

data class AppSettings(
    val notificationsEnabled: Boolean = true,
    val weekStartDay: String = "Monday",
    val themeMode: String = "Light",
    val motivationalQuotes: Boolean = true,
    val celebrationTrigger: Long = 0L,
    val celebrationMessage: String = ""
)

data class HabitUiState(
    val habits: List<HabitWithStats> = emptyList(),
    val isLoading: Boolean = true,
    val todayDateDisplay: String = DateUtils.getTodayDisplayString(),
    val totalHabitsCount: Int = 0,
    val completedTodayCount: Int = 0,
    val overallCompletionRate: Int = 0,
    val maxStreakOverall: Int = 0,
    val allCompletionsCount: Int = 0,
    val celebrationTrigger: Long = 0L,
    val celebrationMessage: String = "",
    val currentScreen: AppScreen = AppScreen.HOME,
    val previousScreen: AppScreen = AppScreen.HOME,
    val selectedHabitId: Long? = null,
    val editingHabitId: Long? = null,
    // Settings
    val notificationsEnabled: Boolean = true,
    val weekStartDay: String = "Monday",
    val themeMode: String = "Light",
    val motivationalQuotes: Boolean = true
)

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HabitRepository

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    private val _previousScreen = MutableStateFlow(AppScreen.HOME)
    private val _selectedHabitId = MutableStateFlow<Long?>(null)
    private val _editingHabitId = MutableStateFlow<Long?>(null)
    private val _settings = MutableStateFlow(AppSettings())

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HabitRepository(database.habitDao())

        // Seed initial habits if database is empty
        viewModelScope.launch {
            repository.seedDefaultDataIfEmpty()
        }
    }

    private val habitsWithStatsFlow: Flow<Pair<List<HabitWithStats>, Int>> = combine(
        repository.allHabits,
        repository.allLogs
    ) { habitsList, logsList ->
        val recent7Days = DateUtils.getLastNDays(7)
        val todayStr = DateUtils.getTodayString()

        val habitsWithStats = habitsList.map { habit ->
            val habitLogs = logsList.filter { it.habitId == habit.id }
            StreakCalculator.calculateStats(habit, habitLogs, recent7Days, todayStr)
        }
        Pair(habitsWithStats, logsList.size)
    }

    val uiState: StateFlow<HabitUiState> = combine(
        habitsWithStatsFlow,
        _currentScreen,
        _selectedHabitId,
        _editingHabitId,
        _settings
    ) { (habitsWithStats, totalLogsCount), screen, selectedId, editingId, settings ->
        val completedToday = habitsWithStats.count { it.isCompletedToday }
        val maxStreak = habitsWithStats.maxOfOrNull { it.currentStreak } ?: 0
        val avgCompletionRate = if (habitsWithStats.isNotEmpty()) {
            habitsWithStats.sumOf { it.completionRatePercent } / habitsWithStats.size
        } else 0

        HabitUiState(
            habits = habitsWithStats,
            isLoading = false,
            todayDateDisplay = DateUtils.getTodayDisplayString(),
            totalHabitsCount = habitsWithStats.size,
            completedTodayCount = completedToday,
            overallCompletionRate = avgCompletionRate,
            maxStreakOverall = maxStreak,
            allCompletionsCount = totalLogsCount,
            celebrationTrigger = settings.celebrationTrigger,
            celebrationMessage = settings.celebrationMessage,
            currentScreen = screen,
            previousScreen = _previousScreen.value,
            selectedHabitId = selectedId,
            editingHabitId = editingId,
            notificationsEnabled = settings.notificationsEnabled,
            weekStartDay = settings.weekStartDay,
            themeMode = settings.themeMode,
            motivationalQuotes = settings.motivationalQuotes
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HabitUiState()
    )

    fun navigateTo(screen: AppScreen) {
        _previousScreen.value = _currentScreen.value
        _currentScreen.value = screen
    }

    fun openHabitDetail(habitId: Long) {
        _selectedHabitId.value = habitId
        _previousScreen.value = _currentScreen.value
        _currentScreen.value = AppScreen.DETAIL
    }

    fun openAddHabit() {
        _editingHabitId.value = null
        _previousScreen.value = _currentScreen.value
        _currentScreen.value = AppScreen.ADD_EDIT
    }

    fun openEditHabit(habitId: Long) {
        _editingHabitId.value = habitId
        _previousScreen.value = _currentScreen.value
        _currentScreen.value = AppScreen.ADD_EDIT
    }

    fun navigateBack(): Boolean {
        if (_currentScreen.value == AppScreen.DETAIL || _currentScreen.value == AppScreen.ADD_EDIT) {
            _currentScreen.value = _previousScreen.value
            return true
        } else if (_currentScreen.value != AppScreen.HOME) {
            _currentScreen.value = AppScreen.HOME
            return true
        }
        return false
    }

    fun toggleHabitToday(habitId: Long) {
        viewModelScope.launch {
            val wasAdded = repository.toggleCompletion(habitId, DateUtils.getTodayString())
            if (wasAdded) {
                val quotes = listOf(
                    "Streak extended! You're on fire! 🔥",
                    "Consistency is key! Keep it burning! ⚡",
                    "Awesome job! Another day completed! 🎯",
                    "Habit conquered! Keep the momentum! 🚀",
                    "Small wins compound into greatness! ✨"
                )
                val current = _settings.value
                _settings.value = current.copy(
                    celebrationTrigger = current.celebrationTrigger + 1,
                    celebrationMessage = quotes.random()
                )
            }
        }
    }

    fun toggleDateCompletion(habitId: Long, dateString: String) {
        viewModelScope.launch {
            repository.toggleCompletion(habitId, dateString)
        }
    }

    fun saveHabit(
        name: String,
        iconName: String,
        colorHex: String,
        frequencyType: String,
        targetDaysOfWeek: String,
        reminderEnabled: Boolean,
        reminderTime: String
    ) {
        viewModelScope.launch {
            val habit = Habit(
                id = _editingHabitId.value ?: 0L,
                name = name.trim(),
                iconName = iconName,
                colorHex = colorHex,
                frequencyType = frequencyType,
                targetDaysOfWeek = targetDaysOfWeek,
                reminderEnabled = reminderEnabled,
                reminderTime = reminderTime
            )
            repository.saveHabit(habit)
            navigateBack()
        }
    }

    fun deleteHabit(habitId: Long) {
        viewModelScope.launch {
            repository.deleteHabit(habitId)
            if (_selectedHabitId.value == habitId) {
                _selectedHabitId.value = null
            }
            if (_currentScreen.value == AppScreen.DETAIL || _currentScreen.value == AppScreen.ADD_EDIT) {
                _currentScreen.value = AppScreen.HOME
            }
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.resetAllData()
            _currentScreen.value = AppScreen.HOME
        }
    }

    // Settings actions
    fun setNotificationsEnabled(enabled: Boolean) {
        _settings.value = _settings.value.copy(notificationsEnabled = enabled)
    }

    fun setWeekStartDay(day: String) {
        _settings.value = _settings.value.copy(weekStartDay = day)
    }

    fun setThemeMode(mode: String) {
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    fun toggleMotivationalQuotes() {
        val current = _settings.value
        _settings.value = current.copy(motivationalQuotes = !current.motivationalQuotes)
    }
}
