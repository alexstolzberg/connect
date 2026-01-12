package com.stolz.connect.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "scheduled_connections")
data class ScheduledConnectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val contactName: String,
    val contactPhoneNumber: String,
    val contactId: String? = null, // Android contact ID if from device contacts
    val reminderFrequencyDays: Int, // e.g., 7 for weekly, 30 for monthly
    val preferredMethod: String, // "call", "message", or "both"
    val reminderTime: String? = null, // Optional time of day (HH:mm format)
    val lastContactedDate: Date? = null,
    val nextReminderDate: Date,
    val notes: String? = null,
    val birthday: Date? = null, // Optional birthday for birthday reminders
    val createdAt: Date = Date(),
    val isActive: Boolean = true
)
