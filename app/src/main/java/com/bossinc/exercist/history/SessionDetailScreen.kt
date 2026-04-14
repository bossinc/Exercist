package com.bossinc.exercist.history

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bossinc.exercist.data.model.ExerciseEntry
import com.bossinc.exercist.data.model.ExerciseSet
import com.bossinc.exercist.workout.WorkoutViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: String,
    onBack: () -> Unit,
    onContinueWorkout: () -> Unit,
    onCopyWorkout: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: SessionDetailViewModel = hiltViewModel(),
    workoutViewModel: WorkoutViewModel = hiltViewModel(LocalContext.current as ComponentActivity)
) {
    val session by viewModel.selectedSession.collectAsState()
    val allExercises by viewModel.exercises.collectAsState()
    val sessionDeleted by viewModel.sessionDeleted.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault()) }
    var isEditing by remember { mutableStateOf(false) }
    var editableExercises by remember { mutableStateOf<List<ExerciseEntry>>(emptyList()) }
    var showResumeConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(sessionId) { viewModel.loadSession(sessionId) }

    LaunchedEffect(sessionDeleted) {
        if (sessionDeleted) onNavigateToHistory()
    }

    LaunchedEffect(session) {
        session?.let { editableExercises = it.exercises }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val displayDate = session?.startedAt ?: session?.date
                    Text(displayDate?.let { dateFormat.format(it) } ?: "Workout")
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                        IconButton(onClick = {
                            session?.let { viewModel.updateSession(it.copy(exercises = editableExercises)) }
                            isEditing = false
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Save")
                        }
                    } else {
                        IconButton(onClick = { isEditing = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->
        session?.let { s ->
            LazyColumn(
                Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    s.startedAt?.let {
                        Text(
                            "Started: ${dateFormat.format(it)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    s.finishedAt?.let {
                        Text(
                            "Finished: ${dateFormat.format(it)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (s.startedAt == null) s.date?.let {
                        Text(
                            dateFormat.format(it),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text("Duration: ${s.durationMinutes} minutes")
                }
                itemsIndexed(editableExercises) { exerciseIndex, entry ->
                    val displayName = allExercises.find { it.id == entry.exerciseId }?.name ?: entry.exerciseName
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(displayName, style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(4.dp))
                            entry.sets.forEachIndexed { setIndex, set ->
                                if (isEditing) {
                                    EditableSetRow(
                                        set = set,
                                        onWeightChange = { newWeight ->
                                            editableExercises = editableExercises.toMutableList()
                                                .also { exercises ->
                                                    val sets =
                                                        exercises[exerciseIndex].sets.toMutableList()
                                                    sets[setIndex] =
                                                        sets[setIndex].copy(weight = newWeight)
                                                    exercises[exerciseIndex] =
                                                        exercises[exerciseIndex].copy(sets = sets)
                                                }
                                        },
                                        onRepsChange = { newReps ->
                                            editableExercises = editableExercises.toMutableList()
                                                .also { exercises ->
                                                    val sets =
                                                        exercises[exerciseIndex].sets.toMutableList()
                                                    sets[setIndex] =
                                                        sets[setIndex].copy(reps = newReps)
                                                    exercises[exerciseIndex] =
                                                        exercises[exerciseIndex].copy(sets = sets)
                                                }
                                        }
                                    )
                                } else {
                                    Text("${set.weight}lbs × ${set.reps}")
                                }
                            }
                            if (isEditing) {
                                TextButton(onClick = {
                                    editableExercises =
                                        editableExercises.toMutableList().also { exercises ->
                                            val sets = exercises[exerciseIndex].sets.toMutableList()
                                            sets.add(ExerciseSet(setNumber = sets.size + 1))
                                            exercises[exerciseIndex] =
                                                exercises[exerciseIndex].copy(sets = sets)
                                        }
                                }) {
                                    Text("+ Add Set")
                                }
                                OutlinedTextField(
                                    value = entry.notes,
                                    onValueChange = { newNotes ->
                                        editableExercises = editableExercises.toMutableList().also { exercises ->
                                            exercises[exerciseIndex] = exercises[exerciseIndex].copy(notes = newNotes)
                                        }
                                    },
                                    placeholder = { Text("Notes") },
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    singleLine = false,
                                    minLines = 1
                                )
                            } else if (entry.notes.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Note: ${entry.notes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                if (isEditing) {
                    item {
                        Button(
                            onClick = {
                                session?.let { s ->
                                    val finishedAt = s.finishedAt
                                    val overAnHour = finishedAt != null &&
                                            (System.currentTimeMillis() - finishedAt.time) > 3_600_000L
                                    if (overAnHour) {
                                        showResumeConfirm = true
                                    } else {
                                        workoutViewModel.resumeWorkout(s)
                                        onContinueWorkout()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text("Continue Workout")
                        }
                    }
                } else {
                    item {
                        Button(
                            onClick = {
                                session?.let {
                                    workoutViewModel.copyWorkout(it)
                                    onCopyWorkout()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Text("Copy Workout")
                        }
                    }
                }
            }
        } ?: Box(Modifier.padding(padding).fillMaxSize()) { Text("Loading...") }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Workout?") },
            text = { Text("This workout will be permanently deleted and cannot be recovered.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    session?.let { viewModel.deleteSession(it.id); onBack() }
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showResumeConfirm) {
        AlertDialog(
            onDismissRequest = { showResumeConfirm = false },
            title = { Text("Resume Workout?") },
            text = { Text("It's been over an hour since you finished this workout. Are you sure you want to resume it?") },
            confirmButton = {
                TextButton(onClick = {
                    showResumeConfirm = false
                    session?.let { workoutViewModel.resumeWorkout(it); onContinueWorkout() }
                }) { Text("Resume") }
            },
            dismissButton = {
                TextButton(onClick = { showResumeConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun EditableSetRow(
    set: ExerciseSet,
    onWeightChange: (Int) -> Unit,
    onRepsChange: (Int) -> Unit
) {
    var weight by remember(set.setNumber) { mutableStateOf(TextFieldValue(if (set.weight > 0) set.weight.toString() else "")) }
    var reps by remember(set.setNumber) { mutableStateOf(TextFieldValue(if (set.reps > 0) set.reps.toString() else "")) }
    val scope = rememberCoroutineScope()

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Set ${set.setNumber}", modifier = Modifier.width(48.dp))
        OutlinedTextField(
            value = weight,
            onValueChange = {
                weight = it
                it.text.toIntOrNull()?.let(onWeightChange)
            },
            label = { Text("lbs") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { if (it.isFocused) scope.launch { weight = weight.copy(selection = TextRange(0, weight.text.length)) } },
            singleLine = true
        )
        OutlinedTextField(
            value = reps,
            onValueChange = {
                reps = it
                it.text.toIntOrNull()?.let(onRepsChange)
            },
            label = { Text("reps") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { if (it.isFocused) scope.launch { reps = reps.copy(selection = TextRange(0, reps.text.length)) } },
            singleLine = true
        )
    }
}
