package com.example.jokes.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : NavKey {

    @Serializable
    data class Start(
        val name: String? = null
    ) : Screen

    @Serializable
    data object Registration : Screen

    @Serializable
    data class Content(
        val name: String? = null
    ) : Screen

    @Serializable
    data class Settings(
        val name: String? = null
    ) : Screen
}