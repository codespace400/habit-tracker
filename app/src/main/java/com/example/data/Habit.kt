package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String = "Flame", // Flame, Water, Run, Book, Meditation, Fitness, Bedtime, Code, Brush, Heart
    val colorHex: String = "#F97316", // Default Energetic Orange
    val frequencyType: String = "DAILY", // DAILY or WEEKDAYS
    val targetDaysOfWeek: String = "1,2,3,4,5,6,7", // 1=Mon .. 7=Sun
    val reminderEnabled: Boolean = false,
    val reminderTime: String = "08:00 AM",
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
) {
    fun isScheduledForDay(dayOfWeek: Int): Boolean {
        if (frequencyType == "DAILY") return true
        val days = targetDaysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
        return days.contains(dayOfWeek)
    }
}
