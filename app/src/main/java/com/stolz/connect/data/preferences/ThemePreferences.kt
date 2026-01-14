package com.stolz.connect.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "theme_preferences",
        Context.MODE_PRIVATE
    )
    
    private val THEME_MODE_KEY = "theme_mode"
    
    fun getThemeMode(): ThemeMode {
        val modeString = prefs.getString(THEME_MODE_KEY, ThemeMode.SYSTEM.name)
        return try {
            ThemeMode.valueOf(modeString ?: ThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }
    
    fun getThemeModeFlow(): Flow<ThemeMode> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == THEME_MODE_KEY) {
                trySend(getThemeMode())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        
        // Emit initial value
        trySend(getThemeMode())
        
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }
    
    fun setThemeMode(mode: ThemeMode) {
        prefs.edit {
            putString(THEME_MODE_KEY, mode.name)
        }
    }
}
