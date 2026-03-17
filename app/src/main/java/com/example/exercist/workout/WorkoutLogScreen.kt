package com.example.exercist.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.exercist.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutLogScreen(
    navController: NavController,
    viewModel: WorkoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showExercisePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) navController.navigate(Routes.History.route) {
            popUpTo(Routes.Workout.route) { inclusive = true }
        }
    }

    if (uiState.isTimerRunning) {
        RestTimerDialog(
            secondsRemaining = uiState.restTimerSeconds,
            onDismiss = viewModel::stopTimer
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.sessionName) },
                actions = {
                    IconButton(onClick = viewModel::finishWorkout) {
                        Icon(Icons.Default.Done, contentDescription = "Finish Workout")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showExercisePicker = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Exercise")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(uiState.exercises) { exerciseIndex, entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(entry.exerciseName, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        entry.sets.forEachIndexed { setIndex, set ->
                            ActiveSetRow(
                                setNumber = set.setNumber,
                                initialReps = set.reps,
                                initialWeight = set.weight,
                                isCompleted = set.isCompleted,
                                onComplete = { reps, weight ->
                                    viewModel.updateSet(exerciseIndex, setIndex, reps, weight)
                                    viewModel.startRestTimer()
                                }
                            )
                        }
                        TextButton(onClick = { viewModel.addSet(exerciseIndex) }) {
                            Text("+ Add Set")
                        }
                    }
                }
            }
        }
    }

    if (showExercisePicker) {
        AlertDialog(
            onDismissRequest = { showExercisePicker = false },
            title = { Text("Add Exercise") },
            text = {
                Column {
                    listOf("Bench Press" to "bench_press", "Squat" to "squat", "Deadlift" to "deadlift").forEach { (name, id) ->
                        TextButton(onClick = {
                            viewModel.addExercise(id, name)
                            showExercisePicker = false
                        }) { Text(name) }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExercisePicker = false }) { Text("Cancel") }
            }
        )
    }
}
