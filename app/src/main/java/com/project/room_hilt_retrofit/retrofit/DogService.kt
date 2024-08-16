package com.project.room_hilt_retrofit.retrofit

import com.project.room_hilt_retrofit.model.DogResponse
import retrofit2.Response
import retrofit2.http.GET

interface DogService {
    @GET("api/breeds/image/random")
    suspend fun getDog(): Response<DogResponse>
}