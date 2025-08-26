package com.project.room_hilt_retrofit

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.project.room_hilt_retrofit.database.AppDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteException
import net.sqlcipher.database.SupportFactory
import java.io.File
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
        val state = getDatabaseState(appContext, dbName)
        Log.e(TAG, "DatabaseState: $state")


        if (dbFile.exists() && state == State.UNENCRYPTED) {
            //Migrate to encripted database
            Log.d(TAG, "Migrate")

            migrateToEncryptedDatabase(dbName, appContext, "key")
        } else {
            Log.d(TAG, "Not migrate state = $state")
        }

/*        Log.d(TAG, "Checking for database migration. File exists: ${dbFile.exists()}, Is migrated: $isMigrated")

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
        }*/
    }

    /**
     * Migrates an existing SQLite database to an encrypted SQLite database using SQLCipher.
     * The original unencrypted database is replaced with the encrypted one.
     *
     * @param dataBaseName The name of the existing SQLite database.
     * @param context The application context.
     * @param password The password used to encrypt the database.
     * @throws SQLiteException If an error occurs during database migration.
     */
    private fun migrateToEncryptedDatabase(
        dataBaseName: String, context: Context, password: String
    ) {
        Log.d(TAG, "migrateToEncryptedDatabase")
        // Obtain the paths for the original and temporary databases
        val databasePath = context.getDatabasePath(dataBaseName).path
        val temporaryDatabasePath = context.getDatabasePath("${dataBaseName}temp").absolutePath
        val originalFile = File(databasePath)
        // Check if the original database file exists
        if (originalFile.exists()) {
            // Create a reference to the temporary database file
            val newFile = File(temporaryDatabasePath)
            // Open the original database and execute SQL commands to encrypt it
            var database = getCurrentSqliteDatabase(databasePath)
            runCatching {
                database.rawExecSQL("ATTACH DATABASE '$temporaryDatabasePath' AS encrypted KEY '$password';")
                database.rawExecSQL("SELECT sqlcipher_export('encrypted')")
                database.rawExecSQL("DETACH DATABASE encrypted;")
                // Retrieve the database version
                val version = database.version
                // Close the database connection
                database.close()
                // Open the encrypted database and set its version
                database = SQLiteDatabase.openDatabase(
                    temporaryDatabasePath, password, null, SQLiteDatabase.OPEN_READWRITE
                )
                database.version = version
                // Close the database connection
                database.close()
                // Delete the original unencrypted database file
                originalFile.delete()
                // Rename the temporary file to the original file name
                newFile.renameTo(originalFile)
                Log.d(TAG, "delete -> close")
                val state = getDatabaseState(appContext, dbName)
                Log.e(TAG, "DatabaseState: $state")
            }.onFailure {
                Log.e(TAG, "Error migrating database", it)
            }
        }
    }

    /**
     * Retrieves the SQLiteDatabase object for the specified database path with optional encryption.
     *
     * @param databasePath The path to the SQLite database file.
     * @param password The password used for encryption (optional).
     * @return An instance of SQLiteDatabase.
     * @throws SQLiteException If there is an error opening the database.
     */
    @Throws(SQLiteException::class)
    private fun getCurrentSqliteDatabase(
        databasePath: String,
        password: String? = null
    ): SQLiteDatabase {
        // Set the passphrase to an empty string if not provided
        val passPhrase = password ?: ""
        // Open the SQLiteDatabase with the specified parameters
        return SQLiteDatabase.openDatabase(
            databasePath,
            passPhrase,
            null,
            SQLiteDatabase.CREATE_IF_NECESSARY
        )
    }

    enum class State {
        DOES_NOT_EXIST, UNENCRYPTED, ENCRYPTED
    }

    private fun getDatabaseState(context: Context, dbName: String?): State {
        SQLiteDatabase.loadLibs(context)
        return getDatabaseState(context.getDatabasePath(dbName))
    }

    private fun getDatabaseState(dbPath: File): State {
        if (!dbPath.exists()) return State.DOES_NOT_EXIST

        return try {
            SQLiteDatabase.openDatabase(
                dbPath.absolutePath, "", null,
                SQLiteDatabase.OPEN_READONLY, null, null
            ).use { db ->
                db.version // Accessing version to check if the database is encrypted
                State.UNENCRYPTED
            }
        } catch (e: SQLiteException) {
            State.ENCRYPTED
        }
    }
}