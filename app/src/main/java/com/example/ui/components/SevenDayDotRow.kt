package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OrangePrimary
import com.example.ui.theme.SuccessGreen
import com.example.util.DateUtils

@Composable
fun SevenDayDotRow(
    recent7DaysStatus: List<Boolean>,
    modifier: Modifier = Modifier,
    habitColor: Color = SuccessGreen
) {
    val recentDays = DateUtils.getLastNDays(7)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        recentDays.forEachIndexed { index, dayInfo ->
            val isDone = if (index < recent7DaysStatus.size) recent7DaysStatus[index] else false

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = dayInfo.dayLetter,
                    fontSize = 11.sp,
                    fontWeight = if (dayInfo.isToday) FontWeight.Bold else FontWeight.Medium,
                    color = if (dayInfo.isToday) OrangePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                val dotSize = 22.dp
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .then(
                            when {
                                isDone -> Modifier.background(SuccessGreen)
                                dayInfo.isToday -> Modifier
                                    .background(Color(0xFFFFEDD5))
                                    .border(1.5.dp, OrangePrimary, CircleShape)
                                else -> Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    } else if (dayInfo.isToday) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(OrangePrimary)
                        )
                    }
                }
            }
        }
    }
}
