package com.example.taskreminderapp.di

import Data.TaskDao
import Data.TaskDatabase
import Data.TaskRepository
import Data.UserDao
import Data.UserRepository
import android.content.Context
import androidx.room.Room
import com.example.taskreminderapp.notification.NotificationHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTaskDatabase(@ApplicationContext context: Context): TaskDatabase {
        return Room.databaseBuilder(
            context,
            TaskDatabase::class.java,
            "task_database"
        )
        .addMigrations(TaskDatabase.MIGRATION_1_2)
        .fallbackToDestructiveMigration() // Allow destructive migrations for development
        .build()
    }

    @Provides
    fun provideUserDao(database: TaskDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideTaskDao(database: TaskDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    @Singleton
    fun provideUserRepository(userDao: UserDao): UserRepository {
        return UserRepository(userDao)
    }

    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper {
        return NotificationHelper(context)
    }

    @Provides
    @Singleton
    fun provideTaskRepository(
        taskDao: TaskDao,
        notificationHelper: NotificationHelper
    ): TaskRepository {
        return TaskRepository(taskDao, notificationHelper)
    }
}
