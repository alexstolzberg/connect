package com.stolz.connect.data.local.dao

import androidx.room.*
import com.stolz.connect.data.local.entity.ScheduledConnectionEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ScheduledConnectionDao {
    
    @Query("SELECT * FROM scheduled_connections WHERE isActive = 1 ORDER BY nextReminderDate ASC")
    fun getAllActiveConnections(): Flow<List<ScheduledConnectionEntity>>
    
    @Query("SELECT * FROM scheduled_connections WHERE id = :id")
    suspend fun getConnectionById(id: Long): ScheduledConnectionEntity?
    
    @Query("""
        SELECT * FROM scheduled_connections 
        WHERE isActive = 1 
        AND date(datetime(nextReminderDate/1000, 'unixepoch', 'localtime')) <= date('now', 'localtime', '+7 days')
        ORDER BY nextReminderDate ASC
    """)
    fun getTodayConnections(): Flow<List<ScheduledConnectionEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnection(connection: ScheduledConnectionEntity): Long
    
    @Update
    suspend fun updateConnection(connection: ScheduledConnectionEntity)
    
    @Delete
    suspend fun deleteConnection(connection: ScheduledConnectionEntity)
    
    @Query("UPDATE scheduled_connections SET lastContactedDate = :date, nextReminderDate = :nextDate WHERE id = :id")
    suspend fun markAsContacted(id: Long, date: Date, nextDate: Date)
    
    @Query("UPDATE scheduled_connections SET nextReminderDate = :nextDate WHERE id = :id")
    suspend fun snoozeReminder(id: Long, nextDate: Date)
}
