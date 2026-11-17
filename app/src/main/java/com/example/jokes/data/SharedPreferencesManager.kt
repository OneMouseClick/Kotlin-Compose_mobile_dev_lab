package com.example.jokes.data

import android.content.Context

class SharedPreferencesManager(context: Context) {

    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    val name: String
        get() = preferences.getString(KEY_NAME, "").orEmpty()

    val gender: String
        get() = preferences.getString(KEY_GENDER, "").orEmpty()

    val age: String
        get() = preferences.getString(KEY_AGE, "").orEmpty()

    fun saveProfile(name: String, gender: String, age: String) {
        preferences.edit()
            .putString(KEY_NAME, name)
            .putString(KEY_GENDER, gender)
            .putString(KEY_AGE, age)
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "user_profile"
        const val KEY_NAME = "profile_name"
        const val KEY_GENDER = "profile_gender"
        const val KEY_AGE = "profile_age"
    }
}
