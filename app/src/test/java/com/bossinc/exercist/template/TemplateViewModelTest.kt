package com.bossinc.exercist.template

import com.bossinc.exercist.data.model.WorkoutTemplate
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
class TemplateViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: TemplateRepository
    private lateinit var viewModel: TemplateViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        every { repository.getTemplates() } returns flowOf(emptyList())
        viewModel = TemplateViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region templates flow

    @Test
    fun `templates emits list from repository`() = runTest {
        val list = listOf(
            WorkoutTemplate(id = "t1", name = "Push Day"),
            WorkoutTemplate(id = "t2", name = "Pull Day")
        )
        every { repository.getTemplates() } returns flowOf(list)
        val vm = TemplateViewModel(repository)

        backgroundScope.launch(testDispatcher) { vm.templates.collect {} }

        assertEquals(list, vm.templates.value)
    }

    @Test
    fun `templates defaults to empty list`() = runTest {
        backgroundScope.launch(testDispatcher) { viewModel.templates.collect {} }
        assertTrue(viewModel.templates.value.isEmpty())
    }

    // endregion

    // region loadTemplate

    @Test
    fun `loadTemplate sets selectedTemplate from repository`() = runTest {
        val template = WorkoutTemplate(id = "t1", name = "Push Day")
        coEvery { repository.getTemplate("t1") } returns template

        viewModel.loadTemplate("t1")

        assertEquals(template, viewModel.selectedTemplate.value)
    }

    @Test
    fun `loadTemplate sets null when repository returns null`() = runTest {
        coEvery { repository.getTemplate("missing") } returns null

        viewModel.loadTemplate("missing")

        assertNull(viewModel.selectedTemplate.value)
    }

    @Test
    fun `loadTemplate overwrites previously loaded template`() = runTest {
        val t1 = WorkoutTemplate(id = "t1", name = "Push Day")
        val t2 = WorkoutTemplate(id = "t2", name = "Pull Day")
        coEvery { repository.getTemplate("t1") } returns t1
        coEvery { repository.getTemplate("t2") } returns t2

        viewModel.loadTemplate("t1")
        viewModel.loadTemplate("t2")

        assertEquals(t2, viewModel.selectedTemplate.value)
    }

    // endregion

    // region createTemplate

    @Test
    fun `createTemplate delegates to repository`() = runTest {
        val template = WorkoutTemplate(name = "Leg Day")

        viewModel.createTemplate(template)

        coVerify { repository.createTemplate(template) }
    }

    // endregion
}
