package com.stolz.connect.data.local.dao

import androidx.room.Room
import com.stolz.connect.data.local.ConnectDatabase
import com.stolz.connect.data.local.entity.ScheduledConnectionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@OptIn(ExperimentalCoroutinesApi::class)
class ScheduledConnectionDaoTest {

    private lateinit var database: ConnectDatabase
    private lateinit var dao: ScheduledConnectionDao

    @Before
    fun setup() {
        // Use Robolectric to get application context
        val context = RuntimeEnvironment.getApplication()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ConnectDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.scheduledConnectionDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insertConnection returns generated id`() = runTest {
        val entity = createEntity(name = "Test Connection")
        val id = dao.insertConnection(entity)
        
        assertTrue(id > 0)
    }

    @Test
    fun `getConnectionById returns inserted connection`() = runTest {
        val entity = createEntity(name = "Test")
        val id = dao.insertConnection(entity)
        
        val retrieved = dao.getConnectionById(id)
        
        assertNotNull(retrieved)
        assertEquals("Test", retrieved?.contactName)
        assertEquals(id, retrieved?.id)
    }

    @Test
    fun `getAllActiveConnections returns only active connections`() = runTest {
        val active1 = createEntity(name = "Active 1", isActive = true)
        val active2 = createEntity(name = "Active 2", isActive = true)
        val inactive = createEntity(name = "Inactive", isActive = false)
        
        dao.insertConnection(active1)
        dao.insertConnection(active2)
        dao.insertConnection(inactive)
        
        val connections = dao.getAllActiveConnections().first()
        
        assertEquals(2, connections.size)
        assertTrue(connections.all { it.isActive })
        assertTrue(connections.any { it.contactName == "Active 1" })
        assertTrue(connections.any { it.contactName == "Active 2" })
    }

    @Test
    fun `getAllActiveConnections orders by nextReminderDate`() = runTest {
        val now = Date()
        val tomorrow = Calendar.getInstance().apply {
            time = now
            add(Calendar.DAY_OF_MONTH, 1)
        }.time
        val nextWeek = Calendar.getInstance().apply {
            time = now
            add(Calendar.DAY_OF_MONTH, 7)
        }.time
        
        val later = createEntity(name = "Later", nextDate = nextWeek)
        val earlier = createEntity(name = "Earlier", nextDate = tomorrow)
        
        dao.insertConnection(later)
        dao.insertConnection(earlier)
        
        val connections = dao.getAllActiveConnections().first()
        
        assertEquals(2, connections.size)
        assertEquals("Earlier", connections[0].contactName)
        assertEquals("Later", connections[1].contactName)
    }

    @Test
    fun `updateConnection modifies existing connection`() = runTest {
        val entity = createEntity(name = "Original")
        val id = dao.insertConnection(entity)
        
        val updated = entity.copy(id = id, contactName = "Updated")
        dao.updateConnection(updated)
        
        val retrieved = dao.getConnectionById(id)
        assertEquals("Updated", retrieved?.contactName)
    }

    @Test
    fun `deleteConnection removes connection`() = runTest {
        val entity = createEntity(name = "To Delete")
        val id = dao.insertConnection(entity)
        
        dao.deleteConnection(entity.copy(id = id))
        
        val retrieved = dao.getConnectionById(id)
        assertNull(retrieved)
    }

    @Test
    fun `markAsContacted updates dates`() = runTest {
        val entity = createEntity(name = "Test")
        val id = dao.insertConnection(entity)
        
        val now = Date()
        val nextDate = Calendar.getInstance().apply {
            time = now
            add(Calendar.DAY_OF_MONTH, 7)
        }.time
        
        dao.markAsContacted(id, now, nextDate)
        
        val retrieved = dao.getConnectionById(id)
        assertNotNull(retrieved?.lastContactedDate)
        assertEquals(now.time / 1000, retrieved?.lastContactedDate?.time?.div(1000))
        assertEquals(nextDate.time / 1000, retrieved?.nextReminderDate?.time?.div(1000))
    }

    @Test
    fun `getTodayConnections returns connections due within 7 days`() = runTest {
        val now = Date()
        val yesterday = Calendar.getInstance().apply {
            time = now
            add(Calendar.DAY_OF_MONTH, -1)
        }.time
        val tomorrow = Calendar.getInstance().apply {
            time = now
            add(Calendar.DAY_OF_MONTH, 1)
        }.time
        val nextWeek = Calendar.getInstance().apply {
            time = now
            add(Calendar.DAY_OF_MONTH, 7)
        }.time
        val farFuture = Calendar.getInstance().apply {
            time = now
            add(Calendar.DAY_OF_MONTH, 14)
        }.time
        
        val pastDue = createEntity(name = "Past Due", nextDate = yesterday)
        val today = createEntity(name = "Today", nextDate = now)
        val tomorrowConn = createEntity(name = "Tomorrow", nextDate = tomorrow)
        val nextWeekConn = createEntity(name = "Next Week", nextDate = nextWeek)
        val farFutureConn = createEntity(name = "Far Future", nextDate = farFuture)
        
        dao.insertConnection(pastDue)
        dao.insertConnection(today)
        dao.insertConnection(tomorrowConn)
        dao.insertConnection(nextWeekConn)
        dao.insertConnection(farFutureConn)
        
        val connections = dao.getTodayConnections().first()
        
        // Should include past due, today, tomorrow, and next week (within 7 days)
        assertTrue(connections.size >= 4)
        assertTrue(connections.any { it.contactName == "Past Due" })
        assertTrue(connections.any { it.contactName == "Today" })
        assertTrue(connections.any { it.contactName == "Tomorrow" })
        assertTrue(connections.any { it.contactName == "Next Week" })
        assertFalse(connections.any { it.contactName == "Far Future" })
    }

    @Test
    fun `snoozeReminder updates next reminder date`() = runTest {
        val entity = createEntity(name = "Test")
        val id = dao.insertConnection(entity)
        
        val snoozeDate = Calendar.getInstance().apply {
            time = Date()
            add(Calendar.DAY_OF_MONTH, 3)
        }.time
        
        dao.snoozeReminder(id, snoozeDate)
        
        val retrieved = dao.getConnectionById(id)
        assertEquals(snoozeDate.time / 1000, retrieved?.nextReminderDate?.time?.div(1000))
    }

    private fun createEntity(
        name: String = "Test",
        nextDate: Date = Date(),
        isActive: Boolean = true
    ) = ScheduledConnectionEntity(
        contactName = name,
        reminderFrequencyDays = 7,
        preferredMethod = "call",
        nextReminderDate = nextDate,
        isActive = isActive
    )
}
