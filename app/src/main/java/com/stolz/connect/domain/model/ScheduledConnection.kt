package com.stolz.connect.domain.model

import java.util.Date

data class ScheduledConnection(
    val id: Long = 0,
    val contactName: String,
    val contactPhoneNumber: String? = null,
    val contactEmail: String? = null,
    val contactPhotoUri: String? = null, // URI to contact photo
    val contactId: String? = null,
    val reminderFrequencyDays: Int,
    val preferredMethod: ConnectionMethod,
    val reminderTime: String? = null,
    val lastContactedDate: Date? = null,
    val nextReminderDate: Date,
    val notes: String? = null,
    val birthday: Date? = null, // Optional birthday for birthday reminders
    val promptOnBirthday: Boolean = true, // Whether to prompt for connection on birthday
    val createdAt: Date = Date(),
    val isActive: Boolean = true
) {
    val isDueToday: Boolean
        get() {
            val today = Date()
            val calendar = java.util.Calendar.getInstance().apply {
                time = today
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val todayStart = calendar.time
            
            val reminderCalendar = java.util.Calendar.getInstance().apply {
                time = nextReminderDate
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val reminderStart = reminderCalendar.time
            
            return reminderStart == todayStart || reminderStart.before(todayStart)
        }
    
    val isPastDue: Boolean
        get() {
            val today = Date()
            val calendar = java.util.Calendar.getInstance().apply {
                time = today
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val todayStart = calendar.time
            
            val reminderCalendar = java.util.Calendar.getInstance().apply {
                time = nextReminderDate
                set(java.util.Calendar.HOUR_OF_DAY, 0)
                set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            val reminderStart = reminderCalendar.time
            
            return reminderStart.before(todayStart)
        }
}

enum class ConnectionMethod {
    CALL,
    MESSAGE,
    EMAIL,
    BOTH
}
