package com.bossinc.exercist.exercise

import com.bossinc.exercist.data.model.Exercise
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: ExerciseRepository
    private lateinit var viewModel: ExerciseViewModel

    private val allExercises = listOf(
        Exercise(id = "1", name = "Bench Press", muscleGroup = "Chest"),
        Exercise(id = "2", name = "Incline Bench", muscleGroup = "Chest"),
        Exercise(id = "3", name = "Squat", muscleGroup = "Legs"),
        Exercise(id = "4", name = "Overhead Press", muscleGroup = "Shoulders")
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        every { repository.getExercises() } returns flowOf(allExercises)
        viewModel = ExerciseViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Subscribes to the exercises flow so SharingStarted.WhileSubscribed starts the upstream.
    // Returns the current value after the upstream has emitted.
    private fun currentExercises(): List<Exercise> = viewModel.exercises.value

    private fun startCollecting() = runTest {
        backgroundScope.launch(testDispatcher) { viewModel.exercises.collect {} }
    }

    // region filtering

    @Test
    fun `exercises emits all exercises when no filter is set`() = runTest {
        backgroundScope.launch(testDispatcher) { viewModel.exercises.collect {} }
        assertEquals(allExercises, viewModel.exercises.value)
    }

    @Test
    fun `setSearchQuery filters by name case-insensitively`() = runTest {
        backgroundScope.launch(testDispatcher) { viewModel.exercises.collect {} }

        viewModel.setSearchQuery("bench")

        val result = viewModel.exercises.value
        assertEquals(2, result.size)
        assertTrue(result.all { "bench" in it.name.lowercase() })
    }

    @Test
    fun `setSearchQuery with no match returns empty list`() = runTest {
        backgroundScope.launch(testDispatcher) { viewModel.exercises.collect {} }

        viewModel.setSearchQuery("zzz")

        assertTrue(viewModel.exercises.value.isEmpty())
    }

    @Test
    fun `setSearchQuery empty string clears filter`() = runTest {
        backgroundScope.launch(testDispatcher) { viewModel.exercises.collect {} }
        viewModel.setSearchQuery("bench")

        viewModel.setSearchQuery("")

        assertEquals(allExercises, viewModel.exercises.value)
    }

    @Test
    fun `setMuscleGroup filters by exact muscle group`() = runTest {
        backgroundScope.launch(testDispatcher) { viewModel.exercises.collect {} }

        viewModel.setMuscleGroup("Chest")

        val result = viewModel.exercises.value
        assertEquals(2, result.size)
        assertTrue(result.all { it.muscleGroup == "Chest" })
    }

    @Test
    fun `setMuscleGroup null removes group filter`() = runTest {
        backgroundScope.launch(testDispatcher) { viewModel.exercises.collect {} }
        viewModel.setMuscleGroup("Chest")

        viewModel.setMuscleGroup(null)

        assertEquals(allExercises, viewModel.exercises.value)
    }

    @Test
    fun `combined name and group filter applies both criteria`() = runTest {
        backgroundScope.launch(testDispatcher) { viewModel.exercises.collect {} }

        viewModel.setSearchQuery("bench")
        viewModel.setMuscleGroup("Chest")

        val result = viewModel.exercises.value
        assertEquals(2, result.size)
    }

    @Test
    fun `combined filter with non-matching group returns empty`() = runTest {
        backgroundScope.launch(testDispatcher) { viewModel.exercises.collect {} }

        viewModel.setSearchQuery("Squat")
        viewModel.setMuscleGroup("Chest")

        assertTrue(viewModel.exercises.value.isEmpty())
    }

    // endregion

    // region state flows

    @Test
    fun `searchQuery reflects current value`() {
        viewModel.setSearchQuery("squat")
        assertEquals("squat", viewModel.searchQuery.value)
    }

    @Test
    fun `selectedMuscleGroup reflects current value`() {
        viewModel.setMuscleGroup("Legs")
        assertEquals("Legs", viewModel.selectedMuscleGroup.value)
    }

    // endregion

    // region CRUD

    @Test
    fun `deleteExercise success sets exerciseDeleted true`() = runTest {
        coEvery { repository.deleteExercise("1") } returns Result.success(Unit)

        viewModel.deleteExercise("1")

        assertTrue(viewModel.exerciseDeleted.value)
    }

    @Test
    fun `deleteExercise failure does not set exerciseDeleted`() = runTest {
        coEvery { repository.deleteExercise("1") } returns Result.failure(Exception("Network error"))

        viewModel.deleteExercise("1")

        assertFalse(viewModel.exerciseDeleted.value)
    }

    @Test
    fun `createExercise delegates to repository`() = runTest {
        val exercise = Exercise(name = "Deadlift", muscleGroup = "Back")

        viewModel.createExercise(exercise)

        coVerify { repository.createExercise(exercise) }
    }

    @Test
    fun `updateExercise delegates to repository`() = runTest {
        val exercise = Exercise(id = "1", name = "Bench Press", muscleGroup = "Chest")

        viewModel.updateExercise(exercise)

        coVerify { repository.updateExercise(exercise) }
    }

    // endregion
}
