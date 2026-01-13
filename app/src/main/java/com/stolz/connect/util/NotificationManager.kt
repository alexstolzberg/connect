package com.stolz.connect.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.stolz.connect.domain.model.ScheduledConnection
import java.util.Calendar
import java.util.Date

object NotificationManager {
    private const val CHANNEL_ID = "connect_reminders"
    private const val NOTIFICATION_ID_BASE = 1000
    
    fun scheduleNotification(context: Context, connection: ScheduledConnection) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Cancel any existing notification for this connection
        cancelNotification(context, connection.id)
        
        // Only schedule if the reminder date is in the future
        val now = Date()
        if (connection.nextReminderDate.before(now) || connection.nextReminderDate == now) {
            // If it's due today or past, show notification immediately
            showNotification(context, connection)
            return
        }
        
        // Create intent for the notification
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("connection_id", connection.id)
            putExtra("connection_name", connection.contactName)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            connection.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Schedule the alarm
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        connection.nextReminderDate.time,
                        pendingIntent
                    )
                } else {
                    // Fallback to inexact alarm
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        connection.nextReminderDate.time,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    connection.nextReminderDate.time,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationManager", "Failed to schedule notification", e)
        }
    }
    
    fun cancelNotification(context: Context, connectionId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            connectionId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        
        // Also cancel any displayed notification
        NotificationManagerCompat.from(context).cancel(connectionId.toInt())
    }
    
    fun showNotification(context: Context, connection: ScheduledConnection) {
        val notificationManager = NotificationManagerCompat.from(context)
        
        // Create notification channel if needed (for Android O+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                CHANNEL_ID,
                "Connection Reminders",
                android.app.NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to connect with your contacts"
            }
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            systemNotificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Time to connect with ${connection.contactName}")
            .setContentText("Don't forget to reach out!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        try {
            notificationManager.notify(connection.id.toInt(), notification)
        } catch (e: SecurityException) {
            android.util.Log.e("NotificationManager", "Failed to show notification - permission denied", e)
        }
    }
}
