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

@Singleton
class NotificationPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun areNotificationsEnabled(): Boolean =
        prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, DEFAULT_ENABLED)

    fun getNotificationsEnabledFlow(): Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_NOTIFICATIONS_ENABLED) {
                trySend(areNotificationsEnabled())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(areNotificationsEnabled())
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit {
            putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
        }
    }

    fun hasShownNotificationPrompt(): Boolean =
        prefs.getBoolean(KEY_PROMPT_SHOWN, false)

    fun setHasShownNotificationPrompt(shown: Boolean) {
        prefs.edit {
            putBoolean(KEY_PROMPT_SHOWN, shown)
        }
    }

    companion object {
        const val PREFS_NAME = "notification_preferences"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_PROMPT_SHOWN = "notification_prompt_shown"
        const val DEFAULT_ENABLED = false
    }
}
