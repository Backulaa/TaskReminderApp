package com.example.taskreminderapp.notification

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.example.taskreminderapp.MainActivity
import com.example.taskreminderapp.R

class TaskReminderReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val taskName = intent.getStringExtra("task_name") ?: "Task"
        val taskId = intent.getLongExtra("task_id", -1)
        
        showNotification(context, taskName, taskId)
    }
    
    private fun showNotification(context: Context, taskName: String, taskId: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create intent to open the app when notification is tapped
        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Task Reminder")
            .setContentText("Don't forget: $taskName")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Your task '$taskName' is due soon. Don't forget to complete it!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(taskId.toInt(), notification)
    }
}
