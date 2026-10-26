package com.example.jokes.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun ScreenLifecycleLogger(screenName: String) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val observer = remember {
        LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> Log.d(screenName, "ON_CREATE")
                Lifecycle.Event.ON_START -> Log.d(screenName, "ON_START")
                Lifecycle.Event.ON_RESUME -> Log.d(screenName, "ON_RESUME")
                Lifecycle.Event.ON_PAUSE -> Log.d(screenName, "ON_PAUSE")
                Lifecycle.Event.ON_STOP -> Log.d(screenName, "ON_STOP")
                Lifecycle.Event.ON_DESTROY -> Log.d(screenName, "ON_DESTROY")
                else -> {}
            }
        }
    }
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}