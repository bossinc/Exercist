package com.bossinc.exercist.exercise

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bossinc.exercist.data.model.MuscleGroups

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    onBack: () -> Unit,
    viewModel: ExerciseViewModel = hiltViewModel()
) {
    val exercises by viewModel.exercises.collectAsState()
    val exerciseDeleted by viewModel.exerciseDeleted.collectAsState()
    val exercise = exercises.firstOrNull { it.id == exerciseId }

    var isEditing by remember { mutableStateOf(false) }
    var name by remember(exercise) { mutableStateOf(exercise?.name ?: "") }
    var muscleGroup by remember(exercise) { mutableStateOf(exercise?.muscleGroup ?: MuscleGroups.all.first()) }
    var equipment by remember(exercise) { mutableStateOf(exercise?.equipment ?: "") }
    var description by remember(exercise) { mutableStateOf(exercise?.description ?: "") }
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(exerciseDeleted) {
        if (exerciseDeleted) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Exercise" else (exercise?.name ?: "Exercise")) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                        IconButton(onClick = {
                            exercise?.let { viewModel.updateExercise(it.copy(name = name, muscleGroup = muscleGroup, equipment = equipment, description = description)) }
                            isEditing = false
                        }, enabled = name.isNotBlank()) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                }
            )
        }
    ) { padding ->
        exercise?.let {
            if (isEditing) {
                Column(
                    Modifier.padding(padding).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = muscleGroup,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Muscle Group") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            MuscleGroups.all.forEach { group ->
                                DropdownMenuItem(text = { Text(group) }, onClick = { muscleGroup = group; expanded = false })
                            }
                        }
                    }
                    OutlinedTextField(
                        value = equipment,
                        onValueChange = { equipment = it },
                        label = { Text("Equipment") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            } else {
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
            }
        } ?: Box(Modifier.padding(padding).fillMaxSize()) { Text("Exercise not found") }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Exercise?") },
            text = { Text("This exercise will be permanently deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteExercise(exerciseId)
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
