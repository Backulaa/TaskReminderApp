package Screen

import Data.Task
import Data.TaskPriority
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditDialog(
    task: Task,
    onDismiss: () -> Unit,
    onUpdateTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    var taskName by remember { mutableStateOf(task.taskName) }
    
    val dueDateTime = Instant.ofEpochMilli(task.dueDate).atZone(ZoneId.systemDefault())
    var selectedDate by remember { mutableStateOf(dueDateTime.toLocalDate()) }
    var selectedTime by remember { mutableStateOf(dueDateTime.toLocalTime()) }
    var selectedReminderMinutes by remember { mutableStateOf(task.reminderMinutesBefore) }
    var selectedPriority by remember { mutableStateOf(task.priority) }
    var isCompleted by remember { mutableStateOf(task.isCompleted) }
    
    var showReminderDropdown by remember { mutableStateOf(false) }
    var showPriorityDropdown by remember { mutableStateOf(false) }
    
    val dateDialogState = rememberMaterialDialogState()
    val timeDialogState = rememberMaterialDialogState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Reminder options
    val reminderOptions = listOf(
        Pair(15, "15 minutes before"),
        Pair(30, "30 minutes before"),
        Pair(60, "1 hour before"),
        Pair(120, "2 hours before"),
        Pair(1440, "1 day before"),
        Pair(2880, "2 days before")
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Task",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    IconButton(
                        onClick = { showDeleteConfirmation = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Task",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Task Name Input
                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("Task Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date and Time Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Due Date Picker
                    OutlinedTextField(
                        value = dateFormatter.format(Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant())),
                        onValueChange = { },
                        label = { Text("Due Date") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { dateDialogState.show() }) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Select Date"
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    // Due Time Picker
                    OutlinedTextField(
                        value = timeFormatter.format(Date.from(selectedTime.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant())),
                        onValueChange = { },
                        label = { Text("Due Time") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { timeDialogState.show() }) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "Select Time"
                                )
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Priority Selection
                ExposedDropdownMenuBox(
                    expanded = showPriorityDropdown,
                    onExpandedChange = { showPriorityDropdown = !showPriorityDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedPriority.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Priority") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Priority"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = when (selectedPriority) {
                                TaskPriority.HIGH -> Color.Red
                                TaskPriority.NORMAL -> Color(0xFFFF9800) // Orange
                                TaskPriority.LOW -> Color.Green
                            }
                        )
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showPriorityDropdown,
                        onDismissRequest = { showPriorityDropdown = false }
                    ) {
                        TaskPriority.values().forEach { priority ->
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
                                    selectedPriority = priority
                                    showPriorityDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reminder Time Selection
                ExposedDropdownMenuBox(
                    expanded = showReminderDropdown,
                    onExpandedChange = { showReminderDropdown = !showReminderDropdown }
                ) {
                    OutlinedTextField(
                        value = reminderOptions.find { it.first == selectedReminderMinutes }?.second ?: "Custom",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Remind me") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Reminder"
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Reminder Time"
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showReminderDropdown,
                        onDismissRequest = { showReminderDropdown = false }
                    ) {
                        reminderOptions.forEach { (minutes, description) ->
                            DropdownMenuItem(
                                text = { Text(description) },
                                onClick = {
                                    selectedReminderMinutes = minutes
                                    showReminderDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Completion Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { isCompleted = it }
                    )
                    Text(
                        text = if (isCompleted) "Task Completed" else "Mark as Complete",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (taskName.isNotBlank()) {
                                val dueDateTime = selectedDate.atTime(selectedTime)
                                    .atZone(ZoneId.systemDefault())
                                    .toInstant()
                                    .toEpochMilli()
                                
                                val updatedTask = task.copy(
                                    taskName = taskName,
                                    dueDate = dueDateTime,
                                    reminderMinutesBefore = selectedReminderMinutes,
                                    priority = selectedPriority,
                                    isCompleted = isCompleted
                                )
                                onUpdateTask(updatedTask)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = taskName.isNotBlank()
                    ) {
                        Text("Update")
                    }
                }
            }
        }
    }

    // Material Date Picker Dialog
    MaterialDialog(
        dialogState = dateDialogState,
        buttons = {
            positiveButton(text = "OK")
            negativeButton(text = "Cancel")
        }
    ) {
        datepicker(
            initialDate = selectedDate,
            title = "Pick a date",
            allowedDateValidator = { date ->
                date >= LocalDate.now()
            }
        ) {
            selectedDate = it
        }
    }

    // Material Time Picker Dialog
    MaterialDialog(
        dialogState = timeDialogState,
        buttons = {
            positiveButton(text = "OK")
            negativeButton(text = "Cancel")
        }
    ) {
        timepicker(
            initialTime = selectedTime,
            title = "Pick a time"
        ) {
            selectedTime = it
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete this task? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTask(task)
                        showDeleteConfirmation = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
