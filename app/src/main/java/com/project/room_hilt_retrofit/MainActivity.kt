package com.project.room_hilt_retrofit

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.project.room_hilt_retrofit.retrofit.NetworkResult
import com.project.room_hilt_retrofit.ui.theme.PurpleGrey40
import com.project.room_hilt_retrofit.ui.theme.RoomHiltRetrofitTheme
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

//Аннотация @AndroidEntryPoint говорит Hilt, чтобы он генерировал классы Component.
//Каждый компонент отвечает за зависимости своего класса, ActivityComponent за Activity, FragmentComponent за Fragment.
//Именно эта аннотация позволяет нам в дальнейшем привязывать зависимости к Fragment, Activity и т.д..
//Также если вы указали эту аннотацию, например, у какого-то фрагмента, её также необходимо указать у Activity, к которой он привязан.
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
//    private val viewModel: MainViewModel by viewModels()

//The field injection can be done by the @Inject keyword, which lets the Hilt fill the required field with the proper class instance.
//The field injection is handy during class inheritance, which we do not possess from Android like Activity, Service, Fragment, View and others.
//    @Inject
//    lateinit var repository: MainRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            //Compose Way
            val viewModel: MainViewModel = hiltViewModel()
            val composition = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lf20_v90rvaig))
            var imagePath by remember { mutableStateOf("") }

            RoomHiltRetrofitTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                    ) {
                        GlideImage(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp),
                            imageModel = { imagePath },
                            imageOptions = ImageOptions(
                                contentScale = ContentScale.Fit,
                                alignment = Alignment.Center
                            ),
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .padding(8.dp)
                                            .background(PurpleGrey40, RoundedCornerShape(16.dp))
                                    ) {
                                        LottieAnimation(
                                            modifier = Modifier.align(Alignment.Center),
                                            composition = composition.value,
                                            iterations = LottieConstants.IterateForever,
                                        )
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.padding(24.dp))
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Text(
                                text = "Add",
                                modifier = Modifier
                                    .clickable {
                                        if (imagePath.isNotEmpty()) {
                                            viewModel.addDog(imagePath)
                                        }
                                    }
                            )
                            Spacer(modifier = Modifier.padding(24.dp))
                            Text(
                                text = "Load New Image",
                                modifier = Modifier
                                    .clickable {
                                        viewModel.fetchDogResponse()
                                    }
                            )
                            Spacer(modifier = Modifier.padding(24.dp))
                            Text(
                                text = "To List",
                                modifier = Modifier
                                    .clickable {
                                        viewModel.getAllDogs()
                                    }
                            )
                        }
                    }
                }
            }

            LaunchedEffect(viewModel) {
                viewModel.response.observe(this@MainActivity) { response ->
                    when (response) {
                        is NetworkResult.Success -> {
                            Log.i("mLogNetworkResult", "Success ${response.data}")
                            imagePath = response.data?.message.toString()
                            // bind data to the view
                        }

                        is NetworkResult.Error -> {
                            Log.e("mLogNetworkResult", "Error ${response.message}")
                            // show error message
                        }

                        is NetworkResult.Loading -> {
                            Log.i("mLogNetworkResult", "Loading")
                            // show a progress bar
                        }
                    }
                }
            }

            LaunchedEffect(viewModel) {
                viewModel.dogsFlow.collectLatest {
                    if (!it.isNullOrEmpty()) {
                        it.forEach { data ->
                            Log.i("mLogHilt", "$data")
                        }
                    }
                }
            }
        }
    }
}