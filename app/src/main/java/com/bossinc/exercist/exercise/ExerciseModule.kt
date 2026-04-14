package com.bossinc.exercist.exercise

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ExerciseModule {
    @Binds
    @Singleton
    abstract fun bindExerciseRepository(impl: FirebaseExerciseRepository): ExerciseRepository
}
