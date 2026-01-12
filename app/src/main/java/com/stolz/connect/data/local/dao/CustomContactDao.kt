package com.stolz.connect.data.local.dao

import androidx.room.*
import com.stolz.connect.data.local.entity.CustomContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomContactDao {
    
    @Query("SELECT * FROM custom_contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<CustomContactEntity>>
    
    @Query("SELECT * FROM custom_contacts WHERE id = :id")
    suspend fun getContactById(id: Long): CustomContactEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: CustomContactEntity): Long
    
    @Update
    suspend fun updateContact(contact: CustomContactEntity)
    
    @Delete
    suspend fun deleteContact(contact: CustomContactEntity)
}
