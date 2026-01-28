package com.stolz.connect.data.mapper

import com.stolz.connect.data.local.entity.ScheduledConnectionEntity
import com.stolz.connect.domain.model.ConnectionMethod
import com.stolz.connect.domain.model.ScheduledConnection
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class ConnectionMapperTest {

    @Test
    fun `toDomain converts entity to domain model correctly`() {
        val entity = ScheduledConnectionEntity(
            id = 1L,
            contactName = "John Doe",
            contactPhoneNumber = "123-456-7890",
            contactEmail = "john@example.com",
            contactPhotoUri = "content://photo",
            avatarColor = 0xFF0000FF.toInt(),
            contactId = "contact123",
            reminderFrequencyDays = 7,
            preferredMethod = "call",
            reminderTime = "09:00",
            lastContactedDate = Date(1000),
            nextReminderDate = Date(2000),
            notes = "Test notes",
            birthday = Date(3000),
            promptOnBirthday = true,
            createdAt = Date(4000),
            isActive = true
        )

        val domain = entity.toDomain()

        assertEquals(1L, domain.id)
        assertEquals("John Doe", domain.contactName)
        assertEquals("123-456-7890", domain.contactPhoneNumber)
        assertEquals("john@example.com", domain.contactEmail)
        assertEquals("content://photo", domain.contactPhotoUri)
        assertEquals(0xFF0000FF.toInt(), domain.avatarColor)
        assertEquals("contact123", domain.contactId)
        assertEquals(7, domain.reminderFrequencyDays)
        assertEquals(ConnectionMethod.CALL, domain.preferredMethod)
        assertEquals("09:00", domain.reminderTime)
        assertEquals(Date(1000), domain.lastContactedDate)
        assertEquals(Date(2000), domain.nextReminderDate)
        assertEquals("Test notes", domain.notes)
        assertEquals(Date(3000), domain.birthday)
        assertTrue(domain.promptOnBirthday)
        assertEquals(Date(4000), domain.createdAt)
        assertTrue(domain.isActive)
    }

    @Test
    fun `toDomain converts all connection methods correctly`() {
        assertEquals(ConnectionMethod.CALL, ScheduledConnectionEntity(preferredMethod = "call", contactName = "Test", reminderFrequencyDays = 7, nextReminderDate = Date()).toDomain().preferredMethod)
        assertEquals(ConnectionMethod.MESSAGE, ScheduledConnectionEntity(preferredMethod = "message", contactName = "Test", reminderFrequencyDays = 7, nextReminderDate = Date()).toDomain().preferredMethod)
        assertEquals(ConnectionMethod.EMAIL, ScheduledConnectionEntity(preferredMethod = "email", contactName = "Test", reminderFrequencyDays = 7, nextReminderDate = Date()).toDomain().preferredMethod)
        assertEquals(ConnectionMethod.BOTH, ScheduledConnectionEntity(preferredMethod = "both", contactName = "Test", reminderFrequencyDays = 7, nextReminderDate = Date()).toDomain().preferredMethod)
        assertEquals(ConnectionMethod.BOTH, ScheduledConnectionEntity(preferredMethod = "unknown", contactName = "Test", reminderFrequencyDays = 7, nextReminderDate = Date()).toDomain().preferredMethod)
    }

    @Test
    fun `toEntity converts domain to entity correctly`() {
        val domain = ScheduledConnection(
            id = 1L,
            contactName = "Jane Smith",
            contactPhoneNumber = "987-654-3210",
            contactEmail = "jane@example.com",
            contactPhotoUri = "content://photo2",
            avatarColor = 0x00FF00FF.toInt(),
            contactId = "contact456",
            reminderFrequencyDays = 14,
            preferredMethod = ConnectionMethod.EMAIL,
            reminderTime = "14:30",
            lastContactedDate = Date(5000),
            nextReminderDate = Date(6000),
            notes = "More notes",
            birthday = Date(7000),
            promptOnBirthday = false,
            createdAt = Date(8000),
            isActive = false
        )

        val entity = domain.toEntity()

        assertEquals(1L, entity.id)
        assertEquals("Jane Smith", entity.contactName)
        assertEquals("987-654-3210", entity.contactPhoneNumber)
        assertEquals("jane@example.com", entity.contactEmail)
        assertEquals("content://photo2", entity.contactPhotoUri)
        assertEquals(0x00FF00FF.toInt(), entity.avatarColor)
        assertEquals("contact456", entity.contactId)
        assertEquals(14, entity.reminderFrequencyDays)
        assertEquals("email", entity.preferredMethod)
        assertEquals("14:30", entity.reminderTime)
        assertEquals(Date(5000), entity.lastContactedDate)
        assertEquals(Date(6000), entity.nextReminderDate)
        assertEquals("More notes", entity.notes)
        assertEquals(Date(7000), entity.birthday)
        assertFalse(entity.promptOnBirthday)
        assertEquals(Date(8000), entity.createdAt)
        assertFalse(entity.isActive)
    }

    @Test
    fun `toEntity converts all connection methods correctly`() {
        assertEquals("call", ScheduledConnection(contactName = "Test", reminderFrequencyDays = 7, preferredMethod = ConnectionMethod.CALL, nextReminderDate = Date()).toEntity().preferredMethod)
        assertEquals("message", ScheduledConnection(contactName = "Test", reminderFrequencyDays = 7, preferredMethod = ConnectionMethod.MESSAGE, nextReminderDate = Date()).toEntity().preferredMethod)
        assertEquals("email", ScheduledConnection(contactName = "Test", reminderFrequencyDays = 7, preferredMethod = ConnectionMethod.EMAIL, nextReminderDate = Date()).toEntity().preferredMethod)
        assertEquals("both", ScheduledConnection(contactName = "Test", reminderFrequencyDays = 7, preferredMethod = ConnectionMethod.BOTH, nextReminderDate = Date()).toEntity().preferredMethod)
    }

    @Test
    fun `round trip conversion preserves data`() {
        val originalEntity = ScheduledConnectionEntity(
            id = 5L,
            contactName = "Round Trip",
            contactPhoneNumber = "555-1234",
            contactEmail = "round@trip.com",
            reminderFrequencyDays = 30,
            preferredMethod = "both",
            nextReminderDate = Date(),
            isActive = true
        )

        val domain = originalEntity.toDomain()
        val convertedEntity = domain.toEntity()

        assertEquals(originalEntity.id, convertedEntity.id)
        assertEquals(originalEntity.contactName, convertedEntity.contactName)
        assertEquals(originalEntity.contactPhoneNumber, convertedEntity.contactPhoneNumber)
        assertEquals(originalEntity.contactEmail, convertedEntity.contactEmail)
        assertEquals(originalEntity.reminderFrequencyDays, convertedEntity.reminderFrequencyDays)
        assertEquals(originalEntity.preferredMethod, convertedEntity.preferredMethod)
        assertEquals(originalEntity.isActive, convertedEntity.isActive)
    }
}
