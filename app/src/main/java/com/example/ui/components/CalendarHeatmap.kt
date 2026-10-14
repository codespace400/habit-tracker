package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.SuccessGreen
import com.example.util.DateUtils
import java.util.Calendar

@Composable
fun CalendarHeatmap(
    completedDates: Set<String>,
    onToggleDate: (String) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = SuccessGreen
) {
    var monthOffset by remember { mutableIntStateOf(0) }

    val displayCalendar = remember(monthOffset) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, monthOffset)
        cal
    }

    val monthYearTitle = remember(monthOffset) {
        DateUtils.getMonthDisplayString(displayCalendar)
    }

    val monthDays = remember(monthOffset) {
        val y = displayCalendar.get(Calendar.YEAR)
        val m = displayCalendar.get(Calendar.MONTH)
        DateUtils.getCalendarGridForMonth(y, m)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Month Switcher Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { monthOffset-- },
                    modifier = Modifier.size(36.dp).testTag("heatmap_prev_month")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Month",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = monthYearTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = { if (monthOffset < 0) monthOffset++ },
                    enabled = monthOffset < 0,
                    modifier = Modifier.size(36.dp).testTag("heatmap_next_month")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Month",
                        tint = if (monthOffset < 0) MaterialTheme.colorScheme.onSurface else Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day of Week Header row
            val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEach { dow ->
                    Text(
                        text = dow,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Heatmap Grid: rows of 7 days
            val rows = monthDays.chunked(7)
            rows.forEach { week ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    week.forEach { dayInfo ->
                        val isDone = completedDates.contains(dayInfo.dateString)
                        val isSelectable = !dayInfo.isFuture && dayInfo.isCurrentMonth

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 2.5.dp)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .then(
                                    when {
                                        !dayInfo.isCurrentMonth -> Modifier.background(Color.Transparent)
                                        isDone -> Modifier.background(activeColor)
                                        dayInfo.isToday -> Modifier
                                            .background(Color(0xFFFFEDD5))
                                            .border(1.5.dp, OrangePrimary, RoundedCornerShape(6.dp))
                                        dayInfo.isFuture -> Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        else -> Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                                    }
                                )
                                .then(
                                    if (isSelectable) {
                                        Modifier.clickable { onToggleDate(dayInfo.dateString) }
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayInfo.isCurrentMonth) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Done",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${dayInfo.dayOfMonth}",
                                        fontSize = 11.sp,
                                        fontWeight = if (dayInfo.isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = if (dayInfo.isToday) OrangePrimary
                                        else if (dayInfo.isFuture) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // GitHub-style Legend & tap instruction
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tap any day to toggle completion",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Missed",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(activeColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Done",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = activeColor
                    )
                }
            }
        }
    }
}
