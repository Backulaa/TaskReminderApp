package Data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class Converters {
    @TypeConverter
    fun fromTaskPriority(priority: TaskPriority): String {
        return priority.name
    }

    @TypeConverter
    fun toTaskPriority(priorityString: String): TaskPriority {
        return TaskPriority.valueOf(priorityString)
    }
}

@Database(entities = [User::class, Task::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: TaskDatabase? = null

        // Migration from version 1 to 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns with default values
                db.execSQL("ALTER TABLE task_table ADD COLUMN reminder_minutes_before INTEGER NOT NULL DEFAULT 60")
                db.execSQL("ALTER TABLE task_table ADD COLUMN priority TEXT NOT NULL DEFAULT 'NORMAL'")
            }
        }

        fun getDatabase(context: Context): TaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaskDatabase::class.java,
                    "task_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration() // Allow destructive migration as fallback
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
