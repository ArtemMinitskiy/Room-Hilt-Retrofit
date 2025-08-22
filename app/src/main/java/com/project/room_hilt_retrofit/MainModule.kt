package com.project.room_hilt_retrofit

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.project.room_hilt_retrofit.database.AppDatabase
import com.project.room_hilt_retrofit.database.DogsDao
import com.project.room_hilt_retrofit.database.DogsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainModule {

    private const val TAG = "mLogDatabase"
    private val password = "key".toByteArray()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        Log.d(TAG, "Providing the final encrypted AppDatabase instance.")
        val factory = SupportFactory(password)
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "room_database"
        )
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideDogsDao(appDatabase: AppDatabase): DogsDao {
        Log.d(TAG, "Providing DogsDao.")
        return appDatabase.dogsDao()
    }

    @Provides
    @Singleton
    fun provideDogsRepository(dogsDao: DogsDao): DogsRepository {
        Log.d(TAG, "Providing DogsRepository.")
        return DogsRepository(dogsDao)
    }
}