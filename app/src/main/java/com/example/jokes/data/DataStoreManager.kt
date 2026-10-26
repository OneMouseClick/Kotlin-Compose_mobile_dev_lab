package com.example.jokes.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "user_settings")

class DataStoreManager(private val context: Context) {

    val isDarkTheme: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.THEME_DARK] ?: false
    }

    val fontScale: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.FONT_SCALE] ?: 1.0f
    }

    val backupExists: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.BACKUP_EXISTS] ?: false
    }

    val userName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.USER_NAME] ?: ""
    }

    suspend fun saveProfile(name: String, gender: String, age: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.USER_NAME] = name
            prefs[PreferencesKeys.USER_GENDER] = gender
            prefs[PreferencesKeys.USER_AGE] = age
        }
    }

    suspend fun setDarkTheme(dark: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.THEME_DARK] = dark }
    }

    suspend fun setFontScale(scale: Float) {
        context.dataStore.edit { it[PreferencesKeys.FONT_SCALE] = scale }
    }

    suspend fun createBackup(data: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.BACKUP_DATA] = data
            prefs[PreferencesKeys.BACKUP_EXISTS] = true
        }
    }

    suspend fun deleteBackup() {
        context.dataStore.edit { prefs ->
            prefs.remove(PreferencesKeys.BACKUP_DATA)
            prefs.remove(PreferencesKeys.BACKUP_EXISTS)
        }
    }

    suspend fun restoreBackup(): String {
        val prefs = context.dataStore.data.first()
        return prefs[PreferencesKeys.BACKUP_DATA] ?: ""
    }
}