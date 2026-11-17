package com.example.jokes.data

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey

object PreferencesKeys {
    val THEME_DARK = booleanPreferencesKey("theme_dark")
    val FONT_SCALE = floatPreferencesKey("font_scale")
}
