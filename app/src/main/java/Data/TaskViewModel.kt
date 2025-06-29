package Data

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
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Since we can't use LiveData with StateFlow directly, we'll load tasks manually
                // In a real implementation, you might use Flow from Room
                val userTasks = taskRepository.getTasksForUserSync(userId)
                _tasks.value = userTasks
            } catch (e: Exception) {
                _tasks.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addTask(taskName: String, dueDateTime: Long, reminderMinutes: Int = 60, priority: TaskPriority = TaskPriority.NORMAL) {
        val userId = _currentUserId.value ?: return
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
                loadTasksForUser(userId) // Refresh the list
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.update(task)
                loadTasksForUser(task.userId) // Refresh the list
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            try {
                taskRepository.delete(task)
                loadTasksForUser(task.userId) // Refresh the list
            } catch (e: Exception) {
                // Handle error
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
