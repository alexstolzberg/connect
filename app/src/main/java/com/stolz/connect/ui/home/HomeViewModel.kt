package com.stolz.connect.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stolz.connect.data.repository.ConnectionRepository
import com.stolz.connect.domain.model.ScheduledConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            // Collect from both Flows - Room Flows automatically emit when data changes
            // Also collect debug flow to see all connections
            combine(
                connectionRepository.getAllActiveConnections(),
                connectionRepository.getTodayConnections(),
                connectionRepository.getAllConnections() // Debug
            ) { allConnections, todayConnections, allDebug ->
                android.util.Log.d("HomeViewModel", "Flow emitted - All: ${allConnections.size}, Today: ${todayConnections.size}, Debug (all): ${allDebug.size}")
                if (allDebug.isNotEmpty() && allConnections.isEmpty()) {
                    android.util.Log.w("HomeViewModel", "WARNING: Found ${allDebug.size} connections in DB but 0 active ones!")
                    allDebug.forEach { conn ->
                        android.util.Log.w("HomeViewModel", "  - ${conn.contactName}, ID: ${conn.id}, Active: ${conn.isActive}")
                    }
                }
                allConnections.forEach { conn ->
                    android.util.Log.d("HomeViewModel", "Connection: ${conn.contactName}, ID: ${conn.id}, Active: ${conn.isActive}, NextDate: ${conn.nextReminderDate}")
                }
                HomeUiState(
                    allConnections = allConnections,
                    todayConnections = todayConnections
                )
            }.collect { state ->
                android.util.Log.d("HomeViewModel", "Updating UI state with ${state.allConnections.size} connections")
                _uiState.value = state
            }
        }
    }
    
    // Public function to refresh connections if needed
    // Note: Room Flows automatically emit when data changes, so this shouldn't be necessary
    // But it can be called if needed for manual refresh
    fun refreshConnections() {
        // Room Flows automatically update, but we can trigger a re-collection if needed
        // by updating the state slightly to force recomposition
        val currentState = _uiState.value
        _uiState.value = currentState.copy() // Trigger state update
    }
    
    fun deleteConnection(connection: ScheduledConnection) {
        viewModelScope.launch {
            connectionRepository.deleteConnection(connection)
        }
    }
    
    fun markAsContacted(connection: ScheduledConnection) {
        viewModelScope.launch {
            connectionRepository.markAsContacted(connection)
        }
    }
}

data class HomeUiState(
    val allConnections: List<ScheduledConnection> = emptyList(),
    val todayConnections: List<ScheduledConnection> = emptyList()
)
