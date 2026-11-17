package com.example.jokes.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_settings")

class DataStoreManager(private val context: Context) {

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.THEME_DARK] ?: false
    }

    val fontScale: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.FONT_SCALE] ?: 1.0f
    }

    suspend fun setDarkTheme(dark: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.THEME_DARK] = dark }
    }

    suspend fun setFontScale(scale: Float) {
        context.dataStore.edit { it[PreferencesKeys.FONT_SCALE] = scale }
    }
}
