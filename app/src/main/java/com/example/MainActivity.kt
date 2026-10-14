package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.HabitViewModel
import com.example.ui.screens.AddEditHabitScreen
import com.example.ui.screens.HabitDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.StreaksTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: HabitViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val isDark = when (uiState.themeMode) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemInDarkTheme()
            }

            StreaksTheme(darkTheme = isDark) {
                MainContent(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun MainContent(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val showBottomBar = uiState.currentScreen in listOf(
        AppScreen.HOME,
        AppScreen.STATS,
        AppScreen.SETTINGS
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = uiState.currentScreen == AppScreen.HOME,
                        onClick = { viewModel.navigateTo(AppScreen.HOME) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Today",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                "Today",
                                fontSize = 12.sp,
                                fontWeight = if (uiState.currentScreen == AppScreen.HOME) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OrangePrimary,
                            selectedTextColor = OrangePrimary,
                            indicatorColor = Color(0xFFFFEDD5)
                        ),
                        modifier = Modifier.testTag("nav_today")
                    )

                    NavigationBarItem(
                        selected = uiState.currentScreen == AppScreen.STATS,
                        onClick = { viewModel.navigateTo(AppScreen.STATS) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Stats",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                "Stats",
                                fontSize = 12.sp,
                                fontWeight = if (uiState.currentScreen == AppScreen.STATS) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OrangePrimary,
                            selectedTextColor = OrangePrimary,
                            indicatorColor = Color(0xFFFFEDD5)
                        ),
                        modifier = Modifier.testTag("nav_stats")
                    )

                    NavigationBarItem(
                        selected = uiState.currentScreen == AppScreen.SETTINGS,
                        onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                "Settings",
                                fontSize = 12.sp,
                                fontWeight = if (uiState.currentScreen == AppScreen.SETTINGS) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OrangePrimary,
                            selectedTextColor = OrangePrimary,
                            indicatorColor = Color(0xFFFFEDD5)
                        ),
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = uiState.currentScreen,
                label = "screen_crossfade"
            ) { screen ->
                when (screen) {
                    AppScreen.HOME -> {
                        HomeScreen(
                            uiState = uiState,
                            onHabitClick = { id -> viewModel.openHabitDetail(id) },
                            onToggleCheck = { id -> viewModel.toggleHabitToday(id) },
                            onAddHabitClick = { viewModel.openAddHabit() }
                        )
                    }

                    AppScreen.STATS -> {
                        StatsScreen(
                            uiState = uiState,
                            onHabitClick = { id -> viewModel.openHabitDetail(id) }
                        )
                    }

                    AppScreen.SETTINGS -> {
                        SettingsScreen(
                            uiState = uiState,
                            onNotificationsChange = { viewModel.setNotificationsEnabled(it) },
                            onWeekStartDayChange = { viewModel.setWeekStartDay(it) },
                            onThemeModeChange = { viewModel.setThemeMode(it) },
                            onToggleQuotes = { viewModel.toggleMotivationalQuotes() },
                            onResetData = { viewModel.resetData() }
                        )
                    }

                    AppScreen.DETAIL -> {
                        val selectedHabit = uiState.habits.firstOrNull { it.habit.id == uiState.selectedHabitId }
                        HabitDetailScreen(
                            habitWithStats = selectedHabit,
                            onBackClick = { viewModel.navigateBack() },
                            onEditClick = { id -> viewModel.openEditHabit(id) },
                            onDeleteClick = { id -> viewModel.deleteHabit(id) },
                            onToggleToday = { id -> viewModel.toggleHabitToday(id) },
                            onToggleDate = { id, dateStr -> viewModel.toggleDateCompletion(id, dateStr) }
                        )
                    }

                    AppScreen.ADD_EDIT -> {
                        val editingHabit = uiState.habits.firstOrNull { it.habit.id == uiState.editingHabitId }?.habit
                        AddEditHabitScreen(
                            editingHabit = editingHabit,
                            onSaveHabit = { name, icon, color, freq, days, reminder, time ->
                                viewModel.saveHabit(name, icon, color, freq, days, reminder, time)
                            },
                            onBackClick = { viewModel.navigateBack() }
                        )
                    }
                }
            }
        }
    }
}
