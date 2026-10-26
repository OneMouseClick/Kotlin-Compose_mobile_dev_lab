package com.example.jokes.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    val USER_NAME = stringPreferencesKey("user_name")
    val USER_GENDER = stringPreferencesKey("user_gender")
    val USER_AGE = stringPreferencesKey("user_age")

    val THEME_DARK = booleanPreferencesKey("theme_dark")
    val FONT_SCALE = floatPreferencesKey("font_scale")

    val BACKUP_EXISTS = booleanPreferencesKey("backup_exists")
    val BACKUP_DATA = stringPreferencesKey("backup_data")
}