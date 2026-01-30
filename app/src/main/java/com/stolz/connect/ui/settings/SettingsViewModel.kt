package com.stolz.connect.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stolz.connect.data.preferences.NotificationPreferences
import com.stolz.connect.data.preferences.ThemeMode
import com.stolz.connect.data.preferences.ThemePreferences
import com.stolz.connect.data.repository.ConnectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themePreferences: ThemePreferences,
    private val notificationPreferences: NotificationPreferences,
    private val connectionRepository: ConnectionRepository
) : ViewModel() {

    private val _themeMode = MutableStateFlow(themePreferences.getThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    val notificationsEnabled: StateFlow<Boolean> = notificationPreferences.getNotificationsEnabledFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = notificationPreferences.areNotificationsEnabled()
        )

    private val _defaultReminderTime = MutableStateFlow(notificationPreferences.getDefaultReminderTime())
    val defaultReminderTime: StateFlow<String> = _defaultReminderTime.asStateFlow()

    init {
        viewModelScope.launch {
            _themeMode.value = themePreferences.getThemeMode()
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.setThemeMode(mode)
            _themeMode.value = mode
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        notificationPreferences.setNotificationsEnabled(enabled)
        viewModelScope.launch {
            if (enabled) {
                connectionRepository.rescheduleAllNotifications()
            } else {
                connectionRepository.cancelAllScheduledNotifications()
            }
        }
    }

    fun cancelAllScheduledNotifications() {
        viewModelScope.launch {
            connectionRepository.cancelAllScheduledNotifications()
        }
    }

    fun rescheduleAllNotifications() {
        viewModelScope.launch {
            connectionRepository.rescheduleAllNotifications()
        }
    }

    fun setDefaultReminderTime(time: String) {
        notificationPreferences.setDefaultReminderTime(time)
        _defaultReminderTime.value = time
        viewModelScope.launch {
            if (notificationPreferences.areNotificationsEnabled()) {
                connectionRepository.rescheduleAllNotifications()
            }
        }
    }
}
