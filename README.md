📱 TaskReminderApp

A modern, feature-rich task reminder application built with Jetpack Compose and Material Design 3

📋 Description

TaskReminderApp is a comprehensive task management application that helps users organize their daily activities with intelligent reminders and beautiful user interface. The app features a modern Material Design 3 interface, smart notification system, and powerful filtering capabilities.

✨ Key Features

    🔐 User Authentication - Secure sign-up and sign-in system
    
    📅 Advanced Date & Time Picker - Material Design calendar and clock interfaces
    
    ⏰ Custom Reminder Times - Choose from 6 reminder options (15min to 2 days)
    
    🎨 Priority System - Color-coded task priorities (High/Normal/Low)
    
    🔔 Smart Notifications - Automatic scheduling with custom timing
    
    🔍 Advanced Search & Filter - Filter by date, priority, and completion status
    
    📱 Modern UI - Beautiful Material Design 3 interface
    
    💾 Offline Storage - Local Room database for reliable data persistence
  
🛠️ Installation

    Prerequisites
    
    Android Studio Arctic Fox (2020.3.1) or newer
    
    JDK 17 or newer
    
    Android SDK with minimum API level 24 (Android 7.0)
    
    Gradle 8.0 or newer
  
  
  Setup Steps
  
    1. Clone the repository
      git clone https://github.com/yourusername/TaskReminderApp.git
      cd TaskReminderApp
    2. Open in Android Studio
      Launch Android Studio
      Select "Open an Existing Project"
      Navigate to the cloned directory
      Click "OK"
    3. Sync Project
      Android Studio will automatically sync Gradle
      Wait for the sync to complete
    4. Build the project
      ./gradlew build
      
📱 Usage

  Getting Started
  
    1. Account Creation
      Launch the app
      Tap "Sign Up" to create a new account
      Enter username, email, and password
      Tap "Sign Up" to complete registration
    2. Sign In
      Enter your email and password
      Tap "Sign In" to access your tasks
    Managing Tasks
    
  ➕ Creating a New Task
  
    1. Tap the Floating Action Button (+ icon) on the main screen
    2. Fill in task details:
      Task Name: Enter a descriptive name
      Due Date: Tap calendar icon to select date
      Due Time: Tap clock icon to select time
      Priority: Choose High (🔴), Normal (🟠), or Low (🟢)
      Reminder: Select when to be notified (15min to 2 days before)
    3. Tap "Add Task" to save
    
  ✏️ Editing Tasks
  
    1. Tap any task in the task list
    2. Modify any field in the edit dialog:
      Change task name, date, time, priority, or reminder
      Toggle completion status with the switch
    3. Tap "Update Task" to save changes
    4. Tap "Delete Task" to remove (with confirmation)
    
  🔍 Searching and Filtering
  
    1. Tap the Search icon (🔍) in the top-right corner
    2/ Apply filters:
      Date Filter: Select specific due date
      Priority Filter: Choose High/Normal/Low
      Status Filter: Filter by Completed/Pending/All
    3. View filtered results with task counter
    4. Clear filters individually or all at once
    
  🔔 Notification Management
  
    Automatic Scheduling:
      Notifications are automatically scheduled when creating tasks
      Timing based on your selected reminder preference
      Cancelled when tasks are completed or deleted
    Reminder Options:
      15 minutes before - Last-minute reminders
      30 minutes before - Quick preparation time
      1 hour before - Default option
      2 hours before - Planning time
      1 day before - Day-ahead notice
      2 days before - Early planning
      
🖥️ System Requirements

  Minimum Requirements
  
    Android Version: 7.0 (API level 24)
    RAM: 2GB
    Storage: 50MB available space
    Permissions: Notification access, Exact alarm scheduling
    
  Recommended Requirements
  
    Android Version: 12.0 (API level 31) or newer
    RAM: 4GB or more
    Storage: 100MB available space
    
  Development Requirements
  
    Java Development Kit: JDK 17+
    Android Studio: Arctic Fox or newer
    Gradle: 8.0+
    Kotlin: 1.9.0+

🚀 Run the App

  Development Environment
  
    1. Clone and Setup
      git clone https://github.com/yourusername/TaskReminderApp.git
      cd TaskReminderApp
    3. Open in Android Studio
      # Open Android Studio and import project
      # Or use command line (if configured)
      studio .
    4. Run on Emulator
      # Create and start emulator
      ./gradlew installDebug
      # Or use Android Studio's run button (Shift+F10)
    5. Run on Physical Device
      Enable Developer Options on your Android device
      Enable USB Debugging
      Connect device via USB
      Run from Android Studio or:
      ./gradlew installDebug
    
📞 Contact Information

  Project Maintainer
  
  Name: Backul
  
  Email: 23520088@gm.uit.edu.vn
  
  GitHub: Backulaa
  
  Project Links
  
  Repository: [https://github.com/yourusername/TaskReminderApp](https://github.com/Backulaa/TaskReminderApp)
