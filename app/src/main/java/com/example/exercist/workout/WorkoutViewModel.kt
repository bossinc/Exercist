package com.example.exercist.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exercist.data.model.ExerciseEntry
import com.example.exercist.data.model.ExerciseSet
import com.example.exercist.data.model.WorkoutSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WorkoutUiState(
    val sessionName: String = "New Workout",
    val exercises: List<ExerciseEntry> = emptyList(),
    val restTimerSeconds: Int = 0,
    val isTimerRunning: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState
    private var timerJob: Job? = null
    private val startTime = System.currentTimeMillis()

    fun addExercise(exerciseId: String, exerciseName: String) {
        _uiState.value = _uiState.value.copy(
            exercises = _uiState.value.exercises + ExerciseEntry(
                exerciseId = exerciseId,
                exerciseName = exerciseName,
                sets = listOf(ExerciseSet(setNumber = 1))
            )
        )
    }

    fun addSet(exerciseIndex: Int) {
        val exercises = _uiState.value.exercises.toMutableList()
        val entry = exercises[exerciseIndex]
        exercises[exerciseIndex] = entry.copy(
            sets = entry.sets + ExerciseSet(setNumber = entry.sets.size + 1)
        )
        _uiState.value = _uiState.value.copy(exercises = exercises)
    }

    fun updateSet(exerciseIndex: Int, setIndex: Int, reps: Int, weight: Double) {
        val exercises = _uiState.value.exercises.toMutableList()
        val entry = exercises[exerciseIndex]
        val sets = entry.sets.toMutableList()
        sets[setIndex] = sets[setIndex].copy(reps = reps, weight = weight, isCompleted = true)
        exercises[exerciseIndex] = entry.copy(sets = sets)
        _uiState.value = _uiState.value.copy(exercises = exercises)
    }

    fun startRestTimer(seconds: Int = 90) {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(restTimerSeconds = seconds, isTimerRunning = true)
        timerJob = viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                _uiState.value = _uiState.value.copy(restTimerSeconds = remaining)
            }
            _uiState.value = _uiState.value.copy(isTimerRunning = false)
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(isTimerRunning = false, restTimerSeconds = 0)
    }

    fun finishWorkout() {
        viewModelScope.launch {
            val durationMinutes = ((System.currentTimeMillis() - startTime) / 60000).toInt()
            val session = WorkoutSession(
                name = _uiState.value.sessionName,
                exercises = _uiState.value.exercises,
                durationMinutes = durationMinutes
            )
            repository.saveWorkoutSession(session)
                .onSuccess { _uiState.value = _uiState.value.copy(isSaved = true) }
                .onFailure { e -> _uiState.value = _uiState.value.copy(error = e.message) }
        }
    }
}
