package Data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM task_table WHERE user_id = :userId ORDER BY due_date ASC")
    fun getTasksForUser(userId: Int): LiveData<List<Task>>

    @Query("SELECT * FROM task_table WHERE user_id = :userId ORDER BY due_date ASC")
    suspend fun getTasksForUserSync(userId: Int): List<Task>

    @Query("SELECT * FROM task_table WHERE id = :id")
    suspend fun getTaskById(id: Int): Task?

    @Query("DELETE FROM task_table WHERE user_id = :userId")
    suspend fun deleteAllTasksForUser(userId: Int)
}
