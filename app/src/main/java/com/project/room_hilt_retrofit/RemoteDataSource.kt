package com.project.room_hilt_retrofit

import com.project.room_hilt_retrofit.retrofit.DogService
import javax.inject.Inject

class RemoteDataSource @Inject constructor(private val dogService: DogService) {
    suspend fun getDog() = dogService.getDog()
}