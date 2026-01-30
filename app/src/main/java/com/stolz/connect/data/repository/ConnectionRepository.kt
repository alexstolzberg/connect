package com.stolz.connect.data.repository

import android.content.Context
import com.stolz.connect.data.local.dao.ScheduledConnectionDao
import com.stolz.connect.data.mapper.toDomain
import com.stolz.connect.data.mapper.toEntity
import com.stolz.connect.data.preferences.NotificationPreferences
import com.stolz.connect.domain.model.ScheduledConnection
import com.stolz.connect.util.NotificationManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class ConnectionRepository @Inject constructor(
    private val connectionDao: ScheduledConnectionDao,
    private val notificationPreferences: NotificationPreferences,
    @ApplicationContext private val context: Context
) {
    
    fun getAllActiveConnections(): Flow<List<ScheduledConnection>> {
        return connectionDao.getAllActiveConnections().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    fun getTodayConnections(): Flow<List<ScheduledConnection>> {
        return connectionDao.getTodayConnections().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    suspend fun getConnectionById(id: Long): ScheduledConnection? {
        return connectionDao.getConnectionById(id)?.toDomain()
    }

    /**
     * Returns connections that match the given name, phone, or email (for duplicate check).
     * @param excludeId If set (e.g. when editing), connections with this id are excluded from results.
     */
    suspend fun findPotentialDuplicates(
        name: String?,
        phone: String?,
        email: String?,
        excludeId: Long? = null
    ): List<ScheduledConnection> {
        val all = connectionDao.getAllActiveConnections().first().map { it.toDomain() }
        val nameNorm = name?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
        val phoneNorm = phone?.replace(Regex("[^\\d]"), "")?.takeIf { it.isNotBlank() }
        val emailNorm = email?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
        if (nameNorm == null && phoneNorm == null && emailNorm == null) return emptyList()
        return all.filter { existing ->
            (excludeId != null && existing.id == excludeId) == false &&
            ((nameNorm != null && existing.contactName.trim().lowercase() == nameNorm) ||
             (phoneNorm != null && existing.contactPhoneNumber?.replace(Regex("[^\\d]"), "")?.let { it == phoneNorm } == true) ||
             (emailNorm != null && existing.contactEmail?.trim()?.lowercase() == emailNorm))
        }
    }

    suspend fun insertConnection(connection: ScheduledConnection): Long {
        val entity = connection.toEntity()
        val insertedId = connectionDao.insertConnection(entity)
        
        val connectionWithId = connection.copy(id = insertedId)
        if (notificationPreferences.areNotificationsEnabled()) {
            NotificationManager.scheduleNotification(context, connectionWithId, showIfDueNow = false, defaultReminderTime = notificationPreferences.getDefaultReminderTime())
        }
        return insertedId
    }

    suspend fun updateConnection(connection: ScheduledConnection) {
        connectionDao.updateConnection(connection.toEntity())
        if (notificationPreferences.areNotificationsEnabled()) {
            NotificationManager.scheduleNotification(context, connection, defaultReminderTime = notificationPreferences.getDefaultReminderTime())
        }
    }
    
    suspend fun deleteConnection(connection: ScheduledConnection) {
        connectionDao.deleteConnection(connection.toEntity())
        // Cancel notification for deleted connection
        NotificationManager.cancelNotification(context, connection.id)
    }
    
    suspend fun markAsContacted(connection: ScheduledConnection) {
        val now = Date()
        val calendar = Calendar.getInstance().apply {
            time = now
            add(Calendar.DAY_OF_MONTH, connection.reminderFrequencyDays)
        }
        val nextReminderDate = calendar.time
        
        connectionDao.markAsContacted(connection.id, now, nextReminderDate)
        
        val updatedConnection = connection.copy(
            lastContactedDate = now,
            nextReminderDate = nextReminderDate
        )
        if (notificationPreferences.areNotificationsEnabled()) {
            NotificationManager.scheduleNotification(context, updatedConnection, defaultReminderTime = notificationPreferences.getDefaultReminderTime())
        }
    }

    suspend fun snoozeReminder(connection: ScheduledConnection, snoozeDate: Date) {
        connectionDao.snoozeReminder(connection.id, snoozeDate)
        val updatedConnection = connection.copy(nextReminderDate = snoozeDate)
        if (notificationPreferences.areNotificationsEnabled()) {
            NotificationManager.scheduleNotification(context, updatedConnection, defaultReminderTime = notificationPreferences.getDefaultReminderTime())
        }
    }

    /** Cancels all scheduled reminder notifications (e.g. when user turns notifications off). */
    suspend fun cancelAllScheduledNotifications() {
        connectionDao.getAllActiveConnections().first().forEach { entity ->
            NotificationManager.cancelNotification(context, entity.id)
        }
    }

    /** Reschedules notifications for all active connections (e.g. when user turns notifications on). */
    suspend fun rescheduleAllNotifications() {
        if (!notificationPreferences.areNotificationsEnabled()) return
        val defaultTime = notificationPreferences.getDefaultReminderTime()
        connectionDao.getAllActiveConnections().first().forEach { entity ->
            NotificationManager.scheduleNotification(context, entity.toDomain(), defaultReminderTime = defaultTime)
        }
    }
}
