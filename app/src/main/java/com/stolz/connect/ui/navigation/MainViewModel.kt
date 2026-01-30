package com.stolz.connect.ui.navigation

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
class MainViewModel @Inject constructor(
    private val themePreferences: ThemePreferences,
    private val notificationPreferences: NotificationPreferences,
    private val connectionRepository: ConnectionRepository
) : ViewModel() {

    private val _pendingConnectionId = MutableStateFlow<Long?>(null)
    val pendingConnectionId: StateFlow<Long?> = _pendingConnectionId.asStateFlow()

    private val _shouldShowNotificationPrompt = MutableStateFlow(
        !notificationPreferences.hasShownNotificationPrompt()
    )
    val shouldShowNotificationPrompt: StateFlow<Boolean> = _shouldShowNotificationPrompt.asStateFlow()

    val notificationsEnabled: StateFlow<Boolean> = notificationPreferences.getNotificationsEnabledFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = notificationPreferences.areNotificationsEnabled()
        )

    fun setPendingConnectionId(id: Long?) {
        _pendingConnectionId.value = id
    }

    fun clearPendingConnectionId() {
        _pendingConnectionId.value = null
    }

    fun dismissNotificationPrompt(enableNotifications: Boolean) {
        notificationPreferences.setHasShownNotificationPrompt(true)
        _shouldShowNotificationPrompt.value = false
        if (enableNotifications) {
            notificationPreferences.setNotificationsEnabled(true)
            viewModelScope.launch {
                connectionRepository.rescheduleAllNotifications()
            }
        }
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        dismissNotificationPrompt(enableNotifications = granted)
    }

    val themeMode: StateFlow<ThemeMode> = themePreferences.getThemeModeFlow()
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = themePreferences.getThemeMode()
        )
}
