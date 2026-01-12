package com.stolz.connect.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_contacts")
data class CustomContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val email: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
