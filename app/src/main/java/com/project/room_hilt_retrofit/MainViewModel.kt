package com.project.room_hilt_retrofit

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.room_hilt_retrofit.database.DogsRepository
import com.project.room_hilt_retrofit.model.Dog
import com.project.room_hilt_retrofit.model.DogResponse
import com.project.room_hilt_retrofit.retrofit.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

//В Hilt для ViewModel есть специальная аннотация @HiltViewModel, ViewModel также как и раньше будут привязаны к жизненным циклам Fragment или Activity, смотря где вы захотите её создать.
@HiltViewModel
//This is called constructor injection. The @Inject keyword comes from the Hilt, which enables the framework to find this constructor and inject it with appropriate class instances.
class MainViewModel @Inject constructor(
    private val retrofitRepository: RetrofitRepository,
    private val dogsRepository: DogsRepository,
) : ViewModel() {

    private val _response: MutableLiveData<NetworkResult<DogResponse>> = MutableLiveData()
    val response: LiveData<NetworkResult<DogResponse>> = _response
    fun fetchDogResponse() = viewModelScope.launch {
        retrofitRepository.getDog().collect { values ->
            _response.value = values
        }
    }

    fun addDog(url: String) {
        viewModelScope.launch {
            dogsRepository.insertDogs(Dog(dogImageUrl = url))
        }.invokeOnCompletion {
            Log.e("mLogHilt", "Dog Added")
        }
    }

    private val _dogsFlow = MutableStateFlow<List<Dog>>(listOf())
    val dogsFlow: StateFlow<List<Dog>> = _dogsFlow.asStateFlow()

    fun getAllDogs() {
        viewModelScope.launch {
            _dogsFlow.value = dogsRepository.getAllDogs()
        }
    }
}