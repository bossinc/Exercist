package com.bossinc.exercist.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bossinc.exercist.exercise.ExerciseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onSessionClick: (String) -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
    exerciseViewModel: ExerciseViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsState()
    val exercises by exerciseViewModel.exercises.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault()) }

    Scaffold(topBar = { TopAppBar(title = { Text("History") }) }, contentWindowInsets = WindowInsets(0)) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(sessions) { session ->
                Card(modifier = Modifier.fillMaxWidth().clickable { onSessionClick(session.id) }) {
                    Column(Modifier.padding(16.dp)) {
                        val displayDate = session.startedAt ?: session.date
                        Text(displayDate?.let { dateFormat.format(it) } ?: "Workout", style = MaterialTheme.typography.titleMedium)
                        val muscleGroups = session.exercises
                            .map { entry -> exercises.find { it.id == entry.exerciseId }?.muscleGroup ?: entry.muscleGroup }
                            .filter { it.isNotBlank() }
                            .groupBy { it }
                            .map { (group, entries) -> "$group (${entries.size})" }
                            .joinToString(" · ")
                        Text(
                            if (muscleGroups.isNotBlank()) muscleGroups else "${session.exercises.size} exercises",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text("${session.durationMinutes} min", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
