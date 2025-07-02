package Data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM user_table WHERE email = :email LIMIT 1")
    fun getUserByEmail(email: String): LiveData<User?>

    @Query("SELECT * FROM user_table WHERE email = :email LIMIT 1")
    suspend fun getUserByEmailSync(email: String): User?

    @Query("SELECT * FROM user_table WHERE id = :id LIMIT 1")
    suspend fun getUserByIdSync(id: Int): User?

    @Query("SELECT * FROM user_table WHERE is_logged_in = 1 LIMIT 1")
    suspend fun getLoggedInUser(): User?
}
