package Data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "task_table",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["user_id"])] // Add index for foreign key
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "task_name") val taskName: String,
    @ColumnInfo(name = "due_date") val dueDate: Long, // Now includes both date and time
    @ColumnInfo(name = "reminder_minutes_before") val reminderMinutesBefore: Int = 60, // Default 1 hour
    @ColumnInfo(name = "priority") val priority: TaskPriority = TaskPriority.NORMAL,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false,
    @ColumnInfo(name = "user_id") val userId: Int // Reference to User table
)

enum class TaskPriority(val displayName: String, val colorName: String) {
    HIGH("High Priority", "Red"),
    NORMAL("Normal Priority", "Orange"), 
    LOW("Low Priority", "Green")
}
