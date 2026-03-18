package com.bossinc.exercist.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bossinc.exercist.data.model.WorkoutSession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {
    private val userId get() = auth.currentUser?.uid ?: ""

    val sessions: StateFlow<List<WorkoutSession>> = callbackFlow {
        if (userId.isEmpty()) { trySend(emptyList()); close(); return@callbackFlow }
        val listener = firestore.collection("users").document(userId).collection("workouts")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val sessions = snapshot?.documents?.mapNotNull { it.toObject(WorkoutSession::class.java) }
                    ?.sortedByDescending { it.startedAt ?: it.date } ?: emptyList()
                trySend(sessions)
            }
        awaitClose { listener.remove() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSession = MutableStateFlow<WorkoutSession?>(null)
    val selectedSession: StateFlow<WorkoutSession?> = _selectedSession

    private val _sessionDeleted = MutableStateFlow(false)
    val sessionDeleted: StateFlow<Boolean> = _sessionDeleted

    fun loadSession(id: String) {
        viewModelScope.launch {
            _selectedSession.value = firestore.collection("users").document(userId)
                .collection("workouts").document(id).get().await()
                .toObject(WorkoutSession::class.java)
        }
    }

    fun updateSession(session: WorkoutSession) {
        viewModelScope.launch {
            firestore.collection("users").document(userId)
                .collection("workouts").document(session.id)
                .set(session).await()
            _selectedSession.value = session
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            firestore.collection("users").document(userId)
                .collection("workouts").document(sessionId)
                .delete().await()
            _sessionDeleted.value = true
        }
    }
}
