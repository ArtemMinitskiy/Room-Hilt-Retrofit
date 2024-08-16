package com.project.room_hilt_retrofit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

//Every app must contain a class, which inherits the Application Android class, which is annotated by @HiltAndroidApp.
//This class is used by Hilt’s code generator, which makes all the components lifecycle aware.
//From this point, the Hilt is fully integrated into our app and we can start using it for DI.
@HiltAndroidApp
class App: Application()