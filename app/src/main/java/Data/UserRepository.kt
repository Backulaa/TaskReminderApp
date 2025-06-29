package Data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class UserRepository(private val userDao: UserDao) {

    fun getUserByEmail(email: String): LiveData<User?> {
        return userDao.getUserByEmail(email)
    }

    suspend fun insert(user: User) {
        userDao.insert(user)
    }

    suspend fun update(user: User) {
        userDao.update(user)
    }

    suspend fun delete(user: User) {
        userDao.delete(user)
    }

    // Authentication methods
    suspend fun registerUser(username: String, email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                // Check if user already exists
                val existingUser = userDao.getUserByEmailSync(email)
                if (existingUser != null) {
                    return@withContext Result.failure(Exception("User with this email already exists"))
                }

                // Hash password for security
                val hashedPassword = hashPassword(password)
                
                // Create new user
                val newUser = User(
                    username = username,
                    email = email,
                    password = hashedPassword,
                    isLoggedIn = false
                )
                
                userDao.insert(newUser)
                Result.success(newUser)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun authenticateUser(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val user = userDao.getUserByEmailSync(email)
                if (user == null) {
                    return@withContext Result.failure(Exception("User not found"))
                }

                val hashedPassword = hashPassword(password)
                if (user.password == hashedPassword) {
                    // Update login status
                    val updatedUser = user.copy(isLoggedIn = true)
                    userDao.update(updatedUser)
                    Result.success(updatedUser)
                } else {
                    Result.failure(Exception("Invalid password"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun logoutUser(user: User) {
        withContext(Dispatchers.IO) {
            val updatedUser = user.copy(isLoggedIn = false)
            userDao.update(updatedUser)
        }
    }

    // Simple password hashing (in production, use proper hashing like bcrypt)
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.fold("") { str, it -> str + "%02x".format(it) }
    }
}
