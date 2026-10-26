package com.example.jokes

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.jokes.navigation.ScreenLifecycleLogger

@Composable
fun RegistrationScreen(
    onRegistered: (RegisteredUser) -> Unit,
    onBack: () -> Unit
) {
    ScreenLifecycleLogger("RegistrationFragment")

    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Регистрация", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Имя") },
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = login, onValueChange = { login = it },
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Пароль (мин. 6 символов)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = confirm, onValueChange = { confirm = it },
                label = { Text("Подтверждение пароля") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(), singleLine = true
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    when {
                        name.isBlank() -> error = "Введите имя"
                        !login.contains("@") -> error = "Введите корректный email"
                        password.length < 6 -> error = "Пароль должен быть не менее 6 символов"
                        password != confirm -> error = "Пароли не совпадают"
                        else -> {
                            Toast.makeText(context, "Регистрация успешна", Toast.LENGTH_SHORT).show()
                            onRegistered(
                                RegisteredUser(
                                    name = name.trim(),
                                    email = login.trim(),
                                    password = password
                                )
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Зарегистрироваться") }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Назад")
            }
        }
    }

    error?.let {
        AlertDialog(
            onDismissRequest = { error = null },
            title = { Text("Ошибка ввода") },
            text = { Text(it) },
            confirmButton = {
                TextButton(onClick = { error = null }) { Text("ОК") }
            }
        )
    }
}