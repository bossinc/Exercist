package com.bossinc.exercist.exercise

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bossinc.exercist.data.model.MuscleGroups

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(
    onExerciseClick: (String) -> Unit,
    onCreateExercise: () -> Unit,
    viewModel: ExerciseViewModel = hiltViewModel()
) {
    val exercises by viewModel.exercises.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedGroup by viewModel.selectedMuscleGroup.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exercises") },
                actions = {
                    IconButton(onClick = onCreateExercise) {
                        Icon(Icons.Default.Add, contentDescription = "Add Exercise")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::setSearchQuery,
                placeholder = { Text("Search exercises...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(selected = selectedGroup == null, onClick = { viewModel.setMuscleGroup(null) }, label = { Text("All") })
                }
                items(MuscleGroups.all) { group ->
                    FilterChip(selected = selectedGroup == group, onClick = { viewModel.setMuscleGroup(if (selectedGroup == group) null else group) }, label = { Text(group) })
                }
            }
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(exercises) { exercise ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onExerciseClick(exercise.id) }) {
                        Column(Modifier.padding(16.dp)) {
                            Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                            Text("${exercise.muscleGroup} · ${exercise.equipment}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
