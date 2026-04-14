package com.bossinc.exercist.profile

import com.bossinc.exercist.data.model.User
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
class ProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: UserRepository
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        every { repository.getUser() } returns flowOf(null)
        viewModel = ProfileViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region user state

    @Test
    fun `user defaults to null when repository emits null`() = runTest {
        backgroundScope.launch(testDispatcher) { viewModel.user.collect {} }
        assertNull(viewModel.user.value)
    }

    @Test
    fun `user reflects value emitted by repository`() = runTest {
        val user = User(id = "u1", email = "user@example.com", displayName = "Alice")
        every { repository.getUser() } returns flowOf(user)
        val vm = ProfileViewModel(repository)

        backgroundScope.launch(testDispatcher) { vm.user.collect {} }

        assertEquals(user, vm.user.value)
    }

    @Test
    fun `user exposes display name from repository`() = runTest {
        val user = User(id = "u1", displayName = "Bob")
        every { repository.getUser() } returns flowOf(user)
        val vm = ProfileViewModel(repository)

        backgroundScope.launch(testDispatcher) { vm.user.collect {} }

        assertEquals("Bob", vm.user.value?.displayName)
    }

    // endregion

    // region signOut

    @Test
    fun `signOut delegates to repository`() {
        viewModel.signOut()
        verify { repository.signOut() }
    }

    // endregion
}
