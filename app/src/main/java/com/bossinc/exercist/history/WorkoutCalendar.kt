package com.bossinc.exercist.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bossinc.exercist.data.model.WorkoutSession
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WorkoutCalendar(
    sessions: List<WorkoutSession>,
    modifier: Modifier = Modifier
) {
    val calendar = remember { Calendar.getInstance() }
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val monthName = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time) }

    val workoutDays = remember(sessions) {
        sessions.mapNotNull { session ->
            session.date?.let { date ->
                val cal = Calendar.getInstance().apply { time = date }
                if (cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month) {
                    cal.get(Calendar.DAY_OF_MONTH)
                } else null
            }
        }.toSet()
    }

    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY)

    Column(modifier = modifier) {
        Text(monthName, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Text(day, style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
        val cells = firstDayOfWeek + daysInMonth
        val rows = (cells + 6) / 7
        var dayCounter = 1
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    if (cellIndex < firstDayOfWeek || dayCounter > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val day = dayCounter
                        dayCounter++
                        val hasWorkout = day in workoutDays
                        Box(
                            modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (hasWorkout) {
                                Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                            }
                            Text(
                                day.toString(),
                                color = if (hasWorkout) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
