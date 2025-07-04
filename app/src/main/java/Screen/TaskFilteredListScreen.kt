package Screen

import Data.Task
import Data.TaskPriority
import Data.TaskViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFilteredListScreen(
    userId: Int,
    filterType: String,
    filterValue: String? = null,
    onNavigateBack: () -> Unit,
    taskViewModel: TaskViewModel = hiltViewModel()
) {
    val tasks by taskViewModel.tasks.collectAsState()
    val isLoading by taskViewModel.isLoading.collectAsState()

    // Load tasks when screen opens
    LaunchedEffect(userId) {
        taskViewModel.loadTasksForUser(userId)
    }

    // Filter tasks based on the filter type
    val filteredTasks = remember(tasks, filterType, filterValue) {
        when (filterType) {
            "completed" -> tasks.filter { it.isCompleted }
            "pending" -> tasks.filter { !it.isCompleted }
            "overdue" -> tasks.filter { !it.isCompleted && it.dueDate < System.currentTimeMillis() }
            "high_priority" -> tasks.filter { it.priority == TaskPriority.HIGH }
            "normal_priority" -> tasks.filter { it.priority == TaskPriority.NORMAL }
            "low_priority" -> tasks.filter { it.priority == TaskPriority.LOW }
            "all" -> tasks
            else -> tasks
        }
    }

    val title = when (filterType) {
        "completed" -> "Completed Tasks"
        "pending" -> "Pending Tasks"
        "overdue" -> "Overdue Tasks"
        "high_priority" -> "High Priority Tasks"
        "normal_priority" -> "Normal Priority Tasks"
        "low_priority" -> "Low Priority Tasks"
        "all" -> "All Tasks"
        else -> "Tasks"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title)
                        Text(
                            text = "${filteredTasks.size} tasks",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No tasks found",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = getEmptyStateMessage(filterType),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(filteredTasks) { task ->
                    TaskCard(
                        task = task,
                        onToggleCompletion = { taskViewModel.toggleTaskCompletion(task) },
                        onDelete = { taskViewModel.deleteTask(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Completion checkbox
            IconButton(
                onClick = onToggleCompletion,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (task.isCompleted) "Mark as incomplete" else "Mark as complete",
                    tint = if (task.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Task content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.taskName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.alpha(if (task.isCompleted) 0.6f else 1f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Due date
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                    Text(
                        text = dateFormat.format(Date(task.dueDate)),
                        fontSize = 12.sp,
                        color = if (task.dueDate < System.currentTimeMillis() && !task.isCompleted)
                            Color(0xFFE53E3E) else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Priority indicator
                    val priorityColor = when (task.priority) {
                        TaskPriority.HIGH -> Color(0xFFE53E3E)
                        TaskPriority.NORMAL -> Color(0xFFFF9800)
                        TaskPriority.LOW -> Color(0xFF4CAF50)
                    }

                    Surface(
                        color = priorityColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = task.priority.displayName,
                            fontSize = 10.sp,
                            color = priorityColor,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Overdue indicator
                    if (task.dueDate < System.currentTimeMillis() && !task.isCompleted) {
                        Surface(
                            color = Color(0xFFE53E3E).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "OVERDUE",
                                fontSize = 10.sp,
                                color = Color(0xFFE53E3E),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getEmptyStateMessage(filterType: String): String {
    return when (filterType) {
        "completed" -> "No completed tasks yet. Complete some tasks to see them here!"
        "pending" -> "No pending tasks. You're all caught up!"
        "overdue" -> "No overdue tasks. Great job staying on schedule!"
        "high_priority" -> "No high priority tasks at the moment."
        "normal_priority" -> "No normal priority tasks at the moment."
        "low_priority" -> "No low priority tasks at the moment."
        else -> "No tasks found for this filter."
    }
}
