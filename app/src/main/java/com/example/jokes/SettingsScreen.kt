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
import com.example.jokes.data.BackupFileInfo
import com.example.jokes.data.DataStoreManager
import com.example.jokes.data.JokeApiRepository
import com.example.jokes.data.JokeBackupManager
import com.example.jokes.data.JokeRepository
import com.example.jokes.data.SharedPreferencesManager
import com.example.jokes.navigation.ScreenLifecycleLogger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(dataStore: DataStoreManager, onBack: () -> Unit) {
    ScreenLifecycleLogger("SettingsFragment")

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sharedPreferences = remember { SharedPreferencesManager(context) }

    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var darkTheme by remember { mutableStateOf(false) }
    var fontScale by remember { mutableStateOf(1.0f) }
    var publicBackup by remember { mutableStateOf<BackupFileInfo?>(null) }
    var internalBackup by remember { mutableStateOf<BackupFileInfo?>(null) }
    var isBackupBusy by remember { mutableStateOf(false) }

    suspend fun refreshBackups() {
        publicBackup = JokeBackupManager.getPublicBackup(context)
        internalBackup = JokeBackupManager.getInternalBackup(context)
    }

    LaunchedEffect(Unit) {
        name = sharedPreferences.name
        gender = sharedPreferences.gender
        age = sharedPreferences.age
        darkTheme = dataStore.isDarkTheme.first()
        fontScale = dataStore.fontScale.first()
        refreshBackups()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
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
                    val parsedAge = age.toIntOrNull()
                    if (age.isNotEmpty() && (parsedAge == null || parsedAge <= 0)) {
                        Toast.makeText(context, "Возраст должен быть положительным", Toast.LENGTH_SHORT).show()
                    } else {
                        sharedPreferences.saveProfile(name, gender, age)
                        Toast.makeText(context, "Профиль сохранён", Toast.LENGTH_SHORT).show()
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
                publicBackup?.let { file ->
                    "Файл: ${file.name}\n" +
                        "Расположение: ${file.location}\n" +
                        "Размер: ${file.sizeLabel()}\n" +
                        "Изменён: ${file.dateLabel()}"
                } ?: "Файл в Downloads отсутствует",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(4.dp))
            Text(
                internalBackup?.let { file ->
                    "Внутренняя копия: ${file.name}, ${file.sizeLabel()}"
                } ?: "Внутренняя копия отсутствует",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        isBackupBusy = true
                        val jokes = JokeApiRepository.loadJokes(context, 10)
                            .getOrElse { JokeRepository.jokes }
                        JokeBackupManager.createBackup(context, jokes)
                            .onSuccess {
                                Toast.makeText(context, "Файл сохранён в Downloads", Toast.LENGTH_SHORT).show()
                                refreshBackups()
                            }
                            .onFailure {
                                Toast.makeText(context, "Ошибка: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                        isBackupBusy = false
                    }
                },
                enabled = !isBackupBusy,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (publicBackup == null) "Создать файл" else "Пересоздать файл") }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    scope.launch {
                        isBackupBusy = true
                        JokeBackupManager.deletePublicAndKeepInternal(context)
                            .onSuccess {
                                Toast.makeText(context, "Файл удалён, копия сохранена внутри приложения", Toast.LENGTH_SHORT).show()
                                refreshBackups()
                            }
                            .onFailure {
                                Toast.makeText(context, "Ошибка: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                        isBackupBusy = false
                    }
                },
                enabled = publicBackup != null && !isBackupBusy,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Удалить файл") }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        isBackupBusy = true
                        JokeBackupManager.restoreBackup(context)
                            .onSuccess {
                                Toast.makeText(context, "Файл восстановлен в Downloads", Toast.LENGTH_SHORT).show()
                                refreshBackups()
                            }
                            .onFailure {
                                Toast.makeText(context, "Ошибка: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                        isBackupBusy = false
                    }
                },
                enabled = internalBackup != null && !isBackupBusy,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Восстановить файл") }

            Spacer(Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
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
                "Настройки",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun BackupFileInfo.sizeLabel(): String =
    if (sizeBytes < 1024) "$sizeBytes Б"
    else String.format(Locale.getDefault(), "%.1f КБ", sizeBytes / 1024.0)

private fun BackupFileInfo.dateLabel(): String =
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(modifiedAtSeconds * 1000))
