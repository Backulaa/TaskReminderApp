package com.example.taskreminderapp.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        const val CHANNEL_ID = "task_reminder_channel"
        const val CHANNEL_NAME = "Task Reminders"
        const val CHANNEL_DESCRIPTION = "Notifications for upcoming task deadlines"
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = ContextCompat.getSystemService(
                context,
                NotificationManager::class.java
            ) as NotificationManager
            
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun scheduleTaskReminder(taskId: Long, taskName: String, dueDate: Long, reminderMinutesBefore: Int = 60) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Schedule reminder based on user's preference
        val reminderTime = dueDate - (reminderMinutesBefore * 60 * 1000) // Convert minutes to milliseconds
        
        // Only schedule if reminder time is in the future
        if (reminderTime > System.currentTimeMillis()) {
            val intent = Intent(context, TaskReminderReceiver::class.java).apply {
                putExtra("task_name", taskName)
                putExtra("task_id", taskId)
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        reminderTime,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                // Handle permission issues gracefully
                // In this case, we could fall back to a less precise alarm
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )
            }
        }
    }
    
    fun cancelTaskReminder(taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}
