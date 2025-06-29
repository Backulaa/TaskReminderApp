package Screen

import Data.AuthViewModel
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AuthFlow(
    onAuthSuccess: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var showSignUp by remember { mutableStateOf(false) }

    if (showSignUp) {
        SignUpScreen(
            onNavigateToSignIn = { showSignUp = false },
            onSignUpSuccess = onAuthSuccess,
            authViewModel = authViewModel
        )
    } else {
        LoginScreen(
            onNavigateToSignUp = { showSignUp = true },
            onLoginSuccess = onAuthSuccess,
            authViewModel = authViewModel
        )
    }
}
