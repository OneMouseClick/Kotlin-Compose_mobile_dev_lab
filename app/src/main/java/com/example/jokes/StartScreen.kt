package com.example.jokes

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.jokes.navigation.ScreenLifecycleLogger

@Composable
fun StartScreen(
    name: String?,
    registeredUser: RegisteredUser?,
    onRegister: () -> Unit,
    onLoginSuccess: (String) -> Unit,
    onLogout: () -> Unit
) {
    ScreenLifecycleLogger("StartFragment")

    val context = LocalContext.current

    var loginInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEmotions,
                contentDescription = "Логотип",
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text("Шутки", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text(
                "Свежие шутки каждый день — прямо в твоём кармане",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            if (name.isNullOrBlank()) {
                // === Форма входа ===
                OutlinedTextField(
                    value = loginInput,
                    onValueChange = { loginInput = it },
                    label = { Text("Логин (email)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Пароль") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        when {
                            loginInput.isBlank() || passwordInput.isBlank() ->
                                Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()

                            !loginInput.contains("@") ->
                                Toast.makeText(context, "Введите корректный email", Toast.LENGTH_SHORT).show()

                            registeredUser == null ->
                                Toast.makeText(context, "Сначала зарегистрируйтесь", Toast.LENGTH_SHORT).show()

                            loginInput.trim() != registeredUser.email ||
                                    passwordInput != registeredUser.password ->
                                Toast.makeText(context, "Неверный логин или пароль", Toast.LENGTH_SHORT).show()

                            else -> {
                                Toast.makeText(context, "Добро пожаловать, ${registeredUser.name}!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess(registeredUser.name)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Войти") }

                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onRegister, modifier = Modifier.fillMaxWidth()) {
                    Text("Регистрация")
                }
            } else {
                // === Приветствие ===
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Привет, $name!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(24.dp))
                OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                    Text("Выйти из аккаунта")
                }
            }
        }
    }
}