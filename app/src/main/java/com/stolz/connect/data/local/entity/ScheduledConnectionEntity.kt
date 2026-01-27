package com.stolz.connect.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "scheduled_connections")
data class ScheduledConnectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactName: String,
    val contactPhoneNumber: String? = null,
    val contactEmail: String? = null,
    val contactPhotoUri: String? = null, // URI to contact photo
    @ColumnInfo(name = "avatarColor") val avatarColor: Int? = null, // Avatar background color (ARGB int)
    val contactId: String? = null, // Android contact ID if from device contacts
    val reminderFrequencyDays: Int, // e.g., 7 for weekly, 30 for monthly
    val preferredMethod: String, // "call", "message", "email", or "both"
    val reminderTime: String? = null, // Optional time of day (HH:mm format)
    val lastContactedDate: Date? = null,
    val nextReminderDate: Date,
    val notes: String? = null,
    val birthday: Date? = null, // Optional birthday for birthday reminders
    val promptOnBirthday: Boolean = true, // Whether to prompt for connection on birthday
    val createdAt: Date = Date(),
    val isActive: Boolean = true
)
