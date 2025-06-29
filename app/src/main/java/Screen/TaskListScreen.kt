package Screen

import Data.Task
import Data.TaskPriority
import Data.TaskViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
fun TaskListScreen(
    userId: Int,
    onSignOut: () -> Unit,
    taskViewModel: TaskViewModel = hiltViewModel()
) {
    val tasks by taskViewModel.tasks.collectAsState()
    val isLoading by taskViewModel.isLoading.collectAsState()
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }

    // Load tasks when the screen is first displayed
    LaunchedEffect(userId) {
        taskViewModel.loadTasksForUser(userId)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header with Sign Out button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Tasks",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${tasks.size} tasks",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = onSignOut) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Sign Out",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (tasks.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No tasks yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap the + button to add your first task!",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Task list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks) { task ->
                        TaskItem(
                            task = task,
                            onTaskClick = { selectedTask = it },
                            onToggleComplete = { taskViewModel.toggleTaskCompletion(it) },
                            formatDate = { taskViewModel.formatDate(it) },
                            formatTime = { taskViewModel.formatTime(it) }
                        )
                    }
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { taskName: String, dueDateTime: Long, reminderMinutes: Int, priority: TaskPriority ->
                taskViewModel.addTask(taskName, dueDateTime, reminderMinutes, priority)
            }
        )
    }

    // Edit Task Dialog
    selectedTask?.let { task ->
        TaskEditDialog(
            task = task,
            onDismiss = { selectedTask = null },
            onUpdateTask = { updatedTask: Task ->
                taskViewModel.updateTask(updatedTask)
            },
            onDeleteTask = { taskToDelete: Task ->
                taskViewModel.deleteTask(taskToDelete)
            }
        )
    }
}

@Composable
fun TaskItem(
    task: Task,
    onTaskClick: (Task) -> Unit,
    onToggleComplete: (Task) -> Unit,
    formatDate: (Long) -> String,
    formatTime: (Long) -> String
) {
    val isOverdue = !task.isCompleted && task.dueDate < System.currentTimeMillis()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTaskClick(task) }
            .alpha(if (task.isCompleted) 0.7f else 1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                task.isCompleted -> MaterialTheme.colorScheme.surfaceVariant
                isOverdue -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Completion toggle
            IconButton(
                onClick = { onToggleComplete(task) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (task.isCompleted) 
                        Icons.Default.CheckCircle 
                    else 
                        Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (task.isCompleted) "Mark as incomplete" else "Mark as complete",
                    tint = if (task.isCompleted) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Task content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = task.taskName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                        color = if (task.isCompleted) 
                            MaterialTheme.colorScheme.onSurfaceVariant 
                        else 
                            MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Priority indicator
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (task.priority) {
                                TaskPriority.HIGH -> Color.Red.copy(alpha = 0.1f)
                                TaskPriority.NORMAL -> Color(0xFFFF9800).copy(alpha = 0.1f)
                                TaskPriority.LOW -> Color.Green.copy(alpha = 0.1f)
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(width = 60.dp, height = 20.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = when (task.priority) {
                                    TaskPriority.HIGH -> "High"
                                    TaskPriority.NORMAL -> "Normal"
                                    TaskPriority.LOW -> "Low"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = when (task.priority) {
                                    TaskPriority.HIGH -> Color.Red
                                    TaskPriority.NORMAL -> Color(0xFFFF9800)
                                    TaskPriority.LOW -> Color.Green
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Due: ${formatDate(task.dueDate)} at ${formatTime(task.dueDate)}",
                        fontSize = 12.sp,
                        color = when {
                            task.isCompleted -> MaterialTheme.colorScheme.onSurfaceVariant
                            isOverdue -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    if (isOverdue) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OVERDUE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                // Reminder info
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Reminder: ${task.reminderMinutesBefore} minutes before",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Status indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = when {
                        task.isCompleted -> MaterialTheme.colorScheme.primary
                        isOverdue -> MaterialTheme.colorScheme.error
                        else -> when (task.priority) {
                            TaskPriority.HIGH -> Color.Red
                            TaskPriority.NORMAL -> Color(0xFFFF9800)
                            TaskPriority.LOW -> Color.Green
                        }
                    },
                    modifier = Modifier.size(8.dp)
                ) {}
            }
        }
    }
}
