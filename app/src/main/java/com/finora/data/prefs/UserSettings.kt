package com.finora.data.prefs

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Singleton
class UserSettings @Inject constructor(@ApplicationContext context: Context) {
    private val preferences = context.getSharedPreferences("finora_preferences", Context.MODE_PRIVATE)

    private val _themeChoice = MutableStateFlow(preferences.getString(KEY_THEME, "system") ?: "system")
    val themeChoice: StateFlow<String> = _themeChoice

    private val _language = MutableStateFlow(preferences.getString(KEY_LANGUAGE, "en") ?: "en")
    val language: StateFlow<String> = _language

    private val _banglaNumerals = MutableStateFlow(preferences.getBoolean(KEY_BANGLA_NUMERALS, false))
    val banglaNumerals: StateFlow<Boolean> = _banglaNumerals

    fun setThemeChoice(value: String) {
        preferences.edit().putString(KEY_THEME, value).apply()
        _themeChoice.value = value
    }

    fun setLanguage(value: String) {
        preferences.edit().putString(KEY_LANGUAGE, value).apply()
        _language.value = value
    }

    fun setBanglaNumerals(value: Boolean) {
        preferences.edit().putBoolean(KEY_BANGLA_NUMERALS, value).apply()
        _banglaNumerals.value = value
    }

    private companion object {
        const val KEY_THEME = "theme"
        const val KEY_LANGUAGE = "language"
        const val KEY_BANGLA_NUMERALS = "bangla_numerals"
    }
}
