package com.project.room_hilt_retrofit

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    private val TAG = "mLogDatabase"

    @Inject
    lateinit var databaseMigrationManager: DatabaseMigrationManager

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application started. Starting database migration check.")

        // Запускаем асинхронную миграцию в фоновом потоке.
        // Это произойдет до того, как Hilt начнет инжектировать базу данных.
        CoroutineScope(Dispatchers.IO).launch {
            databaseMigrationManager.performMigration()
        }
    }
}