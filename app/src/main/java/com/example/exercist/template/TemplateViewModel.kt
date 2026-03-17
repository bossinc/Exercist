package com.example.exercist.template

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exercist.data.model.WorkoutTemplate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TemplateViewModel @Inject constructor(
    private val repository: TemplateRepository
) : ViewModel() {
    val templates: StateFlow<List<WorkoutTemplate>> = repository.getTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTemplate = MutableStateFlow<WorkoutTemplate?>(null)
    val selectedTemplate: StateFlow<WorkoutTemplate?> = _selectedTemplate

    fun createTemplate(template: WorkoutTemplate) {
        viewModelScope.launch { repository.createTemplate(template) }
    }

    fun loadTemplate(id: String) {
        viewModelScope.launch { _selectedTemplate.value = repository.getTemplate(id) }
    }
}
