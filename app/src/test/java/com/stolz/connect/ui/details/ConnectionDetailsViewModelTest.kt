package com.stolz.connect.ui.details

import com.stolz.connect.data.repository.ConnectionRepository
import com.stolz.connect.domain.model.ConnectionMethod
import com.stolz.connect.domain.model.ScheduledConnection
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionDetailsViewModelTest {

    private lateinit var repository: ConnectionRepository
    private lateinit var viewModel: ConnectionDetailsViewModel

    @Before
    fun setup() {
        repository = mockk()
    }

    @Test
    fun `loads connection successfully`() = runTest {
        val connection = ScheduledConnection(
            id = 1L,
            contactName = "John Doe",
            contactPhoneNumber = "123-456-7890",
            reminderFrequencyDays = 7,
            preferredMethod = ConnectionMethod.CALL,
            nextReminderDate = Date()
        )

        coEvery { repository.getConnectionById(1L) } returns connection
        viewModel = ConnectionDetailsViewModel(repository, mockk { every { get<Long>("connectionId") } returns 1L })

        advanceUntilIdle()

        // The state should now be Success
        val state = viewModel.uiState.value
        assertTrue(state is ConnectionDetailsUiState.Success)
        val successState = state as ConnectionDetailsUiState.Success
        assertEquals("John Doe", successState.connection.contactName)
    }

    @Test
    fun `shows error when connection not found`() = runTest {
        coEvery { repository.getConnectionById(1L) } returns null
        viewModel = ConnectionDetailsViewModel(repository, mockk { every { get<Long>("connectionId") } returns 1L })

        // Advance to let the init block's coroutine complete
        advanceUntilIdle()

        // The state should now be Error
        val state = viewModel.uiState.value
        assertTrue(state is ConnectionDetailsUiState.Error)
        assertEquals("Connection not found", (state as ConnectionDetailsUiState.Error).message)
    }

    @Test
    fun `refreshConnection reloads connection`() = runTest {
        val connection = ScheduledConnection(
            id = 1L,
            contactName = "Test",
            reminderFrequencyDays = 7,
            preferredMethod = ConnectionMethod.CALL,
            nextReminderDate = Date()
        )

        coEvery { repository.getConnectionById(1L) } returns connection
        viewModel = ConnectionDetailsViewModel(repository, mockk { every { get<Long>("connectionId") } returns 1L })

        advanceUntilIdle()

        viewModel.refreshConnection()
        advanceUntilIdle()

        coVerify(exactly = 2) { repository.getConnectionById(1L) }
    }

    @Test
    fun `markAsContacted calls repository and reloads`() = runTest {
        val connection = ScheduledConnection(
            id = 1L,
            contactName = "Test",
            reminderFrequencyDays = 7,
            preferredMethod = ConnectionMethod.CALL,
            nextReminderDate = Date()
        )

        coEvery { repository.getConnectionById(1L) } returns connection
        coEvery { repository.markAsContacted(connection) } returns Unit
        viewModel = ConnectionDetailsViewModel(repository, mockk { every { get<Long>("connectionId") } returns 1L })

        advanceUntilIdle()

        viewModel.markAsContacted()
        advanceUntilIdle()

        coVerify { repository.markAsContacted(connection) }
        coVerify(atLeast = 2) { repository.getConnectionById(1L) }
    }

    @Test
    fun `deleteConnection calls repository delete`() = runTest {
        val connection = ScheduledConnection(
            id = 1L,
            contactName = "Test",
            reminderFrequencyDays = 7,
            preferredMethod = ConnectionMethod.CALL,
            nextReminderDate = Date()
        )

        coEvery { repository.getConnectionById(1L) } returns connection
        coEvery { repository.deleteConnection(connection) } returns Unit
        viewModel = ConnectionDetailsViewModel(repository, mockk { every { get<Long>("connectionId") } returns 1L })

        advanceUntilIdle()

        viewModel.deleteConnection()
        advanceUntilIdle()

        val result = viewModel.deleteResult.value
        assertTrue(result is DeleteResult.Success)

        coVerify { repository.deleteConnection(connection) }
    }

    @Test
    fun `deleteConnection handles errors`() = runTest {
        val connection = ScheduledConnection(
            id = 1L,
            contactName = "Test",
            reminderFrequencyDays = 7,
            preferredMethod = ConnectionMethod.CALL,
            nextReminderDate = Date()
        )

        coEvery { repository.getConnectionById(1L) } returns connection
        coEvery { repository.deleteConnection(connection) } throws Exception("Delete failed")
        viewModel = ConnectionDetailsViewModel(repository, mockk { every { get<Long>("connectionId") } returns 1L })

        advanceUntilIdle()

        viewModel.deleteConnection()
        advanceUntilIdle()

        val result = viewModel.deleteResult.value
        assertTrue(result is DeleteResult.Error)
        assertTrue((result as DeleteResult.Error).message.contains("Delete failed"))
    }

    @Test
    fun `clearDeleteResult clears delete result`() = runTest {
        val connection = ScheduledConnection(
            id = 1L,
            contactName = "Test",
            reminderFrequencyDays = 7,
            preferredMethod = ConnectionMethod.CALL,
            nextReminderDate = Date()
        )

        coEvery { repository.getConnectionById(1L) } returns connection
        coEvery { repository.deleteConnection(connection) } returns Unit
        viewModel = ConnectionDetailsViewModel(repository, mockk { every { get<Long>("connectionId") } returns 1L })

        advanceUntilIdle()

        viewModel.deleteConnection()
        advanceUntilIdle()

        viewModel.clearDeleteResult()

        val result = viewModel.deleteResult.value
        assertNull(result)
    }
}
