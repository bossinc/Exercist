package com.bossinc.exercist.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bossinc.exercist.data.model.WorkoutSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: HistoryRepository
) : ViewModel() {

    val sessions: StateFlow<List<WorkoutSession>> = repository.getSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSession = MutableStateFlow<WorkoutSession?>(null)
    val selectedSession: StateFlow<WorkoutSession?> = _selectedSession

    private val _sessionDeleted = MutableStateFlow(false)
    val sessionDeleted: StateFlow<Boolean> = _sessionDeleted

    fun loadSession(id: String) {
        viewModelScope.launch {
            _selectedSession.value = repository.loadSession(id)
        }
    }

    fun updateSession(session: WorkoutSession) {
        viewModelScope.launch {
            repository.updateSession(session)
            _selectedSession.value = session
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            _sessionDeleted.value = true
        }
    }
}
