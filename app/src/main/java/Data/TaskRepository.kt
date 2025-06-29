package Data

import androidx.lifecycle.LiveData
import com.example.taskreminderapp.notification.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val notificationHelper: NotificationHelper
) {

    fun getTasksForUser(userId: Int): LiveData<List<Task>> {
        return taskDao.getTasksForUser(userId)
    }

    suspend fun getTasksForUserSync(userId: Int): List<Task> {
        return withContext(Dispatchers.IO) {
            taskDao.getTasksForUserSync(userId)
        }
    }

    suspend fun insert(task: Task): Long {
        return withContext(Dispatchers.IO) {
            val taskId = taskDao.insert(task)
            
            // Schedule notification for the task
            if (!task.isCompleted) {
                notificationHelper.scheduleTaskReminder(taskId, task.taskName, task.dueDate, task.reminderMinutesBefore)
            }
            
            taskId
        }
    }

    suspend fun update(task: Task) {
        withContext(Dispatchers.IO) {
            taskDao.update(task)
            
            // Update notification
            if (task.isCompleted) {
                // Cancel notification if task is completed
                notificationHelper.cancelTaskReminder(task.id.toLong())
            } else {
                // Reschedule notification if task is updated but not completed
                notificationHelper.cancelTaskReminder(task.id.toLong())
                notificationHelper.scheduleTaskReminder(task.id.toLong(), task.taskName, task.dueDate, task.reminderMinutesBefore)
            }
        }
    }

    suspend fun delete(task: Task) {
        withContext(Dispatchers.IO) {
            taskDao.delete(task)
            
            // Cancel notification when task is deleted
            notificationHelper.cancelTaskReminder(task.id.toLong())
        }
    }

    suspend fun getTaskById(id: Int): Task? {
        return withContext(Dispatchers.IO) {
            taskDao.getTaskById(id)
        }
    }
}
