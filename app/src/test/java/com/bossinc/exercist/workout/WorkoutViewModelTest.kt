package com.bossinc.exercist.workout

import com.bossinc.exercist.data.model.ExerciseEntry
import com.bossinc.exercist.data.model.ExerciseSet
import com.bossinc.exercist.data.model.WorkoutSession
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: WorkoutRepository
    private lateinit var viewModel: WorkoutViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        viewModel = WorkoutViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region initial state

    @Test
    fun `initial state is planning phase with no exercises`() {
        with(viewModel.uiState.value) {
            assertEquals(WorkoutPhase.PLANNING, phase)
            assertTrue(exercises.isEmpty())
            assertNull(error)
        }
    }

    // endregion

    // region addExercise

    @Test
    fun `addExercise appends entry with correct fields`() {
        viewModel.addExercise("ex1", "Bench Press", "Chest")

        val exercise = viewModel.uiState.value.exercises.single()
        assertEquals("ex1", exercise.exerciseId)
        assertEquals("Bench Press", exercise.exerciseName)
        assertEquals("Chest", exercise.muscleGroup)
    }

    @Test
    fun `addExercise starts exercise with one set numbered 1`() {
        viewModel.addExercise("ex1", "Bench Press", "Chest")

        val sets = viewModel.uiState.value.exercises[0].sets
        assertEquals(1, sets.size)
        assertEquals(1, sets[0].setNumber)
    }

    @Test
    fun `addExercise can add multiple exercises`() {
        viewModel.addExercise("ex1", "Bench Press", "Chest")
        viewModel.addExercise("ex2", "Squat", "Legs")

        assertEquals(2, viewModel.uiState.value.exercises.size)
    }

    // endregion

    // region startWorkout

    @Test
    fun `startWorkout transitions phase to ACTIVE`() {
        coEvery { repository.getRecentSessions() } returns emptyList()

        viewModel.startWorkout()

        assertEquals(WorkoutPhase.ACTIVE, viewModel.uiState.value.phase)
    }

    @Test
    fun `startWorkout loads previous sets for matching exercises`() {
        viewModel.addExercise("ex1", "Bench Press", "Chest")
        val prevSets = listOf(ExerciseSet(setNumber = 1, reps = 10, weight = 100))
        coEvery { repository.getRecentSessions() } returns listOf(
            WorkoutSession(exercises = listOf(ExerciseEntry(exerciseId = "ex1", sets = prevSets)))
        )

        viewModel.startWorkout()

        assertEquals(prevSets, viewModel.uiState.value.previousSets["ex1"])
    }

    @Test
    fun `startWorkout only takes first session per exercise`() {
        viewModel.addExercise("ex1", "Bench Press", "Chest")
        val sets1 = listOf(ExerciseSet(setNumber = 1, reps = 10, weight = 100))
        val sets2 = listOf(ExerciseSet(setNumber = 1, reps = 8, weight = 110))
        coEvery { repository.getRecentSessions() } returns listOf(
            WorkoutSession(exercises = listOf(ExerciseEntry(exerciseId = "ex1", sets = sets1))),
            WorkoutSession(exercises = listOf(ExerciseEntry(exerciseId = "ex1", sets = sets2)))
        )

        viewModel.startWorkout()

        assertEquals(sets1, viewModel.uiState.value.previousSets["ex1"])
    }

    @Test
    fun `startWorkout loads previous notes when non-blank`() {
        viewModel.addExercise("ex1", "Bench Press", "Chest")
        coEvery { repository.getRecentSessions() } returns listOf(
            WorkoutSession(exercises = listOf(ExerciseEntry(exerciseId = "ex1", notes = "Keep tight")))
        )

        viewModel.startWorkout()

        assertEquals("Keep tight", viewModel.uiState.value.previousNotes["ex1"])
    }

    @Test
    fun `startWorkout ignores exercises not in current workout`() {
        viewModel.addExercise("ex1", "Bench Press", "Chest")
        coEvery { repository.getRecentSessions() } returns listOf(
            WorkoutSession(exercises = listOf(ExerciseEntry(exerciseId = "other", sets = emptyList())))
        )

        viewModel.startWorkout()

        assertTrue(viewModel.uiState.value.previousSets.isEmpty())
    }

    // endregion

    // region copyWorkout

    @Test
    fun `copyWorkout resets to PLANNING phase`() {
        val session = WorkoutSession(exercises = listOf(ExerciseEntry(exerciseId = "ex1")))
        viewModel.copyWorkout(session)
        assertEquals(WorkoutPhase.PLANNING, viewModel.uiState.value.phase)
    }

    @Test
    fun `copyWorkout replaces each exercise's sets with a single fresh set`() {
        val session = WorkoutSession(
            exercises = listOf(
                ExerciseEntry(
                    exerciseId = "ex1",
                    sets = listOf(
                        ExerciseSet(setNumber = 1, reps = 5, weight = 200),
                        ExerciseSet(setNumber = 2, reps = 5, weight = 200)
                    )
                )
            )
        )

        viewModel.copyWorkout(session)

        val sets = viewModel.uiState.value.exercises[0].sets
        assertEquals(1, sets.size)
        assertEquals(1, sets[0].setNumber)
    }

    @Test
    fun `copyWorkout preserves exercise identity`() {
        val session = WorkoutSession(
            exercises = listOf(
                ExerciseEntry(exerciseId = "ex1", exerciseName = "Squat", muscleGroup = "Legs")
            )
        )

        viewModel.copyWorkout(session)

        val exercise = viewModel.uiState.value.exercises[0]
        assertEquals("ex1", exercise.exerciseId)
        assertEquals("Squat", exercise.exerciseName)
    }

    // endregion

    // region resumeWorkout

    @Test
    fun `resumeWorkout sets ACTIVE phase`() {
        viewModel.resumeWorkout(WorkoutSession(id = "s1"))
        assertEquals(WorkoutPhase.ACTIVE, viewModel.uiState.value.phase)
    }

    @Test
    fun `resumeWorkout restores original session exercises and sets`() {
        val sets = listOf(
            ExerciseSet(setNumber = 1, reps = 5, weight = 200),
            ExerciseSet(setNumber = 2, reps = 5, weight = 200)
        )
        val session = WorkoutSession(
            id = "s1",
            exercises = listOf(ExerciseEntry(exerciseId = "ex1", sets = sets))
        )

        viewModel.resumeWorkout(session)

        assertEquals(sets, viewModel.uiState.value.exercises[0].sets)
    }

    @Test
    fun `resumeWorkout deletes the session from repository`() {
        viewModel.resumeWorkout(WorkoutSession(id = "s1"))
        coVerify { repository.deleteWorkoutSession("s1") }
    }

    // endregion

    // region removeExercise

    @Test
    fun `removeExercise removes exercise at index`() {
        viewModel.addExercise("ex1", "Bench Press", "Chest")
        viewModel.addExercise("ex2", "Squat", "Legs")

        viewModel.removeExercise(0)

        val exercises = viewModel.uiState.value.exercises
        assertEquals(1, exercises.size)
        assertEquals("ex2", exercises[0].exerciseId)
    }

    @Test
    fun `removeExercise from end leaves preceding exercises intact`() {
        viewModel.addExercise("ex1", "Bench Press", "Chest")
        viewModel.addExercise("ex2", "Squat", "Legs")

        viewModel.removeExercise(1)

        assertEquals("ex1", viewModel.uiState.value.exercises[0].exerciseId)
    }

    // endregion

    // region removeLastSet

    @Test
    fun `removeLastSet removes last set`() {
        viewModel.addExercise("ex1", "Bench Press", "Chest")
        viewModel.addSet(0)

        viewModel.removeLastSet(0)

        assertEquals(1, viewModel.uiState.value.exercises[0].sets.size)
    }

    @Test
    fun `removeLastSet does nothing when only one set remains`() {
        viewModel.addExercise("ex1", "Bench Press", "Chest")

        viewModel.removeLastSet(0)

        assertEquals(1, viewModel.uiState.value.exercises[0].sets.size)
    }

    // endregion

    // region addSet

    @Test
    fun `addSet increments set count`() {
        viewModel.addExercise("ex1", "Bench Press", "Chest")

        viewModel.addSet(0)

        assertEquals(2, viewModel.uiState.value.exercises[0].sets.size)
    }

    @Test
    fun `addSet assigns correct setNumber`() {
        viewModel.addExercise("ex1", "Bench Press", "Chest")
        viewModel.addSet(0)

        val sets = viewModel.uiState.value.exercises[0].sets
        assertEquals(2, sets[1].setNumber)
    }

    @Test
    fun `addSet copies reps and weight from last set`() {
        viewModel.addExercise("ex1", "Bench Press", "Chest")
        viewModel.updateSetValues(0, 0, reps = 8, weight = 135)

        viewModel.addSet(0)

        val newSet = viewModel.uiState.value.exercises[0].sets[1]
        assertEquals(8, newSet.reps)
        assertEquals(135, newSet.weight)
    }

    // endregion

    // region updateExerciseNotes

    @Test
    fun `updateExerciseNotes updates notes for the correct exercise`() {
        viewModel.addExercise("ex1", "Bench Press", "Chest")
        viewModel.addExercise("ex2", "Squat", "Legs")

        viewModel.updateExerciseNotes(0, "Pause at bottom")

        assertEquals("Pause at bottom", viewModel.uiState.value.exercises[0].notes)
        assertEquals("", viewModel.uiState.value.exercises[1].notes)
    }

    // endregion

    // region updateSetValues

    @Test
    fun `updateSetValues updates reps and weight for the target set`() {
        viewModel.addExercise("ex1", "Bench Press", "Chest")

        viewModel.updateSetValues(0, 0, reps = 12, weight = 185)

        val set = viewModel.uiState.value.exercises[0].sets[0]
        assertEquals(12, set.reps)
        assertEquals(185, set.weight)
    }

    @Test
    fun `updateSetValues does not affect other sets`() {
        viewModel.addExercise("ex1", "Bench Press", "Chest")
        viewModel.addSet(0)

        viewModel.updateSetValues(0, 0, reps = 12, weight = 185)

        val untouched = viewModel.uiState.value.exercises[0].sets[1]
        assertEquals(0, untouched.reps)
        assertEquals(0, untouched.weight)
    }

    // endregion

    // region finishWorkout

    @Test
    fun `finishWorkout resets to initial state`() {
        coEvery { repository.saveWorkoutSession(any()) } returns Result.success(Unit)
        coEvery { repository.getRecentSessions() } returns emptyList()
        viewModel.addExercise("ex1", "Bench Press", "Chest")
        viewModel.startWorkout()

        viewModel.finishWorkout()

        val state = viewModel.uiState.value
        assertEquals(WorkoutPhase.PLANNING, state.phase)
        assertTrue(state.exercises.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `finishWorkout saves session to repository`() {
        coEvery { repository.saveWorkoutSession(any()) } returns Result.success(Unit)
        coEvery { repository.getRecentSessions() } returns emptyList()
        viewModel.addExercise("ex1", "Bench Press", "Chest")
        viewModel.startWorkout()

        viewModel.finishWorkout()

        coVerify { repository.saveWorkoutSession(any()) }
    }

    @Test
    fun `finishWorkout saves exercises from snapshot`() {
        val captured = mutableListOf<WorkoutSession>()
        coEvery { repository.saveWorkoutSession(capture(captured)) } returns Result.success(Unit)
        coEvery { repository.getRecentSessions() } returns emptyList()
        viewModel.addExercise("ex1", "Bench Press", "Chest")
        viewModel.startWorkout()

        viewModel.finishWorkout()

        assertEquals("ex1", captured.single().exercises[0].exerciseId)
    }

    @Test
    fun `finishWorkout sets error on repository failure`() {
        coEvery { repository.saveWorkoutSession(any()) } returns Result.failure(Exception("Save failed"))
        coEvery { repository.getRecentSessions() } returns emptyList()
        viewModel.addExercise("ex1", "Bench Press", "Chest")
        viewModel.startWorkout()

        viewModel.finishWorkout()

        assertEquals("Save failed", viewModel.uiState.value.error)
    }

    // endregion
}
