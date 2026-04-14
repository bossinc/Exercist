package com.bossinc.exercist.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun skipAuth() {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            repository.signInAnonymously()
                .onSuccess { _uiState.value = AuthUiState(isAuthenticated = true) }
                .onFailure { e -> _uiState.value = AuthUiState(error = e.message) }
        }
    }

    fun signInWithGoogle(context: Context, webClientId: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            repository.signInWithGoogle(context, webClientId)
                .onSuccess { _uiState.value = AuthUiState(isAuthenticated = true) }
                .onFailure { e -> _uiState.value = AuthUiState(error = e.message) }
        }
    }
}
