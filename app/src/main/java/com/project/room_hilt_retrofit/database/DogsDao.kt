package com.project.room_hilt_retrofit.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.project.room_hilt_retrofit.model.Dog

//Data access objects (DAOs) that provide methods that your app can use to query, update, insert, and delete data in the database.
@Dao
interface DogsDao {
    @Query("SELECT * FROM Dog")
    suspend fun getAllDogs(): List<Dog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDog(dog: Dog)

    @Delete
    suspend fun deleteDog(dog: Dog)

}