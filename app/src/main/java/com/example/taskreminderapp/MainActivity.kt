package com.example.taskreminderapp

import Data.AuthState
import Data.AuthViewModel
import Screen.AuthFlow
import Screen.TaskListScreen
import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taskreminderapp.ui.theme.TaskReminderAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, notifications will work
        } else {
            // Permission denied, inform user that notifications won't work
            // You could show a dialog here if needed
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Request exact alarm permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Note: For exact alarms, we need to direct user to settings
                // This is more complex and might require a user explanation dialog
                // For now, we'll handle this gracefully in the NotificationHelper
            }
        }

        setContent {
            TaskReminderAppTheme {
                MainContent()
            }
        }
    }
}

@Composable
fun MainContent(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val navController = rememberNavController()

    when {
        currentUser != null -> {
            // User is authenticated, show main app
            Log.d("MainActivity", "Showing TaskListScreen for user: ${currentUser!!.id}, ${currentUser!!.username}")
            NavHost(
                navController = navController,
                startDestination = "task_list_screen"
            ) {
                composable("task_list_screen") {
                    TaskListScreen(
                        userId = currentUser!!.id,
                        onSignOut = {
                            authViewModel.signOut()
                        }
                    )
                }
            }
        }
        else -> {
            // User is not authenticated, show auth flow
            AuthFlow(
                onAuthSuccess = {
                    // Navigation will be handled automatically by state changes
                }
            )
        }
    }
}
