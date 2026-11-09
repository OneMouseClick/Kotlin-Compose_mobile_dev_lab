package com.example.jokes.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.IOException

object KtorClient {

    private const val BASE_URL = "https://v2.jokeapi.dev"

    private val client: HttpClient by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 10_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 10_000
            }
        }
    }

    suspend fun getJokes(amount: Int = 3): List<JokeDto> {
        val response: HttpResponse = client.get("$BASE_URL/joke/Programming") {
            parameter("type", "single")
            parameter("amount", amount.coerceIn(1, 10))
        }

        if (!response.status.isSuccess()) {
            throw IOException("Ошибка сервера: ${response.status.value}")
        }

        val body: JokeResponse = response.body()
        if (body.error) {
            throw IOException("Сервер вернул ошибку")
        }

        return body.jokes
    }
}
