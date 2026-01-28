package com.stolz.connect.data.local.dao

import androidx.room.Room
import com.stolz.connect.data.local.ConnectDatabase
import com.stolz.connect.data.local.entity.CustomContactEntity
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

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@OptIn(ExperimentalCoroutinesApi::class)
class CustomContactDaoTest {

    private lateinit var database: ConnectDatabase
    private lateinit var dao: CustomContactDao

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        database = Room.inMemoryDatabaseBuilder(
            context,
            ConnectDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.customContactDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `insertContact returns generated id`() = runTest {
        val entity = createEntity(name = "Test Contact")
        val id = dao.insertContact(entity)
        
        assertTrue(id > 0)
    }

    @Test
    fun `getContactById returns inserted contact`() = runTest {
        val entity = createEntity(name = "Test")
        val id = dao.insertContact(entity)
        
        val retrieved = dao.getContactById(id)
        
        assertNotNull(retrieved)
        assertEquals("Test", retrieved?.name)
        assertEquals(id, retrieved?.id)
    }

    @Test
    fun `getAllContacts returns all contacts ordered by name`() = runTest {
        val contact1 = createEntity(name = "Zebra")
        val contact2 = createEntity(name = "Alpha")
        val contact3 = createEntity(name = "Beta")
        
        dao.insertContact(contact1)
        dao.insertContact(contact2)
        dao.insertContact(contact3)
        
        val contacts = dao.getAllContacts().first()
        
        assertEquals(3, contacts.size)
        assertEquals("Alpha", contacts[0].name)
        assertEquals("Beta", contacts[1].name)
        assertEquals("Zebra", contacts[2].name)
    }

    @Test
    fun `updateContact modifies existing contact`() = runTest {
        val entity = createEntity(name = "Original", phone = "123-456-7890")
        val id = dao.insertContact(entity)
        
        val updated = entity.copy(id = id, name = "Updated", phoneNumber = "987-654-3210")
        dao.updateContact(updated)
        
        val retrieved = dao.getContactById(id)
        assertEquals("Updated", retrieved?.name)
        assertEquals("987-654-3210", retrieved?.phoneNumber)
    }

    @Test
    fun `deleteContact removes contact`() = runTest {
        val entity = createEntity(name = "To Delete")
        val id = dao.insertContact(entity)
        
        dao.deleteContact(entity.copy(id = id))
        
        val retrieved = dao.getContactById(id)
        assertNull(retrieved)
    }

    @Test
    fun `insertContact handles null email and notes`() = runTest {
        val entity = createEntity(name = "Test", email = null, notes = null)
        val id = dao.insertContact(entity)
        
        val retrieved = dao.getContactById(id)
        assertNotNull(retrieved)
        assertNull(retrieved?.email)
        assertNull(retrieved?.notes)
    }

    @Test
    fun `insertContact with onConflict replaces existing`() = runTest {
        val entity = createEntity(name = "Test", phone = "123-456-7890")
        val id = dao.insertContact(entity)
        
        val updated = entity.copy(id = id, name = "Updated")
        dao.insertContact(updated)
        
        val retrieved = dao.getContactById(id)
        assertEquals("Updated", retrieved?.name)
    }

    private fun createEntity(
        name: String = "Test",
        phone: String = "123-456-7890",
        email: String? = "test@example.com",
        notes: String? = "Test notes"
    ) = CustomContactEntity(
        name = name,
        phoneNumber = phone,
        email = email,
        notes = notes
    )
}
