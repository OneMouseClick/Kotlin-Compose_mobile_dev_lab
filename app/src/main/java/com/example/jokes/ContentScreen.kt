package com.example.jokes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jokes.data.Joke
import com.example.jokes.data.JokeRepository
import com.example.jokes.navigation.ScreenLifecycleLogger
import kotlin.random.Random

@Composable
fun ContentScreen(onSettings: () -> Unit) {
    ScreenLifecycleLogger("ContentFragment")

    var jokes by remember { mutableStateOf(JokeRepository.jokes) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Text(
                "Шутки",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            if (jokes.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Нет данных", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("Проверьте резервную копию в настройках", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onSettings) { Text("Открыть настройки") }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    items(jokes) { joke -> JokeCard(joke) }
                    item {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                val extra = jokes.map { it.copy(id = it.id + 1000 + Random.nextInt(1000)) }
                                jokes = jokes + extra
                            },
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        ) { Text("Загрузить ещё") }
                        Spacer(Modifier.height(88.dp))
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onSettings,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Настройки")
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(joke.text, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(joke.category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("★ ${joke.rating}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            }
        }
    }
}