package com.stolz.connect.data.repository

import android.content.Context
import com.stolz.connect.data.local.dao.ScheduledConnectionDao
import com.stolz.connect.data.mapper.toDomain
import com.stolz.connect.data.mapper.toEntity
import com.stolz.connect.domain.model.ScheduledConnection
import com.stolz.connect.util.NotificationManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class ConnectionRepository @Inject constructor(
    private val connectionDao: ScheduledConnectionDao,
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
    
    suspend fun insertConnection(connection: ScheduledConnection): Long {
        val entity = connection.toEntity()
        val insertedId = connectionDao.insertConnection(entity)
        
        // Schedule notification for the connection
        val connectionWithId = connection.copy(id = insertedId)
        NotificationManager.scheduleNotification(context, connectionWithId)
        
        return insertedId
    }
    
    suspend fun updateConnection(connection: ScheduledConnection) {
        connectionDao.updateConnection(connection.toEntity())
        // Reschedule notification for the updated connection
        NotificationManager.scheduleNotification(context, connection)
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
        
        // Reschedule notification for the next reminder date
        val updatedConnection = connection.copy(
            lastContactedDate = now,
            nextReminderDate = nextReminderDate
        )
        NotificationManager.scheduleNotification(context, updatedConnection)
    }
    
    suspend fun snoozeReminder(connection: ScheduledConnection, snoozeDate: Date) {
        connectionDao.snoozeReminder(connection.id, snoozeDate)
        
        // Reschedule notification for the snooze date
        val updatedConnection = connection.copy(
            nextReminderDate = snoozeDate
        )
        NotificationManager.scheduleNotification(context, updatedConnection)
    }
}
