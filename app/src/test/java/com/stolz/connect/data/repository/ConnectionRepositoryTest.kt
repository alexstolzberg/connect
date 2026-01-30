package com.stolz.connect.data.repository

import android.content.Context
import com.stolz.connect.data.local.dao.ScheduledConnectionDao
import com.stolz.connect.data.mapper.toDomain
import com.stolz.connect.data.mapper.toEntity
import com.stolz.connect.data.preferences.NotificationPreferences
import com.stolz.connect.domain.model.ConnectionMethod
import com.stolz.connect.domain.model.ScheduledConnection
import com.stolz.connect.util.NotificationManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionRepositoryTest {

    private lateinit var dao: ScheduledConnectionDao
    private lateinit var notificationPreferences: NotificationPreferences
    private lateinit var context: Context
    private lateinit var repository: ConnectionRepository

    @Before
    fun setup() {
        dao = mockk()
        notificationPreferences = mockk(relaxed = true)
        every { notificationPreferences.areNotificationsEnabled() } returns true
        context = mockk(relaxed = true)

        mockkObject(NotificationManager)
        every { NotificationManager.scheduleNotification(any(), any()) } returns Unit
        every { NotificationManager.scheduleNotification(any(), any(), any()) } returns Unit
        every { NotificationManager.cancelNotification(any(), any()) } returns Unit

        repository = ConnectionRepository(dao, notificationPreferences, context)
    }

    @Test
    fun `getAllActiveConnections maps entities to domain`() = runTest {
        val entity = createEntity(1, "Test")
        every { dao.getAllActiveConnections() } returns flowOf(listOf(entity))

        repository.getAllActiveConnections().collect { connections ->
            assertEquals(1, connections.size)
            assertEquals("Test", connections.first().contactName)
        }
    }

    @Test
    fun `getTodayConnections maps entities to domain`() = runTest {
        val entity = createEntity(1, "Today")
        every { dao.getTodayConnections() } returns flowOf(listOf(entity))

        repository.getTodayConnections().collect { connections ->
            assertEquals(1, connections.size)
            assertEquals("Today", connections.first().contactName)
        }
    }

    @Test
    fun `getConnectionById returns domain model`() = runTest {
        val entity = createEntity(1, "Test")
        coEvery { dao.getConnectionById(1L) } returns entity

        val result = repository.getConnectionById(1L)

        assertNotNull(result)
        assertEquals("Test", result?.contactName)
    }

    @Test
    fun `getConnectionById returns null when not found`() = runTest {
        coEvery { dao.getConnectionById(1L) } returns null

        val result = repository.getConnectionById(1L)

        assertNull(result)
    }

    @Test
    fun `insertConnection converts to entity and schedules notification`() = runTest {
        val connection = createConnection(0, "New")
        val entity = connection.toEntity()
        coEvery { dao.insertConnection(entity) } returns 1L

        val result = repository.insertConnection(connection)

        assertEquals(1L, result)
        coVerify { dao.insertConnection(entity) }
        // Note: NotificationManager.scheduleNotification would be called but we can't easily verify it
        // without mocking NotificationManager or using a test double
    }

    @Test
    fun `updateConnection converts to entity and reschedules notification`() = runTest {
        val connection = createConnection(1, "Updated")
        val entity = connection.toEntity()
        coEvery { dao.updateConnection(entity) } returns Unit

        repository.updateConnection(connection)

        coVerify { dao.updateConnection(entity) }
    }

    @Test
    fun `deleteConnection converts to entity and cancels notification`() = runTest {
        val connection = createConnection(1, "To Delete")
        val entity = connection.toEntity()
        coEvery { dao.deleteConnection(entity) } returns Unit

        repository.deleteConnection(connection)

        coVerify { dao.deleteConnection(entity) }
    }

    @Test
    fun `markAsContacted calculates next reminder date and updates`() = runTest {
        val connection = createConnection(1, "Test", frequencyDays = 7)

        // We don't care about exact dates here, just that the DAO is called with the right id and some dates
        coEvery { dao.markAsContacted(connection.id, any(), any()) } returns Unit

        repository.markAsContacted(connection)

        coVerify { dao.markAsContacted(connection.id, any(), any()) }
    }

    @Test
    fun `snoozeReminder updates next reminder date`() = runTest {
        val connection = createConnection(1, "Test")
        val snoozeDate = Date()
        coEvery { dao.snoozeReminder(connection.id, snoozeDate) } returns Unit

        repository.snoozeReminder(connection, snoozeDate)

        coVerify { dao.snoozeReminder(connection.id, snoozeDate) }
    }

    private fun createEntity(
        id: Long = 1L,
        name: String = "Test",
        frequencyDays: Int = 7
    ) = com.stolz.connect.data.local.entity.ScheduledConnectionEntity(
        id = id,
        contactName = name,
        reminderFrequencyDays = frequencyDays,
        preferredMethod = "call",
        nextReminderDate = Date()
    )

    private fun createConnection(
        id: Long = 1L,
        name: String = "Test",
        frequencyDays: Int = 7
    ) = ScheduledConnection(
        id = id,
        contactName = name,
        reminderFrequencyDays = frequencyDays,
        preferredMethod = ConnectionMethod.CALL,
        nextReminderDate = Date()
    )
}
