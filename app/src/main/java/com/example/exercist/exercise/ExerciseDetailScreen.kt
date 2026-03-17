package com.example.exercist.exercise

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.exercist.data.model.Exercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    onBack: () -> Unit,
    viewModel: ExerciseViewModel = hiltViewModel()
) {
    val exercises by viewModel.exercises.collectAsState()
    val exercise = exercises.firstOrNull { it.id == exerciseId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise?.name ?: "Exercise") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        exercise?.let {
            Column(Modifier.padding(padding).padding(16.dp)) {
                Text(it.name, style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(8.dp))
                Text("Muscle Group: ${it.muscleGroup}")
                Text("Equipment: ${it.equipment}")
                if (it.description.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(it.description)
                }
            }
        } ?: Box(Modifier.padding(padding).fillMaxSize()) { Text("Exercise not found") }
    }
}
