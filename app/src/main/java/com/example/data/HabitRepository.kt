package com.example.data

import com.example.util.DateUtils
import com.example.util.DayInfo
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

data class HabitWithStats(
    val habit: Habit,
    val isCompletedToday: Boolean,
    val currentStreak: Int,
    val longestStreak: Int,
    val completionRatePercent: Int,
    val totalCompletions: Int,
    val recent7DaysStatus: List<Boolean>, // for the 7-day dot row
    val completedDates: Set<String>
)

object StreakCalculator {
    fun calculateStats(
        habit: Habit,
        logs: List<HabitLog>,
        recentDays: List<DayInfo>,
        todayStr: String = DateUtils.getTodayString()
    ): HabitWithStats {
        val completedDates = logs.map { it.dateString }.toSet()
        val isCompletedToday = completedDates.contains(todayStr)

        // 7-day dot row status
        val recent7Status = recentDays.map { day ->
            completedDates.contains(day.dateString)
        }

        // Current Streak calculation
        // Check consecutive scheduled days backwards from today (or yesterday if today isn't done yet)
        var streak = 0
        val cal = Calendar.getInstance()

        // If today is scheduled and completed, streak includes today
        // If today is scheduled but not completed, check if yesterday was completed
        val todayDow = getStandardDow(cal)
        val todayScheduled = habit.isScheduledForDay(todayDow)

        if (todayScheduled && isCompletedToday) {
            streak++
            cal.add(Calendar.DAY_OF_YEAR, -1)
        } else if (!todayScheduled) {
            // Today isn't scheduled, start looking from yesterday without penalty
            cal.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            // Today is scheduled but not completed. Streak can still be alive from yesterday!
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }

        // Now walk backwards
        var daysBackChecked = 0
        while (daysBackChecked < 365) {
            val dStr = DateUtils.formatDate(cal.time)
            val dow = getStandardDow(cal)
            val isScheduled = habit.isScheduledForDay(dow)

            if (isScheduled) {
                if (completedDates.contains(dStr)) {
                    streak++
                } else {
                    // Missed a scheduled day; streak ends here
                    break
                }
            }
            cal.add(Calendar.DAY_OF_YEAR, -1)
            daysBackChecked++
        }

        // Longest Streak calculation (over all completed dates or past 120 days)
        var longestStreak = streak
        var tempStreak = 0
        val walkCal = Calendar.getInstance()
        walkCal.add(Calendar.DAY_OF_YEAR, -120) // look back 120 days
        for (i in 0..120) {
            val dStr = DateUtils.formatDate(walkCal.time)
            val dow = getStandardDow(walkCal)
            if (habit.isScheduledForDay(dow)) {
                if (completedDates.contains(dStr)) {
                    tempStreak++
                    if (tempStreak > longestStreak) {
                        longestStreak = tempStreak
                    }
                } else {
                    tempStreak = 0
                }
            }
            walkCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Completion Rate calculation:
        // Calculated over the last 30 scheduled days
        val past30Cal = Calendar.getInstance()
        past30Cal.add(Calendar.DAY_OF_YEAR, -30)
        var totalScheduledDays = 0
        var totalCompletedIn30 = 0
        for (i in 0..30) {
            val dStr = DateUtils.formatDate(past30Cal.time)
            val dow = getStandardDow(past30Cal)
            if (habit.isScheduledForDay(dow)) {
                totalScheduledDays++
                if (completedDates.contains(dStr)) {
                    totalCompletedIn30++
                }
            }
            past30Cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        val rate = if (totalScheduledDays > 0) {
            ((totalCompletedIn30.toFloat() / totalScheduledDays.toFloat()) * 100).toInt().coerceIn(0, 100)
        } else 100

        return HabitWithStats(
            habit = habit,
            isCompletedToday = isCompletedToday,
            currentStreak = streak,
            longestStreak = longestStreak.coerceAtLeast(streak),
            completionRatePercent = rate,
            totalCompletions = logs.size,
            recent7DaysStatus = recent7Status,
            completedDates = completedDates
        )
    }

    private fun getStandardDow(cal: Calendar): Int {
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }
}

class HabitRepository(private val habitDao: HabitDao) {
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()
    val allLogs: Flow<List<HabitLog>> = habitDao.getAllLogs()

    suspend fun getHabitById(id: Long): Habit? = habitDao.getHabitById(id)

    fun getLogsForHabit(habitId: Long): Flow<List<HabitLog>> = habitDao.getLogsForHabit(habitId)

    suspend fun saveHabit(habit: Habit): Long {
        return if (habit.id == 0L) {
            habitDao.insertHabit(habit)
        } else {
            habitDao.updateHabit(habit)
            habit.id
        }
    }

    suspend fun deleteHabit(habitId: Long) {
        habitDao.deleteAllLogsForHabit(habitId)
        habitDao.deleteHabit(habitId)
    }

    suspend fun toggleCompletion(habitId: Long, dateString: String = DateUtils.getTodayString()): Boolean {
        val existingLogs = habitDao.getLogsListForHabit(habitId)
        val isAlreadyDone = existingLogs.any { it.dateString == dateString }
        return if (isAlreadyDone) {
            habitDao.deleteLog(habitId, dateString)
            false
        } else {
            habitDao.insertLog(HabitLog(habitId = habitId, dateString = dateString))
            true
        }
    }

    suspend fun setCompletion(habitId: Long, dateString: String, completed: Boolean) {
        if (completed) {
            habitDao.insertLog(HabitLog(habitId = habitId, dateString = dateString))
        } else {
            habitDao.deleteLog(habitId, dateString)
        }
    }

    suspend fun seedDefaultDataIfEmpty() {
        val count = habitDao.getHabitCount()
        if (count == 0) {
            // Seed motivating habits with initial completions
            val h1 = Habit(
                name = "Morning 20m Run",
                iconName = "Run",
                colorHex = "#F97316", // Energetic Orange
                frequencyType = "DAILY",
                reminderEnabled = true,
                reminderTime = "07:00 AM"
            )
            val h2 = Habit(
                name = "Drink 2.5L Water",
                iconName = "Water",
                colorHex = "#0EA5E9", // Ocean Blue
                frequencyType = "DAILY",
                reminderEnabled = true,
                reminderTime = "09:00 AM"
            )
            val h3 = Habit(
                name = "Read 15 Pages",
                iconName = "Book",
                colorHex = "#8B5CF6", // Violet
                frequencyType = "DAILY",
                reminderEnabled = true,
                reminderTime = "09:30 PM"
            )
            val h4 = Habit(
                name = "Mindful Meditation",
                iconName = "Meditation",
                colorHex = "#22C55E", // Success Green
                frequencyType = "WEEKDAYS",
                targetDaysOfWeek = "1,2,3,4,5",
                reminderEnabled = false,
                reminderTime = "08:00 AM"
            )

            val id1 = habitDao.insertHabit(h1)
            val id2 = habitDao.insertHabit(h2)
            val id3 = habitDao.insertHabit(h3)
            val id4 = habitDao.insertHabit(h4)

            // Seed logs for realistic streaks
            val cal = Calendar.getInstance()
            val logs = mutableListOf<HabitLog>()

            // h1: Run has 6 days streak (yesterday and 5 days prior, today uncompleted so user can tap!)
            for (offset in 1..6) {
                val c = Calendar.getInstance()
                c.add(Calendar.DAY_OF_YEAR, -offset)
                logs.add(HabitLog(habitId = id1, dateString = DateUtils.formatDate(c.time)))
            }
            // Add some scattered past dates for h1 in past month
            for (offset in listOf(8, 9, 10, 12, 14, 15, 18, 20, 21, 22)) {
                val c = Calendar.getInstance()
                c.add(Calendar.DAY_OF_YEAR, -offset)
                logs.add(HabitLog(habitId = id1, dateString = DateUtils.formatDate(c.time)))
            }

            // h2: Drink Water completed today + 9 days prior (10 day streak!)
            for (offset in 0..9) {
                val c = Calendar.getInstance()
                c.add(Calendar.DAY_OF_YEAR, -offset)
                logs.add(HabitLog(habitId = id2, dateString = DateUtils.formatDate(c.time)))
            }
            for (offset in listOf(12, 13, 15, 16, 17, 19, 23, 24, 25)) {
                val c = Calendar.getInstance()
                c.add(Calendar.DAY_OF_YEAR, -offset)
                logs.add(HabitLog(habitId = id2, dateString = DateUtils.formatDate(c.time)))
            }

            // h3: Read completed today + 4 days prior (5 day streak)
            for (offset in 0..4) {
                val c = Calendar.getInstance()
                c.add(Calendar.DAY_OF_YEAR, -offset)
                logs.add(HabitLog(habitId = id3, dateString = DateUtils.formatDate(c.time)))
            }

            // h4: Meditation completed 3 days
            for (offset in 1..3) {
                val c = Calendar.getInstance()
                c.add(Calendar.DAY_OF_YEAR, -offset)
                logs.add(HabitLog(habitId = id4, dateString = DateUtils.formatDate(c.time)))
            }

            habitDao.insertLogs(logs)
        }
    }

    suspend fun resetAllData() {
        habitDao.clearAllLogs()
        habitDao.clearAllHabits()
        seedDefaultDataIfEmpty()
    }
}
