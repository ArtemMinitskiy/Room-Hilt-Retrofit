package com.project.room_hilt_retrofit.database

import com.project.room_hilt_retrofit.model.Dog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DogsRepository @Inject constructor(private val dogsDao: DogsDao) {
    suspend fun insertDogs(dog: Dog) {
        withContext(Dispatchers.IO) {
            dogsDao.insertDog(dog)
        }
    }

    suspend fun getAllDogs(): List<Dog> {
        return withContext(Dispatchers.IO) {
            return@withContext dogsDao.getAllDogs()
        }
    }

}