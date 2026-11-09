package com.example.jokes.network

import kotlinx.serialization.Serializable

@Serializable
data class JokeResponse(
    val error: Boolean = false,
    val amount: Int = 0,
    val jokes: List<JokeDto> = emptyList()
)

@Serializable
data class JokeDto(
    val id: Int,
    val category: String = "",
    val type: String = "",
    val joke: String? = null,
    val setup: String? = null,
    val delivery: String? = null
)
