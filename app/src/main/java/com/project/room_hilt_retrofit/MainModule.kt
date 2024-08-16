package com.project.room_hilt_retrofit

import android.content.Context
import androidx.room.Room
import com.project.room_hilt_retrofit.database.AppDatabase
import com.project.room_hilt_retrofit.database.DogsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//A Hilt module can be thought of as a collection of “instructions” that tell Hilt how to create an instance of something that doesn’t have a constructor.
//Such as an interface or a system service.
@Module
@InstallIn(SingletonComponent::class)
class MainModule {
//There are instances which can’t be constructor injected.
//Let’s list down a few such cases:
//
//Injecting interface : When a class depends on an interface, how will we tell the system to instantiate it because interfaces don’t have constructors.
//Injecting third party classes : When a class depends on an object which we don’t own because it comes from a third party library, how will we add it our Hilt tree.
//Injecting instances created by Builder pattern
//In these cases, we can provide Hilt with binding information by using Hilt modules.
//
//A Hilt module is a class that is annotated with @Module annotation. It informs Hilt how to provide instances of certain types.
//Every Hilt module must be annotated @InstallIn to tell Hilt which Android class each module will be used or installed in.

    //When an instance can’t be constructed directly, you can create a provider. A provider is a factory function that returns an instance of an object.
    @Provides
    //@Singleton, говорит Hilt, чтобы он не пересоздавал каждый раз новый экземпляр, а предоставлял уже имеющийся(если ни разу ещё не был создан, то он его создаст).
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "room_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideDogsDao(appDatabase: AppDatabase): DogsDao {
        return appDatabase.dogsDao()
    }

}