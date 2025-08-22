package com.project.room_hilt_retrofit

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.project.room_hilt_retrofit.database.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.sqlcipher.database.SupportFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseMigrationManager @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    private val TAG = "mLogDatabase"
    private val dbName = "room_database"
    private val password = "key".toByteArray()
    private val prefsName = "app_prefs"
    private val migrationFlagKey = "is_db_migrated"

    suspend fun performMigration() = withContext(Dispatchers.IO) {
        val prefs = appContext.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val isMigrated = prefs.getBoolean(migrationFlagKey, false)
        val dbFile = appContext.getDatabasePath(dbName)

        Log.d(TAG, "Checking for database migration. File exists: ${dbFile.exists()}, Is migrated: $isMigrated")

        if (dbFile.exists() && !isMigrated) {
            Log.d(TAG, "Unencrypted database found. Starting data migration...")

            try {
                // 1. Открываем старую, незашифрованную БД для чтения.
                val oldDb = Room.databaseBuilder(appContext, AppDatabase::class.java, dbName).build()
                val dogs = oldDb.dogsDao().getAllDogs()
                Log.d(TAG, "Found ${dogs.size} dogs in the old database. Closing old DB.")
                oldDb.close()

                // 2. Удаляем старый файл.
                if (dbFile.delete()) {
                    Log.d(TAG, "Old unencrypted database file deleted.")
                } else {
                    Log.e(TAG, "Failed to delete old unencrypted database file.")
                }

                // 3. Создаем новую зашифрованную БД и записываем в нее данные.
                val factory = SupportFactory(password)
                val newDb = Room.databaseBuilder(appContext, AppDatabase::class.java, dbName)
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build()

                // Здесь мы получаем доступ к DAO новой БД, чтобы записать в нее данные.
                val newDogsDao = newDb.dogsDao()
                dogs.forEach { dog ->
                    newDogsDao.insertDog(dog)
                }
                Log.d(TAG, "Migrated ${dogs.size} dogs to the new encrypted database.")

                newDb.close()

                // 4. Устанавливаем флаг, что миграция выполнена.
                prefs.edit().putBoolean(migrationFlagKey, true).apply()
                Log.d(TAG, "Migration flag set. Data migration completed successfully.")

            } catch (e: Exception) {
                Log.e(TAG, "Data migration failed: ${e.message}", e)
                // В случае ошибки, лучше всего удалить остатки и начать с чистого листа
                dbFile.delete()
            }
        } else {
            Log.d(TAG, "No data migration needed. Using existing encrypted database.")
        }
    }
}