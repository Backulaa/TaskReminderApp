package Data

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

            val result = userRepository.registerUser(username, email, password)
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                _authState.value = AuthState.Success("Account created successfully!")
            } else {
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
}

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data object SignedOut : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
