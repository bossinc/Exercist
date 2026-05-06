package com.bossinc.exercist.exercise

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bossinc.exercist.data.model.Exercise
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val repository: ExerciseRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    private val _selectedMuscleGroup = MutableStateFlow<String?>(null)
    private val _allExercises = MutableStateFlow<List<Exercise>>(emptyList())
    val allExercises: StateFlow<List<Exercise>> = _allExercises

    val exercises: StateFlow<List<Exercise>> = combine(
        _allExercises, _searchQuery, _selectedMuscleGroup
    ) { all, query, group ->
        all.filter { exercise ->
            (query.isBlank() || exercise.name.contains(query, ignoreCase = true)) &&
            (group == null || exercise.muscleGroup == group)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchQuery: StateFlow<String> = _searchQuery
    val selectedMuscleGroup: StateFlow<String?> = _selectedMuscleGroup

    init {
        viewModelScope.launch {
            repository.getExercises().collect { _allExercises.value = it }
        }
    }

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setMuscleGroup(group: String?) { _selectedMuscleGroup.value = group }

    private val _exerciseDeleted = MutableStateFlow(false)
    val exerciseDeleted: StateFlow<Boolean> = _exerciseDeleted

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    fun clearError() { _errorMessage.value = null }

    fun createExercise(exercise: Exercise) {
        viewModelScope.launch { repository.createExercise(exercise) }
    }

    fun updateExercise(exercise: Exercise) {
        viewModelScope.launch { repository.updateExercise(exercise) }
    }

    fun deleteExercise(exerciseId: String) {
        viewModelScope.launch {
            repository.deleteExercise(exerciseId)
                .onSuccess { _exerciseDeleted.value = true }
                .onFailure {
                    Log.e("ExerciseViewModel", "deleteExercise failed", it)
                    _errorMessage.value = "Delete failed: ${it.message ?: it.javaClass.simpleName}"
                }
        }
    }

    fun importExercises(exercises: List<Exercise>) {
        viewModelScope.launch {
            exercises.forEach { repository.upsertExercise(it) }
        }
    }
}
