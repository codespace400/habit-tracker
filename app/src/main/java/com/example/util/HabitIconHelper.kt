package com.example.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class HabitIconOption(
    val id: String,
    val label: String,
    val icon: ImageVector
)

object HabitIconHelper {
    val options = listOf(
        HabitIconOption("Flame", "Flame", Icons.Default.LocalFireDepartment),
        HabitIconOption("Run", "Exercise", Icons.Default.DirectionsRun),
        HabitIconOption("Water", "Hydration", Icons.Default.WaterDrop),
        HabitIconOption("Book", "Reading", Icons.Default.MenuBook),
        HabitIconOption("Meditation", "Mindfulness", Icons.Default.SelfImprovement),
        HabitIconOption("Fitness", "Workout", Icons.Default.FitnessCenter),
        HabitIconOption("Bedtime", "Sleep", Icons.Default.Bedtime),
        HabitIconOption("Code", "Coding", Icons.Default.Code),
        HabitIconOption("Brush", "Creative", Icons.Default.Brush),
        HabitIconOption("Heart", "Health", Icons.Default.Favorite),
        HabitIconOption("Music", "Music", Icons.Default.MusicNote),
        HabitIconOption("Food", "Nutrition", Icons.Default.Restaurant)
    )

    fun getIcon(id: String): ImageVector {
        return options.firstOrNull { it.id.equals(id, ignoreCase = true) }?.icon
            ?: Icons.Default.LocalFireDepartment
    }

    val colorOptions = listOf(
        "#F97316", // Energetic Orange (Brand primary)
        "#22C55E", // Success Green (Brand)
        "#0EA5E9", // Sky Blue
        "#8B5CF6", // Purple/Violet
        "#EC4899", // Pink
        "#EAB308", // Sun Amber
        "#14B8A6", // Teal
        "#EF4444"  // Rose Red
    )

    fun parseColor(hex: String, fallback: Color = Color(0xFFF97316)): Color {
        return try {
            val cleanHex = if (hex.startsWith("#")) hex.substring(1) else hex
            Color(android.graphics.Color.parseColor("#$cleanHex"))
        } catch (e: Exception) {
            fallback
        }
    }
}
