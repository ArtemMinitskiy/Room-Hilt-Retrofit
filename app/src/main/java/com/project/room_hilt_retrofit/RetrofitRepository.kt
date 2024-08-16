package com.project.room_hilt_retrofit

import com.project.room_hilt_retrofit.model.DogResponse
import com.project.room_hilt_retrofit.retrofit.BaseApiResponse
import com.project.room_hilt_retrofit.retrofit.NetworkResult
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@ActivityRetainedScoped
class RetrofitRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) : BaseApiResponse() {
    suspend fun getDog(): Flow<NetworkResult<DogResponse>> {
        return flow {
            emit(safeApiCall { remoteDataSource.getDog() })
        }.flowOn(Dispatchers.IO)
    }
}