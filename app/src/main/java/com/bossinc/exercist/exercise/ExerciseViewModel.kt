package com.bossinc.exercist.exercise

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
        }
    }
}
