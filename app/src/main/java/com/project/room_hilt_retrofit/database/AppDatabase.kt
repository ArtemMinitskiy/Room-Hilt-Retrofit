package com.project.room_hilt_retrofit.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.project.room_hilt_retrofit.model.Dog

//The database class that holds the database and serves as the main access point for the underlying connection to your app's persisted data.

//The class must be annotated with a @Database annotation that includes an entities array that lists all of the data entities associated with the database.
//The class must be an abstract class that extends RoomDatabase.
//For each DAO class that is associated with the database, the database class must define an abstract method that has zero arguments and returns an instance of the DAO class.
@Database(entities = [Dog::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dogsDao(): DogsDao
}