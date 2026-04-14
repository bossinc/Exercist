package com.bossinc.exercist.template

import com.bossinc.exercist.data.model.WorkoutTemplate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface TemplateRepository {
    fun getTemplates(): Flow<List<WorkoutTemplate>>
    suspend fun createTemplate(template: WorkoutTemplate): Result<Unit>
    suspend fun getTemplate(id: String): WorkoutTemplate?
}

@Singleton
class FirebaseTemplateRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : TemplateRepository {
    private val userId get() = auth.currentUser?.uid ?: ""
    private fun collection() = firestore.collection("users").document(userId).collection("templates")

    override fun getTemplates(): Flow<List<WorkoutTemplate>> {
        if (userId.isEmpty()) return flowOf(emptyList())
        return callbackFlow {
            val listener = collection().addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val templates = snapshot?.documents?.mapNotNull { it.toObject(WorkoutTemplate::class.java) } ?: emptyList()
                trySend(templates)
            }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun createTemplate(template: WorkoutTemplate): Result<Unit> = runCatching {
        collection().add(template.copy(userId = userId)).await()
        Unit
    }

    override suspend fun getTemplate(id: String): WorkoutTemplate? =
        collection().document(id).get().await().toObject(WorkoutTemplate::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class TemplateModule {
    @Binds
    @Singleton
    abstract fun bindTemplateRepository(impl: FirebaseTemplateRepository): TemplateRepository
}
