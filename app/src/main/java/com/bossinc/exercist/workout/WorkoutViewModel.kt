package com.bossinc.exercist.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bossinc.exercist.data.model.ExerciseEntry
import com.bossinc.exercist.data.model.ExerciseSet
import com.bossinc.exercist.data.model.WorkoutSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

enum class WorkoutPhase { PLANNING, ACTIVE }

data class WorkoutUiState(
    val phase: WorkoutPhase = WorkoutPhase.PLANNING,
    val exercises: List<ExerciseEntry> = emptyList(),
    // maps exerciseId -> sets from last session containing that exercise
    val previousSets: Map<String, List<ExerciseSet>> = emptyMap(),
    val previousNotes: Map<String, String> = emptyMap(),
    val error: String? = null
)

private const val DRAFT_SYNC_DEBOUNCE_MS = 1500L

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState
    private var startedAt: Date? = null
    private var draftId: String = ""
    private var syncJob: Job? = null

    fun addExercise(exerciseId: String) {
        _uiState.value = _uiState.value.copy(
            exercises = _uiState.value.exercises + ExerciseEntry(
                exerciseId = exerciseId,
                sets = listOf(ExerciseSet())
            )
        )
        scheduleDraftSync()
    }

    fun startWorkout() {
        startedAt = Date()
        _uiState.value = _uiState.value.copy(phase = WorkoutPhase.ACTIVE)
        scheduleDraftSync(immediate = true)
        val exerciseIds = _uiState.value.exercises.map { it.exerciseId }.toSet()
        viewModelScope.launch {
            val previousSets = mutableMapOf<String, List<ExerciseSet>>()
            val previousNotes = mutableMapOf<String, String>()
            repository.getRecentSessions().forEach { session ->
                session.exercises.forEach { entry ->
                    if (entry.exerciseId in exerciseIds && entry.exerciseId !in previousSets) {
                        previousSets[entry.exerciseId] = entry.sets
                        if (entry.notes.isNotBlank()) previousNotes[entry.exerciseId] = entry.notes
                    }
                }
            }
            _uiState.value = _uiState.value.copy(previousSets = previousSets, previousNotes = previousNotes)
        }
    }

    fun copyWorkout(session: com.bossinc.exercist.data.model.WorkoutSession) {
        syncJob?.cancel()
        draftId = ""
        startedAt = null
        _uiState.value = WorkoutUiState(
            phase = WorkoutPhase.PLANNING,
            exercises = session.exercises.map { it.copy(sets = listOf(ExerciseSet())) }
        )
    }

    fun resumeWorkout(session: com.bossinc.exercist.data.model.WorkoutSession) {
        syncJob?.cancel()
        startedAt = session.startedAt ?: Date()
        draftId = session.id
        _uiState.value = WorkoutUiState(
            phase = WorkoutPhase.ACTIVE,
            exercises = session.exercises
        )
        scheduleDraftSync(immediate = true)
    }

    fun moveExercise(fromIndex: Int, toIndex: Int) {
        val exercises = _uiState.value.exercises.toMutableList()
        if (toIndex < 0 || toIndex >= exercises.size) return
        val item = exercises.removeAt(fromIndex)
        exercises.add(toIndex, item)
        _uiState.value = _uiState.value.copy(exercises = exercises)
        scheduleDraftSync()
    }

    fun removeExercise(exerciseIndex: Int) {
        val exercises = _uiState.value.exercises.toMutableList()
        exercises.removeAt(exerciseIndex)
        _uiState.value = _uiState.value.copy(exercises = exercises)
        scheduleDraftSync()
    }

    fun removeLastSet(exerciseIndex: Int) {
        val exercises = _uiState.value.exercises.toMutableList()
        val entry = exercises[exerciseIndex]
        if (entry.sets.size <= 1) return
        exercises[exerciseIndex] = entry.copy(sets = entry.sets.dropLast(1))
        _uiState.value = _uiState.value.copy(exercises = exercises)
        scheduleDraftSync()
    }

    fun addSet(exerciseIndex: Int) {
        val exercises = _uiState.value.exercises.toMutableList()
        val entry = exercises[exerciseIndex]
        val last = entry.sets.lastOrNull()
        exercises[exerciseIndex] = entry.copy(
            sets = entry.sets + ExerciseSet(
                reps = last?.reps ?: 0,
                weight = last?.weight ?: 0
            )
        )
        _uiState.value = _uiState.value.copy(exercises = exercises)
        scheduleDraftSync()
    }

    fun updateExerciseNotes(exerciseIndex: Int, notes: String) {
        val exercises = _uiState.value.exercises.toMutableList()
        exercises[exerciseIndex] = exercises[exerciseIndex].copy(notes = notes)
        _uiState.value = _uiState.value.copy(exercises = exercises)
        scheduleDraftSync()
    }

    fun updateSetValues(exerciseIndex: Int, setIndex: Int, reps: Int, weight: Int) {
        val exercises = _uiState.value.exercises.toMutableList()
        val entry = exercises[exerciseIndex]
        val sets = entry.sets.toMutableList()
        sets[setIndex] = sets[setIndex].copy(reps = reps, weight = weight)
        exercises[exerciseIndex] = entry.copy(sets = sets)
        _uiState.value = _uiState.value.copy(exercises = exercises)
        scheduleDraftSync()
    }

    fun finishWorkout() {
        val snapshot = _uiState.value
        val finishedAt = Date()
        val start = startedAt ?: finishedAt
        val finalDraftId = draftId
        syncJob?.cancel()
        _uiState.value = WorkoutUiState()
        startedAt = null
        draftId = ""
        viewModelScope.launch {
            val session = WorkoutSession(
                id = finalDraftId,
                exercises = snapshot.exercises,
                startedAt = start,
                finishedAt = finishedAt
            )
            repository.upsertWorkoutSession(session)
                .onFailure { e -> _uiState.value = WorkoutUiState(error = e.message) }
        }
    }

    private fun scheduleDraftSync(immediate: Boolean = false) {
        if (_uiState.value.phase != WorkoutPhase.ACTIVE) return
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            if (!immediate) delay(DRAFT_SYNC_DEBOUNCE_MS)
            val state = _uiState.value
            val session = WorkoutSession(
                id = draftId,
                exercises = state.exercises,
                startedAt = startedAt,
                finishedAt = null
            )
            repository.upsertWorkoutSession(session)
                .onSuccess { id -> if (draftId.isBlank()) draftId = id }
        }
    }
}
