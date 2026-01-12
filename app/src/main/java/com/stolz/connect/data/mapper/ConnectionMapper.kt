package com.stolz.connect.data.mapper

import com.stolz.connect.data.local.entity.ScheduledConnectionEntity
import com.stolz.connect.domain.model.ConnectionMethod
import com.stolz.connect.domain.model.ScheduledConnection

fun ScheduledConnectionEntity.toDomain(): ScheduledConnection {
    return ScheduledConnection(
        id = id,
        contactName = contactName,
        contactPhoneNumber = contactPhoneNumber,
        contactId = contactId,
        reminderFrequencyDays = reminderFrequencyDays,
        preferredMethod = when (preferredMethod.lowercase()) {
            "call" -> ConnectionMethod.CALL
            "message" -> ConnectionMethod.MESSAGE
            "both" -> ConnectionMethod.BOTH
            else -> ConnectionMethod.BOTH
        },
        reminderTime = reminderTime,
        lastContactedDate = lastContactedDate,
        nextReminderDate = nextReminderDate,
        notes = notes,
        birthday = birthday,
        createdAt = createdAt,
        isActive = isActive
    )
}

fun ScheduledConnection.toEntity(): ScheduledConnectionEntity {
    return ScheduledConnectionEntity(
        id = id,
        contactName = contactName,
        contactPhoneNumber = contactPhoneNumber,
        contactId = contactId,
        reminderFrequencyDays = reminderFrequencyDays,
        preferredMethod = when (preferredMethod) {
            ConnectionMethod.CALL -> "call"
            ConnectionMethod.MESSAGE -> "message"
            ConnectionMethod.BOTH -> "both"
        },
        reminderTime = reminderTime,
        lastContactedDate = lastContactedDate,
        nextReminderDate = nextReminderDate,
        notes = notes,
        birthday = birthday,
        createdAt = createdAt,
        isActive = isActive
    )
}
