package com.project.room_hilt_retrofit.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.project.room_hilt_retrofit.model.Dog

@Database(entities = [Dog::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dogsDao(): DogsDao
}