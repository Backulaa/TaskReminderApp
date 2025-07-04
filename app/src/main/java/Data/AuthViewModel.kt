package Data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        checkLoginStatus()
    }

    fun signUp(username: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            // Validation
            if (!isValidEmail(email)) {
                _authState.value = AuthState.Error("Invalid email format")
                return@launch
            }

            if (password.length < 6) {
                _authState.value = AuthState.Error("Password must be at least 6 characters")
                return@launch
            }

            if (username.isBlank()) {
                _authState.value = AuthState.Error("Username cannot be empty")
                return@launch
            }

            Log.d("AuthViewModel", "Attempting to register user: $username, $email")

            val result = userRepository.registerUser(username, email, password)
            if (result.isSuccess) {
                val user = result.getOrNull()
                Log.d("AuthViewModel", "User registered successfully: ${user?.id}, ${user?.username}")
                _currentUser.value = user
                _authState.value = AuthState.Success("Account created successfully!")
            } else {
                Log.e("AuthViewModel", "Registration failed: ${result.exceptionOrNull()?.message}")
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            if (email.isBlank() || password.isBlank()) {
                _authState.value = AuthState.Error("Email and password cannot be empty")
                return@launch
            }

            val result = userRepository.authenticateUser(email, password)
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                _authState.value = AuthState.Success("Login successful!")
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                userRepository.logoutUser(user)
            }
            _currentUser.value = null
            _authState.value = AuthState.SignedOut
        }
    }

    fun updateProfile(user: User, newUsername: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            if (newUsername.isBlank()) {
                _authState.value = AuthState.Error("Username cannot be empty")
                return@launch
            }

            Log.d("AuthViewModel", "Updating profile for user: ${user.id}, new username: $newUsername")

            // Try direct update method first
            val result = userRepository.updateUserByIdDirect(user.id, newUsername)
            if (result.isSuccess) {
                val updatedUser = result.getOrNull()!!
                // Refresh user data from database to ensure we have the latest data
                val refreshedUser = userRepository.getUserById(user.id)
                _currentUser.value = refreshedUser ?: updatedUser
                _authState.value = AuthState.Success("Profile updated successfully!")
                Log.d("AuthViewModel", "Profile updated successfully for user: ${user.id}, refreshed user: ${refreshedUser?.username}")
            } else {
                // Fall back to original method
                Log.w("AuthViewModel", "Direct update failed, trying original method")
                val fallbackResult = userRepository.updateProfile(user, newUsername)
                if (fallbackResult.isSuccess) {
                    val updatedUser = fallbackResult.getOrNull()!!
                    val refreshedUser = userRepository.getUserById(user.id)
                    _currentUser.value = refreshedUser ?: updatedUser
                    _authState.value = AuthState.Success("Profile updated successfully!")
                    Log.d("AuthViewModel", "Profile updated successfully for user: ${user.id}, refreshed user: ${refreshedUser?.username}")
                } else {
                    _authState.value = AuthState.Error(fallbackResult.exceptionOrNull()?.message ?: "Failed to update profile")
                    Log.e("AuthViewModel", "Failed to update profile: ${fallbackResult.exceptionOrNull()?.message}")
                }
            }
        }
    }

    fun changePassword(user: User, currentPassword: String, newPassword: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            if (currentPassword.isBlank()) {
                _authState.value = AuthState.Error("Current password cannot be empty")
                return@launch
            }

            if (newPassword.length < 6) {
                _authState.value = AuthState.Error("New password must be at least 6 characters")
                return@launch
            }

            Log.d("AuthViewModel", "Changing password for user: ${user.id}")

            // Try direct password change method first
            val result = userRepository.changePasswordDirect(user.id, currentPassword, newPassword)
            if (result.isSuccess) {
                val updatedUser = result.getOrNull()!!
                // Refresh user data from database to ensure we have the latest data
                val refreshedUser = userRepository.getUserById(user.id)
                _currentUser.value = refreshedUser ?: updatedUser
                _authState.value = AuthState.Success("Password changed successfully!")
                Log.d("AuthViewModel", "Password changed successfully for user: ${user.id}")
            } else {
                // Fall back to original method
                Log.w("AuthViewModel", "Direct password change failed, trying original method")
                val fallbackResult = userRepository.changePassword(user, currentPassword, newPassword)
                if (fallbackResult.isSuccess) {
                    val updatedUser = fallbackResult.getOrNull()!!
                    val refreshedUser = userRepository.getUserById(user.id)
                    _currentUser.value = refreshedUser ?: updatedUser
                    _authState.value = AuthState.Success("Password changed successfully!")
                    Log.d("AuthViewModel", "Password changed successfully for user: ${user.id}")
                } else {
                    _authState.value = AuthState.Error(fallbackResult.exceptionOrNull()?.message ?: "Failed to change password")
                    Log.e("AuthViewModel", "Failed to change password: ${fallbackResult.exceptionOrNull()?.message}")
                }
            }
        }
    }

    fun ensureCurrentUser(user: User) {
        if (_currentUser.value == null || _currentUser.value?.id != user.id) {
            Log.d("AuthViewModel", "Ensuring current user is set: ${user.id}, ${user.username}")
            _currentUser.value = user
        }
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            // Check if user is already logged in (you can implement this in UserDao)
            _authState.value = AuthState.SignedOut
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun clearAuthState() {
        _authState.value = AuthState.Idle
    }

    fun refreshCurrentUser() {
        viewModelScope.launch {
            val currentUserValue = _currentUser.value
            if (currentUserValue != null) {
                val refreshedUser = userRepository.getUserById(currentUserValue.id)
                if (refreshedUser != null) {
                    _currentUser.value = refreshedUser
                    Log.d("AuthViewModel", "Current user refreshed: ${refreshedUser.id}, ${refreshedUser.username}")
                } else {
                    Log.w("AuthViewModel", "Failed to refresh current user - user not found in database")
                }
            }
        }
    }

    fun testDatabaseOperations(user: User) {
        viewModelScope.launch {
            val testResult = userRepository.testDatabaseOperations(user.id)
            Log.d("AuthViewModel", "Database test result: $testResult")
        }
    }
}

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object SignedOut : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
