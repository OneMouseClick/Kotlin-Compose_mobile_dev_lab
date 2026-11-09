package com.example.jokes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jokes.data.Joke
import com.example.jokes.data.JokeApiRepository
import com.example.jokes.data.JokeRepository
import com.example.jokes.navigation.ScreenLifecycleLogger
import kotlinx.coroutines.launch

private const val JOKES_PER_PAGE = 3

@Composable
fun ContentScreen(onSettings: () -> Unit, onBack: () -> Unit) {
    ScreenLifecycleLogger("ContentFragment")

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var jokes by remember { mutableStateOf<List<Joke>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isOfflineFallback by remember { mutableStateOf(false) }
    var offlineMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val offlineJokes = JokeRepository.jokes

    fun addRandomOfflineJokes() {
        val usedIds = jokes.map { it.id }.toMutableSet()
        var nextId = (usedIds.maxOrNull() ?: 0) + 1
        val newJokes = offlineJokes
            .shuffled()
            .take(JOKES_PER_PAGE)
            .map { joke ->
                while (nextId in usedIds) nextId++
                val newJoke = joke.copy(id = nextId)
                usedIds += nextId
                nextId++
                newJoke
            }
        jokes = jokes + newJokes
    }

    suspend fun loadInitialJokes() {
        isLoading = true
        errorMessage = null

        JokeApiRepository.loadJokes(context, JOKES_PER_PAGE)
            .onSuccess {
                jokes = it
                isOfflineFallback = false
                offlineMessage = null
            }
            .onFailure {
                jokes = offlineJokes.take(JOKES_PER_PAGE)
                isOfflineFallback = true
                offlineMessage = "Нет подключения к интернету. Показаны сохранённые шутки."
            }

        isLoading = false
    }

    suspend fun retryConnection() {
        isLoading = true
        errorMessage = null

        JokeApiRepository.loadJokes(context, JOKES_PER_PAGE)
            .onSuccess { newJokes ->
                val existingIds = jokes.map { it.id }.toSet()
                jokes = jokes + newJokes.filter { it.id !in existingIds }
                isOfflineFallback = false
                offlineMessage = null
            }
            .onFailure {
                offlineMessage = "Нет подключения к интернету. Показаны сохранённые шутки."
            }

        isLoading = false
    }

    suspend fun loadMoreJokes() {
        isLoading = true
        errorMessage = null

        if (isOfflineFallback) {
            addRandomOfflineJokes()
        } else {
            JokeApiRepository.loadJokes(context, JOKES_PER_PAGE)
                .onSuccess { newJokes ->
                    val existingIds = jokes.map { it.id }.toSet()
                    val uniqueJokes = newJokes.filter { it.id !in existingIds }
                    if (uniqueJokes.isEmpty()) {
                        errorMessage = "Новых шуток не найдено. Попробуйте ещё раз."
                    } else {
                        jokes = jokes + uniqueJokes
                    }
                }
                .onFailure {
                    isOfflineFallback = true
                    offlineMessage = "Нет подключения к интернету. Показаны сохранённые шутки."
                    addRandomOfflineJokes()
                }
        }

        isLoading = false
    }

    LaunchedEffect(Unit) {
        loadInitialJokes()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = onBack,
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Назад")
            }
            Spacer(Modifier.width(12.dp))
            Text(
                "Шутки",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            FilledTonalButton(
                onClick = onSettings,
                modifier = Modifier.size(48.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Настройки",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        if (isLoading && jokes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (jokes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Нет данных", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(offlineMessage ?: errorMessage ?: "Проверьте подключение к интернету", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { scope.launch { retryConnection() } }) { Text("Повторить") }
            }
        } else {
            if (offlineMessage != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        offlineMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { scope.launch { retryConnection() } }) {
                        Text("Повторить")
                    }
                }
            }
            if (errorMessage != null) {
                Text(
                    errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                items(jokes, key = { it.id }) { joke -> JokeCard(joke) }
                item {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { scope.launch { loadMoreJokes() } },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text(if (isLoading) "Загрузка..." else "Загрузить ещё")
                    }
                    Spacer(Modifier.height(88.dp))
                }
            }
        }
    }
}

@Composable
fun JokeCard(joke: Joke) {
    val container = when (joke.category) {
        "Программирование" -> MaterialTheme.colorScheme.primaryContainer
        "Работа" -> MaterialTheme.colorScheme.secondaryContainer
        "Животные" -> MaterialTheme.colorScheme.tertiaryContainer
        "Технологии" -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(joke.text, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(joke.category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (joke.rating > 0) {
                    Text("★ ${joke.rating}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}
}
