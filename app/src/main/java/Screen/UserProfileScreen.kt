package Screen

import Data.Task
import Data.TaskPriority
import Data.TaskViewModel
import Data.User
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    user: User,
    onNavigateBack: () -> Unit,
    onNavigateToTaskFilter: (String) -> Unit = {},
    taskViewModel: TaskViewModel = hiltViewModel()
) {
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Observe tasks from ViewModel
    val tasks by taskViewModel.tasks.collectAsState()
    val viewModelLoading by taskViewModel.isLoading.collectAsState()

    // Load tasks when screen opens
    LaunchedEffect(user.id) {
        taskViewModel.loadTasksForUser(user.id)
    }

    // Update loading state
    LaunchedEffect(viewModelLoading) {
        isLoading = viewModelLoading
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Statistics") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Profile Information
            UserInfoCard(user = user)

            // Task Statistics
            if (isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                TaskStatisticsCard(
                    tasks = tasks,
                    onNavigateToTaskFilter = onNavigateToTaskFilter
                )
                TaskPriorityCard(
                    tasks = tasks,
                    onNavigateToTaskFilter = onNavigateToTaskFilter
                )
                TaskStatusCard(
                    tasks = tasks,
                    onNavigateToTaskFilter = onNavigateToTaskFilter
                )
            }
        }
    }
}

@Composable
fun UserInfoCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Profile Information",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Username
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Username",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Username",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = user.username,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Email
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Email",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = user.email,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun TaskStatisticsCard(
    tasks: List<Task>,
    onNavigateToTaskFilter: (String) -> Unit = {}
) {
    val totalTasks = tasks.size
    val completedTasks = tasks.count { it.isCompleted }
    val pendingTasks = totalTasks - completedTasks
    val completionPercentage = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks * 100).toInt() else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Task Overview",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Total Tasks
            StatisticRow(
                icon = Icons.AutoMirrored.Filled.Assignment,
                label = "Total Tasks",
                value = totalTasks.toString(),
                color = MaterialTheme.colorScheme.primary,
                onClick = { onNavigateToTaskFilter("all") }
            )

            // Completed Tasks
            StatisticRow(
                icon = Icons.Default.CheckCircle,
                label = "Completed",
                value = "$completedTasks ($completionPercentage%)",
                color = Color(0xFF4CAF50),
                onClick = { onNavigateToTaskFilter("completed") }
            )

            // Pending Tasks
            StatisticRow(
                icon = Icons.Default.Schedule,
                label = "Pending",
                value = pendingTasks.toString(),
                color = Color(0xFFFF9800),
                onClick = { onNavigateToTaskFilter("pending") }
            )

            // Progress Bar
            if (totalTasks > 0) {
                Column {
                    Text(
                        text = "Completion Progress",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { completedTasks.toFloat() / totalTasks },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$completionPercentage% Complete",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun TaskPriorityCard(
    tasks: List<Task>,
    onNavigateToTaskFilter: (String) -> Unit = {}
) {
    val highPriorityTasks = tasks.count { it.priority == TaskPriority.HIGH }
    val normalPriorityTasks = tasks.count { it.priority == TaskPriority.NORMAL }
    val lowPriorityTasks = tasks.count { it.priority == TaskPriority.LOW }
    val totalTasks = tasks.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Priority Distribution",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // High Priority
            val highPercentage = if (totalTasks > 0) (highPriorityTasks.toFloat() / totalTasks * 100).toInt() else 0
            PriorityRow(
                label = "High Priority",
                count = highPriorityTasks,
                percentage = highPercentage,
                color = Color(0xFFE53E3E),
                onClick = { onNavigateToTaskFilter("high_priority") }
            )

            // Normal Priority
            val normalPercentage = if (totalTasks > 0) (normalPriorityTasks.toFloat() / totalTasks * 100).toInt() else 0
            PriorityRow(
                label = "Normal Priority",
                count = normalPriorityTasks,
                percentage = normalPercentage,
                color = Color(0xFFFF9800),
                onClick = { onNavigateToTaskFilter("normal_priority") }
            )

            // Low Priority
            val lowPercentage = if (totalTasks > 0) (lowPriorityTasks.toFloat() / totalTasks * 100).toInt() else 0
            PriorityRow(
                label = "Low Priority",
                count = lowPriorityTasks,
                percentage = lowPercentage,
                color = Color(0xFF4CAF50),
                onClick = { onNavigateToTaskFilter("low_priority") }
            )
        }
    }
}

@Composable
fun TaskStatusCard(
    tasks: List<Task>,
    onNavigateToTaskFilter: (String) -> Unit = {}
) {
    val completedTasks = tasks.filter { it.isCompleted }
    val pendingTasks = tasks.filter { !it.isCompleted }
    val overdueTasks = pendingTasks.filter { it.dueDate < System.currentTimeMillis() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Task Status Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Completed Tasks
            StatusRow(
                label = "Completed Tasks",
                count = completedTasks.size,
                color = Color(0xFF4CAF50),
                onClick = { onNavigateToTaskFilter("completed") }
            )

            // Pending Tasks
            StatusRow(
                label = "Pending Tasks",
                count = pendingTasks.size,
                color = Color(0xFFFF9800),
                onClick = { onNavigateToTaskFilter("pending") }
            )

            // Overdue Tasks
            StatusRow(
                label = "Overdue Tasks",
                count = overdueTasks.size,
                color = Color(0xFFE53E3E),
                onClick = { onNavigateToTaskFilter("overdue") }
            )

            // Recent Activity
            if (completedTasks.isNotEmpty()) {
                val recentlyCompleted = completedTasks.sortedByDescending { it.dueDate }.take(3)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Recently Completed",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                recentlyCompleted.forEach { task ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = task.taskName,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatisticRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun PriorityRow(
    label: String,
    count: Int,
    percentage: Int,
    color: Color,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PriorityHigh,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$count ($percentage%)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
        if (percentage > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { percentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp),
                color = color
            )
        }
    }
}

@Composable
fun StatusRow(
    label: String,
    count: Int,
    color: Color,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = count.toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
