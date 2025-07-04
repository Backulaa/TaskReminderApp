package Data

import android.util.Log
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

                // Insert user and get the generated ID
                val userId = userDao.insert(newUser)

                // Create user object with correct ID and mark as logged in
                val loggedInUser = newUser.copy(id = userId.toInt(), isLoggedIn = true)
                userDao.update(loggedInUser)

                Result.success(loggedInUser)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun authenticateUser(email: String, password: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                val user = userDao.getUserByEmailSync(email)
                    ?: return@withContext Result.failure(Exception("User not found"))

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

    // Profile update methods
    suspend fun updateProfile(user: User, newUsername: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("UserRepository", "Updating profile for user ${user.id}: ${user.username} -> $newUsername")

                val updatedUser = user.copy(username = newUsername)
                userDao.update(updatedUser)

                // Verify the update was saved
                val verifyUser = userDao.getUserByIdSync(user.id)
                Log.d("UserRepository", "After update - DB user: ${verifyUser?.id}, ${verifyUser?.username}")

                if (verifyUser?.username == newUsername) {
                    Log.d("UserRepository", "Profile updated successfully for user ${user.id}")
                    Result.success(updatedUser)
                } else {
                    Log.e("UserRepository", "Profile update verification failed. Expected: $newUsername, Got: ${verifyUser?.username}")
                    Result.failure(Exception("Profile update was not saved to database"))
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "Failed to update profile for user ${user.id}", e)
                Result.failure(e)
            }
        }
    }

    suspend fun changePassword(user: User, currentPassword: String, newPassword: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("UserRepository", "Changing password for user ${user.id}")
                // Verify current password
                val hashedCurrentPassword = hashPassword(currentPassword)
                if (user.password != hashedCurrentPassword) {
                    Log.w("UserRepository", "Current password verification failed for user ${user.id}")
                    return@withContext Result.failure(Exception("Current password is incorrect"))
                }

                // Hash new password
                val hashedNewPassword = hashPassword(newPassword)
                val updatedUser = user.copy(password = hashedNewPassword)

                Log.d("UserRepository", "Before password update - user: ${user.id}, old password hash: ${user.password.take(10)}...")
                Log.d("UserRepository", "About to update with new password hash: ${hashedNewPassword.take(10)}...")

                userDao.update(updatedUser)

                // Verify the update was saved
                val verifyUser = userDao.getUserByIdSync(user.id)
                Log.d("UserRepository", "After password update - DB user: ${verifyUser?.id}, password hash: ${verifyUser?.password?.take(10)}...")

                if (verifyUser?.password == hashedNewPassword) {
                    Log.d("UserRepository", "Password changed successfully for user ${user.id}")
                    Result.success(updatedUser)
                } else {
                    Log.e("UserRepository", "Password change verification failed. Expected hash: ${hashedNewPassword.take(10)}..., Got: ${verifyUser?.password?.take(10)}...")
                    Result.failure(Exception("Password change was not saved to database"))
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "Failed to change password for user ${user.id}", e)
                Result.failure(e)
            }
        }
    }

    // Alternative update method using direct SQL update
    suspend fun updateUserByIdDirect(userId: Int, newUsername: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("UserRepository", "Direct update: user $userId -> username: $newUsername")

                // Get current user
                val currentUser = userDao.getUserByIdSync(userId)
                if (currentUser == null) {
                    return@withContext Result.failure(Exception("User not found"))
                }

                // Use direct SQL update
                userDao.updateUsernameDirect(userId, newUsername)

                // Verify the update
                val verifyUser = userDao.getUserByIdSync(userId)
                Log.d("UserRepository", "After direct update - DB user: ${verifyUser?.id}, ${verifyUser?.username}")

                if (verifyUser?.username == newUsername) {
                    val updatedUser = currentUser.copy(username = newUsername)
                    Log.d("UserRepository", "Direct profile update successful for user $userId")
                    Result.success(updatedUser)
                } else {
                    Log.e("UserRepository", "Direct profile update verification failed")
                    Result.failure(Exception("Direct profile update was not saved to database"))
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "Failed to update profile directly for user $userId", e)
                Result.failure(e)
            }
        }
    }

    // Alternative password change method using direct SQL update
    suspend fun changePasswordDirect(userId: Int, currentPassword: String, newPassword: String): Result<User> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("UserRepository", "Direct password change for user $userId")

                // Get current user
                val currentUser = userDao.getUserByIdSync(userId)
                if (currentUser == null) {
                    return@withContext Result.failure(Exception("User not found"))
                }

                // Verify current password
                val hashedCurrentPassword = hashPassword(currentPassword)
                if (currentUser.password != hashedCurrentPassword) {
                    Log.w("UserRepository", "Current password verification failed for user $userId")
                    return@withContext Result.failure(Exception("Current password is incorrect"))
                }

                // Hash new password
                val hashedNewPassword = hashPassword(newPassword)

                Log.d("UserRepository", "Before direct password update - user: $userId")

                // Use direct SQL update
                userDao.updatePasswordDirect(userId, hashedNewPassword)

                // Verify the update
                val verifyUser = userDao.getUserByIdSync(userId)
                Log.d("UserRepository", "After direct password update - DB user: ${verifyUser?.id}")

                if (verifyUser?.password == hashedNewPassword) {
                    val updatedUser = currentUser.copy(password = hashedNewPassword)
                    Log.d("UserRepository", "Direct password change successful for user $userId")
                    Result.success(updatedUser)
                } else {
                    Log.e("UserRepository", "Direct password change verification failed")
                    Result.failure(Exception("Direct password change was not saved to database"))
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "Failed to change password directly for user $userId", e)
                Result.failure(e)
            }
        }
    }

    // Debug method to verify database updates
    suspend fun getUserById(userId: Int): User? {
        return withContext(Dispatchers.IO) {
            try {
                val user = userDao.getUserByIdSync(userId)
                Log.d("UserRepository", "Retrieved user from DB: ${user?.id}, ${user?.username}, ${user?.email}")
                user
            } catch (e: Exception) {
                Log.e("UserRepository", "Error retrieving user $userId", e)
                null
            }
        }
    }

    // Debug method to test database operations
    suspend fun testDatabaseOperations(userId: Int): String {
        return withContext(Dispatchers.IO) {
            try {
                val user = userDao.getUserByIdSync(userId)
                if (user == null) {
                    return@withContext "User not found in database"
                }

                val originalUsername = user.username
                val testUsername = "TestUser_${System.currentTimeMillis()}"

                Log.d("UserRepository", "Testing database operations for user $userId")
                Log.d("UserRepository", "Original username: $originalUsername")

                // Test direct SQL update
                userDao.updateUsernameDirect(userId, testUsername)

                // Verify the change
                val updatedUser = userDao.getUserByIdSync(userId)
                Log.d("UserRepository", "After test update: ${updatedUser?.username}")

                if (updatedUser?.username == testUsername) {
                    // Restore original username
                    userDao.updateUsernameDirect(userId, originalUsername)
                    val restoredUser = userDao.getUserByIdSync(userId)

                    if (restoredUser?.username == originalUsername) {
                        "Database operations working correctly"
                    } else {
                        "Database operations partially working - couldn't restore original username"
                    }
                } else {
                    "Database operations not working - update failed"
                }
            } catch (e: Exception) {
                Log.e("UserRepository", "Database test failed", e)
                "Database test failed: ${e.message}"
            }
        }
    }

    // Simple password hashing (in production, use proper hashing like bcrypt)
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(password.toByteArray())
        return hash.fold("") { str, it -> str + "%02x".format(it) }
    }
}
