package com.bossinc.exercist.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bossinc.exercist.data.model.ExerciseEntry
import com.bossinc.exercist.data.model.ExerciseSet
import com.bossinc.exercist.data.model.WorkoutSession
import dagger.hilt.android.lifecycle.HiltViewModel
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

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState
    private var startedAt: Date? = null

    fun addExercise(exerciseId: String, exerciseName: String, muscleGroup: String) {
        _uiState.value = _uiState.value.copy(
            exercises = _uiState.value.exercises + ExerciseEntry(
                exerciseId = exerciseId,
                exerciseName = exerciseName,
                muscleGroup = muscleGroup,
                sets = listOf(ExerciseSet(setNumber = 1))
            )
        )
    }

    fun startWorkout() {
        startedAt = Date()
        _uiState.value = _uiState.value.copy(phase = WorkoutPhase.ACTIVE)
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
        _uiState.value = WorkoutUiState(
            phase = WorkoutPhase.PLANNING,
            exercises = session.exercises.map { it.copy(sets = listOf(ExerciseSet(setNumber = 1))) }
        )
    }

    fun resumeWorkout(session: com.bossinc.exercist.data.model.WorkoutSession) {
        startedAt = session.startedAt ?: session.date ?: Date()
        _uiState.value = WorkoutUiState(
            phase = WorkoutPhase.ACTIVE,
            exercises = session.exercises
        )
        viewModelScope.launch { repository.deleteWorkoutSession(session.id) }
    }

    fun moveExercise(fromIndex: Int, toIndex: Int) {
        val exercises = _uiState.value.exercises.toMutableList()
        if (toIndex < 0 || toIndex >= exercises.size) return
        val item = exercises.removeAt(fromIndex)
        exercises.add(toIndex, item)
        _uiState.value = _uiState.value.copy(exercises = exercises)
    }

    fun removeExercise(exerciseIndex: Int) {
        val exercises = _uiState.value.exercises.toMutableList()
        exercises.removeAt(exerciseIndex)
        _uiState.value = _uiState.value.copy(exercises = exercises)
    }

    fun removeLastSet(exerciseIndex: Int) {
        val exercises = _uiState.value.exercises.toMutableList()
        val entry = exercises[exerciseIndex]
        if (entry.sets.size <= 1) return
        exercises[exerciseIndex] = entry.copy(sets = entry.sets.dropLast(1))
        _uiState.value = _uiState.value.copy(exercises = exercises)
    }

    fun addSet(exerciseIndex: Int) {
        val exercises = _uiState.value.exercises.toMutableList()
        val entry = exercises[exerciseIndex]
        val last = entry.sets.lastOrNull()
        exercises[exerciseIndex] = entry.copy(
            sets = entry.sets + ExerciseSet(
                setNumber = entry.sets.size + 1,
                reps = last?.reps ?: 0,
                weight = last?.weight ?: 0
            )
        )
        _uiState.value = _uiState.value.copy(exercises = exercises)
    }

    fun updateExerciseNotes(exerciseIndex: Int, notes: String) {
        val exercises = _uiState.value.exercises.toMutableList()
        exercises[exerciseIndex] = exercises[exerciseIndex].copy(notes = notes)
        _uiState.value = _uiState.value.copy(exercises = exercises)
    }

    fun updateSetValues(exerciseIndex: Int, setIndex: Int, reps: Int, weight: Int) {
        val exercises = _uiState.value.exercises.toMutableList()
        val entry = exercises[exerciseIndex]
        val sets = entry.sets.toMutableList()
        sets[setIndex] = sets[setIndex].copy(reps = reps, weight = weight)
        exercises[exerciseIndex] = entry.copy(sets = sets)
        _uiState.value = _uiState.value.copy(exercises = exercises)
    }

    fun finishWorkout() {
        val snapshot = _uiState.value
        val finishedAt = Date()
        val start = startedAt ?: finishedAt
        val durationMinutes = ((finishedAt.time - start.time) / 60000).toInt()
        _uiState.value = WorkoutUiState()
        viewModelScope.launch {
            val session = WorkoutSession(
                exercises = snapshot.exercises,
                durationMinutes = durationMinutes,
                startedAt = start,
                finishedAt = finishedAt
            )
            repository.saveWorkoutSession(session)
                .onFailure { e -> _uiState.value = WorkoutUiState(error = e.message) }
        }
    }
}
