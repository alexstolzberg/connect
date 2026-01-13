package com.stolz.connect.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.stolz.connect.data.local.ConnectDatabase
import com.stolz.connect.data.mapper.toDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun onReceive(context: Context, intent: Intent) {
        val connectionId = intent.getLongExtra("connection_id", -1)
        val connectionName = intent.getStringExtra("connection_name") ?: "Contact"
        
        if (connectionId > 0) {
            scope.launch {
                // Fetch the connection from the database
                val database = Room.databaseBuilder(
                    context,
                    ConnectDatabase::class.java,
                    "connect_database"
                ).build()
                
                val connectionEntity = database.scheduledConnectionDao().getConnectionById(connectionId)
                connectionEntity?.let {
                    val connection = it.toDomain()
                    // Show the notification
                    NotificationManager.showNotification(context, connection)
                } ?: run {
                    // Fallback if connection not found
                    NotificationManager.showNotification(
                        context,
                        com.stolz.connect.domain.model.ScheduledConnection(
                            id = connectionId,
                            contactName = connectionName,
                            reminderFrequencyDays = 7,
                            preferredMethod = com.stolz.connect.domain.model.ConnectionMethod.BOTH,
                            nextReminderDate = java.util.Date()
                        )
                    )
                }
                
                database.close()
            }
        }
    }
}
