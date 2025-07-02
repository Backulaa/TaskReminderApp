package Data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentUserId = MutableStateFlow<Int?>(null)

    fun loadTasksForUser(userId: Int) {
        _currentUserId.value = userId
        Log.d("TaskViewModel", "Loading tasks for user ID: $userId")
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Since we can't use LiveData with StateFlow directly, we'll load tasks manually
                // In a real implementation, you might use Flow from Room
                val userTasks = taskRepository.getTasksForUserSync(userId)
                Log.d("TaskViewModel", "Loaded ${userTasks.size} tasks for user $userId")
                _tasks.value = userTasks
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error loading tasks for user $userId", e)
                _tasks.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addTask(taskName: String, dueDateTime: Long, reminderMinutes: Int = 60, priority: TaskPriority = TaskPriority.NORMAL) {
        val userId = _currentUserId.value ?: return
        Log.d("TaskViewModel", "Adding task '$taskName' for user ID: $userId")
        viewModelScope.launch {
            try {
                val newTask = Task(
                    taskName = taskName,
                    dueDate = dueDateTime,
                    reminderMinutesBefore = reminderMinutes,
                    priority = priority,
                    isCompleted = false,
                    userId = userId
                )
                taskRepository.insert(newTask)
                Log.d("TaskViewModel", "Successfully added task, reloading tasks for user $userId")
                loadTasksForUser(userId) // Refresh the list
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error adding task for user $userId", e)
                // Handle error
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.update(task)
                Log.d("TaskViewModel", "Task updated: ${task.taskName}")
                loadTasksForUser(task.userId) // Refresh the list
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error updating task", e)
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.delete(task)
                Log.d("TaskViewModel", "Task deleted: ${task.taskName}")
                loadTasksForUser(task.userId) // Refresh the list
            } catch (e: Exception) {
                Log.e("TaskViewModel", "Error deleting task", e)
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        val updatedTask = task.copy(isCompleted = !task.isCompleted)
        updateTask(updatedTask)
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
