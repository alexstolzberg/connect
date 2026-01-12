package com.stolz.connect.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stolz.connect.data.repository.ConnectionRepository
import com.stolz.connect.domain.model.ScheduledConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionDetailsViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val connectionId: Long = savedStateHandle.get<Long>("connectionId") ?: 0
    
    private val _uiState = MutableStateFlow<ConnectionDetailsUiState>(ConnectionDetailsUiState.Loading)
    val uiState: StateFlow<ConnectionDetailsUiState> = _uiState.asStateFlow()
    
    private val _deleteResult = MutableStateFlow<DeleteResult?>(null)
    val deleteResult: StateFlow<DeleteResult?> = _deleteResult.asStateFlow()
    
    init {
        loadConnection()
    }
    
    private fun loadConnection() {
        viewModelScope.launch {
            val connection = connectionRepository.getConnectionById(connectionId)
            if (connection != null) {
                _uiState.value = ConnectionDetailsUiState.Success(connection)
            } else {
                _uiState.value = ConnectionDetailsUiState.Error("Connection not found")
            }
        }
    }
    
    fun refreshConnection() {
        loadConnection()
    }
    
    fun markAsContacted() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ConnectionDetailsUiState.Success) {
                try {
                    connectionRepository.markAsContacted(currentState.connection)
                    loadConnection() // Reload to get updated dates
                } catch (e: Exception) {
                    _uiState.value = ConnectionDetailsUiState.Error(e.message ?: "Failed to mark as contacted")
                }
            }
        }
    }
    
    fun deleteConnection() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ConnectionDetailsUiState.Success) {
                try {
                    connectionRepository.deleteConnection(currentState.connection)
                    _deleteResult.value = DeleteResult.Success
                } catch (e: Exception) {
                    _deleteResult.value = DeleteResult.Error(e.message ?: "Failed to delete connection")
                }
            }
        }
    }
    
    fun clearDeleteResult() {
        _deleteResult.value = null
    }
}

sealed class ConnectionDetailsUiState {
    object Loading : ConnectionDetailsUiState()
    data class Success(val connection: ScheduledConnection) : ConnectionDetailsUiState()
    data class Error(val message: String) : ConnectionDetailsUiState()
}

sealed class DeleteResult {
    object Success : DeleteResult()
    data class Error(val message: String) : DeleteResult()
}
