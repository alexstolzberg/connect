package com.stolz.connect.data.mapper

import com.stolz.connect.data.local.entity.CustomContactEntity
import com.stolz.connect.domain.model.CustomContact
import org.junit.Assert.*
import org.junit.Test

class ContactMapperTest {

    @Test
    fun `toDomain converts entity to domain model correctly`() {
        val entity = CustomContactEntity(
            id = 1L,
            name = "John Doe",
            phoneNumber = "123-456-7890",
            email = "john@example.com",
            notes = "Test notes",
            createdAt = 1000L
        )

        val domain = entity.toDomain()

        assertEquals(1L, domain.id)
        assertEquals("John Doe", domain.name)
        assertEquals("123-456-7890", domain.phoneNumber)
        assertEquals("john@example.com", domain.email)
        assertEquals("Test notes", domain.notes)
        assertEquals(1000L, domain.createdAt)
    }

    @Test
    fun `toEntity converts domain to entity correctly`() {
        val domain = CustomContact(
            id = 2L,
            name = "Jane Smith",
            phoneNumber = "987-654-3210",
            email = "jane@example.com",
            notes = "More notes",
            createdAt = 2000L
        )

        val entity = domain.toEntity()

        assertEquals(2L, entity.id)
        assertEquals("Jane Smith", entity.name)
        assertEquals("987-654-3210", entity.phoneNumber)
        assertEquals("jane@example.com", entity.email)
        assertEquals("More notes", entity.notes)
        assertEquals(2000L, entity.createdAt)
    }

    @Test
    fun `round trip conversion preserves data`() {
        val originalEntity = CustomContactEntity(
            id = 5L,
            name = "Round Trip",
            phoneNumber = "555-1234",
            email = "round@trip.com",
            notes = "Test",
            createdAt = 5000L
        )

        val domain = originalEntity.toDomain()
        val convertedEntity = domain.toEntity()

        assertEquals(originalEntity.id, convertedEntity.id)
        assertEquals(originalEntity.name, convertedEntity.name)
        assertEquals(originalEntity.phoneNumber, convertedEntity.phoneNumber)
        assertEquals(originalEntity.email, convertedEntity.email)
        assertEquals(originalEntity.notes, convertedEntity.notes)
        assertEquals(originalEntity.createdAt, convertedEntity.createdAt)
    }

    @Test
    fun `handles null email correctly`() {
        val entity = CustomContactEntity(
            id = 1L,
            name = "No Email",
            phoneNumber = "123-456-7890",
            email = null,
            notes = null,
            createdAt = 1000L
        )

        val domain = entity.toDomain()
        assertNull(domain.email)
        assertNull(domain.notes)

        val convertedEntity = domain.toEntity()
        assertNull(convertedEntity.email)
        assertNull(convertedEntity.notes)
    }
}
