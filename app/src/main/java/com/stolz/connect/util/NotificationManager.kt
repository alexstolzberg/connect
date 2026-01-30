package com.stolz.connect.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.stolz.connect.MainActivity
import com.stolz.connect.domain.model.ScheduledConnection
import java.util.Calendar
import java.util.Date

object NotificationManager {
    private const val CHANNEL_ID = "connect_reminders"
    private const val NOTIFICATION_ID_BASE = 1000

    /** Intent extra for opening a specific connection when the app is launched from a notification. */
    const val EXTRA_OPEN_CONNECTION_ID = "com.stolz.connect.OPEN_CONNECTION_ID"
    
    /**
     * Computes the exact alarm time: the reminder date at the user's preferred time of day (reminderTime in HH:mm),
     * or nextReminderDate as-is if no reminderTime is set.
     */
    private fun alarmTimeFor(connection: ScheduledConnection): Long {
        val cal = Calendar.getInstance().apply { time = connection.nextReminderDate }
        val reminderTime = connection.reminderTime
        if (!reminderTime.isNullOrBlank()) {
            val parts = reminderTime.trim().split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 9
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            cal.set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            cal.set(Calendar.MINUTE, minute.coerceIn(0, 59))
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }
    
    /**
     * Schedules a reminder notification for the connection.
     * @param showIfDueNow If true and the reminder time is already past, shows a notification immediately.
     *                     Set to false when creating a new connection so the user doesn't get a notification right after adding.
     */
    fun scheduleNotification(context: Context, connection: ScheduledConnection, showIfDueNow: Boolean = true) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        cancelNotification(context, connection.id)

        val alarmTimeMillis = alarmTimeFor(connection)
        val now = System.currentTimeMillis()

        if (alarmTimeMillis <= now) {
            if (showIfDueNow) {
                showNotification(context, connection)
            }
            return
        }
        
        // Create intent for the alarm (broadcast to NotificationReceiver)
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
        
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        alarmTimeMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmTimeMillis,
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
        
        // Tap opens the app and navigates to this contact's details
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_OPEN_CONNECTION_ID, connection.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            connection.id.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.stolz.connect.R.drawable.ic_notification_bell)
            .setContentTitle("It's time to connect!")
            .setContentText("This is a reminder to connect with ${connection.contactName}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .build()
        
        try {
            notificationManager.notify(connection.id.toInt(), notification)
        } catch (e: SecurityException) {
            android.util.Log.e("NotificationManager", "Failed to show notification - permission denied", e)
        }
    }
}
