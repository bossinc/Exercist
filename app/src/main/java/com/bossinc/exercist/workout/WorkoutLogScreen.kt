package com.bossinc.exercist.workout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.activity.ComponentActivity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.bossinc.exercist.data.model.Exercise
import com.bossinc.exercist.data.model.MuscleGroups
import com.bossinc.exercist.exercise.ExerciseViewModel
import com.bossinc.exercist.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutLogScreen(
    navController: NavController,
    viewModel: WorkoutViewModel = hiltViewModel(LocalContext.current as ComponentActivity),
    exerciseViewModel: ExerciseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val exercises by exerciseViewModel.exercises.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var showExercisePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.phase == WorkoutPhase.PLANNING) "Plan Workout" else "Workout")
                },
                actions = {
                    IconButton(onClick = { showExercisePicker = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Exercise")
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.phase == WorkoutPhase.PLANNING) {
                // Planning phase: show exercise list without set inputs
                if (uiState.exercises.isEmpty()) {
                    item {
                        Text(
                            "Add exercises to your workout plan, then press Start.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                itemsIndexed(uiState.exercises) { index, entry ->
                    val exercise = exercises.find { it.id == entry.exerciseId }
                    val displayName = exercise?.name ?: entry.exerciseName
                    ExerciseSwipeToDismiss(onDismiss = { viewModel.removeExercise(index) }) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(displayName, style = MaterialTheme.typography.titleMedium)
                                val subtitle = listOfNotNull(
                                    exercise?.muscleGroup?.takeIf { it.isNotBlank() },
                                    exercise?.equipment?.takeIf { it.isNotBlank() }
                                ).joinToString(" · ")
                                if (subtitle.isNotBlank()) {
                                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (!exercise?.description.isNullOrBlank()) {
                                    Text(exercise!!.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                item {
                    Button(
                        onClick = viewModel::startWorkout,
                        enabled = uiState.exercises.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Start Workout")
                    }
                }
            } else {
                // Active phase: show set inputs
                itemsIndexed(uiState.exercises) { exerciseIndex, entry ->
                    val exercise = exercises.find { it.id == entry.exerciseId }
                    val displayName = exercise?.name ?: entry.exerciseName
                    var showInfoDialog by remember { mutableStateOf(false) }
                    ExerciseSwipeToDismiss(onDismiss = { viewModel.removeExercise(exerciseIndex) }) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                displayName,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.clickable(
                                    enabled = exercise != null && (!exercise.equipment.isNullOrBlank() || !exercise.description.isNullOrBlank())
                                ) { showInfoDialog = true }
                            )
                            if (showInfoDialog && exercise != null) {
                                AlertDialog(
                                    onDismissRequest = { showInfoDialog = false },
                                    title = { Text(exercise.name) },
                                    text = {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            if (exercise.equipment.isNotBlank()) {
                                                Text("Equipment: ${exercise.equipment}")
                                            }
                                            if (exercise.description.isNotBlank()) {
                                                Spacer(Modifier.height(4.dp))
                                                Text(exercise.description)
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = { showInfoDialog = false }) { Text("Close") }
                                    }
                                )
                            }
                            val prevSets = uiState.previousSets[entry.exerciseId]
                            val prevNotes = uiState.previousNotes[entry.exerciseId]
                            if (!prevSets.isNullOrEmpty()) {
                                Spacer(Modifier.height(4.dp))
                                Text("Last time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                prevSets.forEach { prev ->
                                    Text(
                                        "${prev.weight}lbs × ${prev.reps}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (!prevNotes.isNullOrBlank()) {
                                    Text(
                                        "Note: $prevNotes",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            } else {
                                Spacer(Modifier.height(8.dp))
                            }
                            entry.sets.forEachIndexed { setIndex, set ->
                                ActiveSetRow(
                                    setNumber = set.setNumber,
                                    initialReps = set.reps,
                                    initialWeight = set.weight,
                                    onValuesChange = { reps, weight ->
                                        viewModel.updateSetValues(exerciseIndex, setIndex, reps, weight)
                                    }
                                )
                            }
                            OutlinedTextField(
                                value = entry.notes,
                                onValueChange = { viewModel.updateExerciseNotes(exerciseIndex, it) },
                                placeholder = { Text("Notes") },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                singleLine = false,
                                minLines = 1
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                if (entry.sets.size > 1) {
                                    TextButton(onClick = { viewModel.removeLastSet(exerciseIndex) }) {
                                        Text("- Remove Set", color = MaterialTheme.colorScheme.error)
                                    }
                                } else {
                                    Spacer(Modifier.weight(1f))
                                }
                                TextButton(onClick = { keyboardController?.hide(); focusManager.clearFocus(); viewModel.addSet(exerciseIndex) }) {
                                    Text("+ Add Set")
                                }
                            }
                        }
                    }
                    } // ExerciseSwipeToDismiss
                }
                item {
                    Button(
                        onClick = {
                            viewModel.finishWorkout()
                            navController.navigate(Routes.History.route) {
                                popUpTo(Routes.Workout.route) { inclusive = true }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Finish Workout")
                    }
                }
            }
        }
    }

    if (showExercisePicker) {
        ExercisePickerSheet(
            onExerciseSelected = { id, name, muscleGroup ->
                viewModel.addExercise(id, name, muscleGroup)
                showExercisePicker = false
            },
            onDismiss = { showExercisePicker = false },
            exerciseViewModel = exerciseViewModel
        )
    }
}

@Composable
private fun ExerciseSwipeToDismiss(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var isLongPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { isLongPressed = true },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                    },
                    onDragEnd = {
                        if (offsetX < -(size.width * 0.4f)) onDismiss()
                        offsetX = 0f
                        isLongPressed = false
                    },
                    onDragCancel = {
                        offsetX = 0f
                        isLongPressed = false
                    }
                )
            }
    ) {
        if (isLongPressed) {
            Box(
                modifier = Modifier.matchParentSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        }
        Box(modifier = Modifier.offset { IntOffset(offsetX.roundToInt().coerceAtMost(0), 0) }) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExercisePickerSheet(
    onExerciseSelected: (id: String, name: String, muscleGroup: String) -> Unit,
    onDismiss: () -> Unit,
    exerciseViewModel: ExerciseViewModel
) {
    val exercises by exerciseViewModel.exercises.collectAsState()
    val searchQuery by exerciseViewModel.searchQuery.collectAsState()
    val selectedGroup by exerciseViewModel.selectedMuscleGroup.collectAsState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxHeight(0.85f)) {
            Text(
                "Add Exercise",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            OutlinedTextField(
                value = searchQuery,
                onValueChange = exerciseViewModel::setSearchQuery,
                placeholder = { Text("Search exercises...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedGroup == null,
                        onClick = { exerciseViewModel.setMuscleGroup(null) },
                        label = { Text("All") }
                    )
                }
                items(MuscleGroups.all) { group ->
                    FilterChip(
                        selected = selectedGroup == group,
                        onClick = { exerciseViewModel.setMuscleGroup(if (selectedGroup == group) null else group) },
                        label = { Text(group) }
                    )
                }
            }
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(exercises) { exercise ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onExerciseSelected(exercise.id, exercise.name, exercise.muscleGroup) }
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(exercise.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${exercise.muscleGroup} · ${exercise.equipment}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
