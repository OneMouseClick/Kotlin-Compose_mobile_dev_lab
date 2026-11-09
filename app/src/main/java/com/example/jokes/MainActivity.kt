package com.example.jokes

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.jokes.data.DataStoreManager
import com.example.jokes.navigation.Screen
import com.example.jokes.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate")
        setContent {
            val dataStore = DataStoreManager(applicationContext)
            val darkTheme by dataStore.isDarkTheme.collectAsState(initial = false)
            val fontScale by dataStore.fontScale.collectAsState(initial = 1.0f)

            MyApplicationTheme(darkTheme = darkTheme, fontScale = fontScale) {
                AppNavigation(dataStore = dataStore)
            }
        }
    }

    override fun onStart() { super.onStart(); Log.d("MainActivity", "onStart") }
    override fun onResume() { super.onResume(); Log.d("MainActivity", "onResume") }
    override fun onPause() { super.onPause(); Log.d("MainActivity", "onPause") }
    override fun onStop() { super.onStop(); Log.d("MainActivity", "onStop") }
    override fun onDestroy() { super.onDestroy(); Log.d("MainActivity", "onDestroy") }
    override fun onRestart() { super.onRestart(); Log.d("MainActivity", "onRestart") }
}

/**
 * Данные зарегистрированного пользователя, которые хранятся В ПАМЯТИ (rememberSaveable),
 * а не в DataStore. Живут, пока живо приложение (переживают поворот экрана).
 */
data class RegisteredUser(
    val name: String,
    val email: String,
    val password: String
)

@Composable
fun AppNavigation(dataStore: DataStoreManager) {
    val backStack = rememberNavBackStack(Screen.Start())

    // Храним зарегистрированного пользователя в памяти
    var registeredUser by rememberSaveable { mutableStateOf<RegisteredUser?>(null) }

    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        entryProvider = entryProvider {

            entry<Screen.Start> { route ->
                StartScreen(
                    name = route.name,
                    registeredUser = registeredUser,
                    onRegister = { backStack.add(Screen.Registration) },
                    onLoginSuccess = { loggedInName ->
                        backStack.clear()
                        backStack.add(Screen.Start(name = loggedInName))
                        backStack.add(Screen.Content(name = loggedInName))
                    },
                    onLogout = {
                        backStack.clear()
                        backStack.add(Screen.Start())
                    }
                )
            }

            entry<Screen.Registration> {
                RegistrationScreen(
                    onRegistered = { newUser ->
                        registeredUser = newUser
                        // После регистрации — возврат на Start (анонимный),
                        // чтобы пользователь вошёл, введя email + пароль
                        backStack.clear()
                        backStack.add(Screen.Start())
                    },
                    onBack = { if (backStack.size > 1) backStack.removeLastOrNull() }
                )
            }

            entry<Screen.Content> { route ->
                ContentScreen(
                    onSettings = { backStack.add(Screen.Settings(name = route.name)) },
                    onBack = { if (backStack.size > 1) backStack.removeLastOrNull() }
                )
            }

            entry<Screen.Settings> { route ->
                SettingsScreen(
                    dataStore = dataStore,
                    onBack = { if (backStack.size > 1) backStack.removeLastOrNull() }
                )
            }
        }
    )
}
