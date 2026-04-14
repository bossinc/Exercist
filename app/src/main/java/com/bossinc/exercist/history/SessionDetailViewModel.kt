package com.bossinc.exercist.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bossinc.exercist.data.model.Exercise
import com.bossinc.exercist.data.model.WorkoutSession
import com.bossinc.exercist.exercise.ExerciseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {
    private fun workoutsCollection() = firestore.collection("users")
        .document(auth.currentUser?.uid ?: "").collection("workouts")

    private val _selectedSession = MutableStateFlow<WorkoutSession?>(null)
    val selectedSession: StateFlow<WorkoutSession?> = _selectedSession

    private val _sessionDeleted = MutableStateFlow(false)
    val sessionDeleted: StateFlow<Boolean> = _sessionDeleted

    val exercises: StateFlow<List<Exercise>> = exerciseRepository.getExercises()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadSession(id: String) {
        viewModelScope.launch {
            _selectedSession.value = workoutsCollection().document(id).get().await()
                .toObject(WorkoutSession::class.java)
        }
    }

    fun updateSession(session: WorkoutSession) {
        viewModelScope.launch {
            workoutsCollection().document(session.id).set(session).await()
            _selectedSession.value = session
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            workoutsCollection().document(sessionId).delete().await()
            _sessionDeleted.value = true
        }
    }
}
