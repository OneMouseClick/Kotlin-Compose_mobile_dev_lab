package com.example.jokes

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.jokes.data.DataStoreManager
import com.example.jokes.data.JokeRepository
import com.example.jokes.navigation.ScreenLifecycleLogger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(dataStore: DataStoreManager, onBack: () -> Unit) {
    ScreenLifecycleLogger("SettingsFragment")

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var darkTheme by remember { mutableStateOf(false) }
    var fontScale by remember { mutableStateOf(1.0f) }
    var backupExists by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        name = dataStore.userName.first()
        darkTheme = dataStore.isDarkTheme.first()
        fontScale = dataStore.fontScale.first()
        backupExists = dataStore.backupExists.first()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // === Слой 1: скроллируемый контент ===
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(
                    top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 80.dp,
                    bottom = 16.dp
                )
        ) {
            Text("Профиль", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя / Никнейм") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = gender,
                onValueChange = { gender = it },
                label = { Text("Пол") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = age,
                onValueChange = { if (it.all(Char::isDigit)) age = it },
                label = { Text("Возраст") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (age.isNotEmpty() && age.toInt() <= 0) {
                        Toast.makeText(context, "Возраст должен быть положительным", Toast.LENGTH_SHORT).show()
                    } else {
                        scope.launch {
                            dataStore.saveProfile(name, gender, age)
                            Toast.makeText(context, "Профиль сохранён", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Сохранить профиль") }

            Spacer(Modifier.height(24.dp))

            Text("Оформление", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Тёмная тема")
                Switch(
                    checked = darkTheme,
                    onCheckedChange = {
                        darkTheme = it
                        scope.launch { dataStore.setDarkTheme(it) }
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
            Text("Размер шрифта: ${"%.1f".format(fontScale)}")
            Slider(
                value = fontScale,
                onValueChange = { fontScale = it },
                onValueChangeFinished = {
                    scope.launch { dataStore.setFontScale(fontScale) }
                },
                valueRange = 0.8f..1.6f
            )

            Spacer(Modifier.height(24.dp))

            Text("Резервная копия", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                if (backupExists) "Копия найдена на устройстве"
                else "Копия отсутствует — создайте её для работы офлайн",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        val data = JokeRepository.jokes.joinToString("|") { it.text }
                        dataStore.createBackup(data)
                        backupExists = true
                        Toast.makeText(context, "Копия создана", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Создать копию") }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        val data = dataStore.restoreBackup()
                        if (data.isBlank()) {
                            Toast.makeText(context, "Копия пуста", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Данные восстановлены (${data.length} симв.)", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = backupExists,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Восстановить из копии") }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    scope.launch {
                        dataStore.deleteBackup()
                        backupExists = false
                        Toast.makeText(context, "Копия удалена", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = backupExists,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Удалить копию") }

            Spacer(Modifier.height(16.dp))
        }

        // === Слой 2: шапка с кнопкой "Назад" поверх всего ===
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.statusBars) // отступ от status bar
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
                "Настройки",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}