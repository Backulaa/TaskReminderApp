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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

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
    
    // Search and filter states
    var showSearchDialog by remember { mutableStateOf(false) }
    var filterByDate by remember { mutableStateOf<Long?>(null) }
    var filterByPriority by remember { mutableStateOf<TaskPriority?>(null) }
    var filterByCompletion by remember { mutableStateOf<Boolean?>(null) }
    
    // Filtered tasks
    val filteredTasks = remember(tasks, filterByDate, filterByPriority, filterByCompletion) {
        tasks.filter { task ->
            val dateMatch = filterByDate?.let { filterDateMillis ->
                val taskDate = Date(task.dueDate)
                val taskCalendar = Calendar.getInstance()
                taskCalendar.time = taskDate
                val taskDay = taskCalendar.get(Calendar.DAY_OF_YEAR)
                val taskYear = taskCalendar.get(Calendar.YEAR)
                
                val filterDate = Date(filterDateMillis)
                val filterCalendar = Calendar.getInstance()
                filterCalendar.time = filterDate
                val filterDay = filterCalendar.get(Calendar.DAY_OF_YEAR)
                val filterYear = filterCalendar.get(Calendar.YEAR)
                
                taskDay == filterDay && taskYear == filterYear
            } ?: true
            
            val priorityMatch = filterByPriority?.let { it == task.priority } ?: true
            val completionMatch = filterByCompletion?.let { it == task.isCompleted } ?: true
            
            dateMatch && priorityMatch && completionMatch
        }
    }

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
            // Header with Search and Sign Out buttons
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
                        text = "${filteredTasks.size} of ${tasks.size} tasks",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    // Search/Filter button
                    IconButton(onClick = { showSearchDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Tasks",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Sign out button
                    IconButton(onClick = onSignOut) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
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
            } else if (filteredTasks.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (tasks.isEmpty()) "No tasks yet" else "No tasks match your filters",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (tasks.isEmpty()) "Tap the + button to add your first task!" else "Try adjusting your search filters",
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
                    items(filteredTasks) { task ->
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
    
    // Search/Filter Dialog
    if (showSearchDialog) {
        SearchFilterDialog(
            currentDateFilter = filterByDate,
            currentPriorityFilter = filterByPriority,
            currentCompletionFilter = filterByCompletion,
            onDismiss = { showSearchDialog = false },
            onDateFilterChanged = { date ->
                filterByDate = date
            },
            onPriorityFilterChanged = { priority ->
                filterByPriority = priority
            },
            onCompletionFilterChanged = { completion ->
                filterByCompletion = completion
            },
            onClearFilters = {
                filterByDate = null
                filterByPriority = null
                filterByCompletion = null
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFilterDialog(
    currentDateFilter: Long?,
    currentPriorityFilter: TaskPriority?,
    currentCompletionFilter: Boolean?,
    onDismiss: () -> Unit,
    onDateFilterChanged: (Long?) -> Unit,
    onPriorityFilterChanged: (TaskPriority?) -> Unit,
    onCompletionFilterChanged: (Boolean?) -> Unit,
    onClearFilters: () -> Unit
) {
    var showPriorityDropdown by remember { mutableStateOf(false) }
    var showCompletionDropdown by remember { mutableStateOf(false) }
    
    val dateDialogState = rememberMaterialDialogState()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    val completionOptions = listOf(
        Pair(null, "All Tasks"),
        Pair(true, "Completed Tasks"),
        Pair(false, "Pending Tasks")
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Tasks",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    TextButton(onClick = onClearFilters) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Filters",
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Clear All")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Date Filter
                Text(
                    text = "Filter by Due Date",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                OutlinedTextField(
                    value = currentDateFilter?.let { 
                        dateFormatter.format(Date(it))
                    } ?: "All dates",
                    onValueChange = { },
                    label = { Text("Due Date") },
                    readOnly = true,
                    trailingIcon = {
                        Row {
                            if (currentDateFilter != null) {
                                IconButton(
                                    onClick = { onDateFilterChanged(null) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear Date Filter",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            IconButton(onClick = { dateDialogState.show() }) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Select Date"
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Priority Filter
                Text(
                    text = "Filter by Priority",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                ExposedDropdownMenuBox(
                    expanded = showPriorityDropdown,
                    onExpandedChange = { showPriorityDropdown = !showPriorityDropdown }
                ) {
                    OutlinedTextField(
                        value = currentPriorityFilter?.displayName ?: "All priorities",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Priority") },
                        trailingIcon = {
                            Row {
                                if (currentPriorityFilter != null) {
                                    IconButton(
                                        onClick = { onPriorityFilterChanged(null) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear Priority Filter",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select Priority"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = currentPriorityFilter?.let { priority ->
                                when (priority) {
                                    TaskPriority.HIGH -> Color.Red
                                    TaskPriority.NORMAL -> Color(0xFFFF9800)
                                    TaskPriority.LOW -> Color.Green
                                }
                            } ?: MaterialTheme.colorScheme.onSurface
                        )
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showPriorityDropdown,
                        onDismissRequest = { showPriorityDropdown = false }
                    ) {
                        // All priorities option
                        DropdownMenuItem(
                            text = { Text("All priorities") },
                            onClick = {
                                onPriorityFilterChanged(null)
                                showPriorityDropdown = false
                            }
                        )
                        
                        TaskPriority.entries.forEach { priority ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        priority.displayName,
                                        color = when (priority) {
                                            TaskPriority.HIGH -> Color.Red
                                            TaskPriority.NORMAL -> Color(0xFFFF9800)
                                            TaskPriority.LOW -> Color.Green
                                        }
                                    ) 
                                },
                                onClick = {
                                    onPriorityFilterChanged(priority)
                                    showPriorityDropdown = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Completion Status Filter
                Text(
                    text = "Filter by Status",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                ExposedDropdownMenuBox(
                    expanded = showCompletionDropdown,
                    onExpandedChange = { showCompletionDropdown = !showCompletionDropdown }
                ) {
                    OutlinedTextField(
                        value = completionOptions.find { it.first == currentCompletionFilter }?.second ?: "All Tasks",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Completion Status") },
                        trailingIcon = {
                            Row {
                                if (currentCompletionFilter != null) {
                                    IconButton(
                                        onClick = { onCompletionFilterChanged(null) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear Status Filter",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select Status"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showCompletionDropdown,
                        onDismissRequest = { showCompletionDropdown = false }
                    ) {
                        completionOptions.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    onCompletionFilterChanged(value)
                                    showCompletionDropdown = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }
    
    // Date Picker Dialog
    MaterialDialog(
        dialogState = dateDialogState,
        buttons = {
            positiveButton(text = "OK")
            negativeButton(text = "Cancel")
        }
    ) {
        datepicker(
            initialDate = selectedDate,
            title = "Select date to filter by"
        ) { date ->
            selectedDate = date
            val dateInMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            onDateFilterChanged(dateInMillis)
        }
    }
}
