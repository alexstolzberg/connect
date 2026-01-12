package com.stolz.connect.data.repository

import com.stolz.connect.data.local.dao.CustomContactDao
import com.stolz.connect.data.mapper.toDomain
import com.stolz.connect.data.mapper.toEntity
import com.stolz.connect.domain.model.CustomContact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(
    private val contactDao: CustomContactDao
) {
    
    fun getAllContacts(): Flow<List<CustomContact>> {
        return contactDao.getAllContacts().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun getContactById(id: Long): CustomContact? {
        return contactDao.getContactById(id)?.toDomain()
    }
    
    suspend fun insertContact(contact: CustomContact): Long {
        return contactDao.insertContact(contact.toEntity())
    }
    
    suspend fun updateContact(contact: CustomContact) {
        contactDao.updateContact(contact.toEntity())
    }
    
    suspend fun deleteContact(contact: CustomContact) {
        contactDao.deleteContact(contact.toEntity())
    }
}
