package com.example.jokes.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.jokes.network.JokeDto
import com.example.jokes.network.KtorClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object JokeApiRepository {

    suspend fun loadJokes(context: Context, amount: Int = 3): Result<List<Joke>> =
        withContext(Dispatchers.IO) {
            if (!isNetworkAvailable(context)) {
                return@withContext Result.failure(IOException("Нет подключения к интернету"))
            }

            try {
                val jokes = KtorClient.getJokes(amount)
                    .mapNotNull { it.toJoke() }
                    .distinctBy { it.id }

                if (jokes.isEmpty()) {
                    Result.failure(IOException("Сервер вернул пустой список"))
                } else {
                    Result.success(jokes)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun JokeDto.toJoke(): Joke? {
        val jokeText = joke ?: listOfNotNull(setup, delivery).joinToString("\n\n")
        if (jokeText.isBlank()) return null

        return Joke(
            id = id,
            text = jokeText,
            category = category.toDisplayCategory(),
            rating = 0
        )
    }

    private fun String.toDisplayCategory(): String = when (this) {
        "Programming" -> "Программирование"
        "Misc" -> "Разное"
        "Dark" -> "Чёрный юмор"
        "Pun" -> "Каламбур"
        "Spooky" -> "Страшное"
        "Christmas" -> "Новогоднее"
        else -> ifBlank { "Шутка" }
    }
}
