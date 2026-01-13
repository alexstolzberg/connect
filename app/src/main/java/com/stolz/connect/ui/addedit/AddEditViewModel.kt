package com.stolz.connect.ui.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stolz.connect.data.repository.ConnectionRepository
import com.stolz.connect.domain.model.ConnectionMethod
import com.stolz.connect.domain.model.ScheduledConnection
import com.stolz.connect.util.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    
    private val connectionId: Long? = savedStateHandle.get<Long>("connectionId")?.takeIf { it > 0 }
    
    private val _uiState = MutableStateFlow(
        AddEditUiState(
            contactName = "",
            contactPhoneNumber = null,
            contactEmail = null,
            contactPhotoUri = null,
            contactId = null,
            reminderFrequencyDays = 7,
            preferredMethod = ConnectionMethod.BOTH,
            reminderTime = null,
            notes = null,
            birthday = null,
            promptOnBirthday = true
        )
    )
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()
    
    private val _saveResult = MutableStateFlow<SaveResult?>(null)
    val saveResult: StateFlow<SaveResult?> = _saveResult.asStateFlow()
    
    init {
        android.util.Log.d("AddEditViewModel", "Init: connectionId from savedStateHandle = ${savedStateHandle.get<Long>("connectionId")}, filtered = $connectionId")
        if (connectionId != null && connectionId > 0) {
            loadConnection(connectionId)
        } else {
            // Set default next reminder date to today (user can change it)
            val calendar = Calendar.getInstance()
            _uiState.value = _uiState.value.copy(
                nextReminderDate = calendar.time
            )
        }
    }
    
    private fun loadConnection(id: Long) {
        viewModelScope.launch {
            val connection = connectionRepository.getConnectionById(id)
            connection?.let {
                _uiState.value = AddEditUiState(
                    contactName = it.contactName,
                    contactPhoneNumber = it.contactPhoneNumber,
                    contactEmail = it.contactEmail,
                    contactPhotoUri = it.contactPhotoUri,
                    contactId = it.contactId,
                    reminderFrequencyDays = it.reminderFrequencyDays,
                    preferredMethod = it.preferredMethod,
                    reminderTime = it.reminderTime,
                    notes = it.notes,
                    birthday = it.birthday,
                    promptOnBirthday = it.promptOnBirthday,
                    nextReminderDate = it.nextReminderDate
                )
            }
        }
    }
    
    fun updateContactName(name: String) {
        _uiState.value = _uiState.value.copy(contactName = name)
    }
    
    fun updateContactPhoneNumber(phone: String?) {
        val cleaned = phone?.takeIf { it.isNotBlank() }
        _uiState.value = _uiState.value.copy(contactPhoneNumber = cleaned)
    }
    
    fun updateContactEmail(email: String?) {
        val cleaned = email?.takeIf { it.isNotBlank() }
        _uiState.value = _uiState.value.copy(contactEmail = cleaned)
    }
    
    fun updateContactPhotoUri(photoUri: String?) {
        _uiState.value = _uiState.value.copy(contactPhotoUri = photoUri)
    }
    
    fun updateContactId(contactId: String?) {
        _uiState.value = _uiState.value.copy(contactId = contactId)
    }
    
    fun updateReminderFrequencyDays(days: Int) {
        _uiState.value = _uiState.value.copy(reminderFrequencyDays = days)
    }
    
    fun updateReminderFrequencyDaysFromString(daysString: String) {
        // Allow empty string, but default to current value if invalid
        if (daysString.isBlank()) {
            // Keep current value when field is empty
            return
        }
        val days = daysString.toIntOrNull() ?: _uiState.value.reminderFrequencyDays
        _uiState.value = _uiState.value.copy(reminderFrequencyDays = days)
    }
    
    fun updatePreferredMethod(method: ConnectionMethod) {
        _uiState.value = _uiState.value.copy(preferredMethod = method)
    }
    
    fun updateReminderTime(time: String?) {
        _uiState.value = _uiState.value.copy(reminderTime = time)
    }
    
    fun updateNotes(notes: String?) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }
    
    fun updateNextReminderDate(date: Date) {
        _uiState.value = _uiState.value.copy(nextReminderDate = date)
    }
    
    fun updateBirthday(date: Date?) {
        _uiState.value = _uiState.value.copy(birthday = date)
    }
    
    fun updatePromptOnBirthday(prompt: Boolean) {
        _uiState.value = _uiState.value.copy(promptOnBirthday = prompt)
    }
    
    fun saveConnection() {
        android.util.Log.d("AddEditViewModel", "saveConnection() called")
        val state = _uiState.value
        android.util.Log.d("AddEditViewModel", "State: name=${state.contactName}, phone=${state.contactPhoneNumber}, isEdit=${connectionId != null}")
        
        // Validate fields
        if (state.contactName.isBlank() || (state.contactPhoneNumber.isNullOrBlank() && state.contactEmail.isNullOrBlank())) {
            android.util.Log.w("AddEditViewModel", "Validation failed: blank fields")
            _saveResult.value = SaveResult.Error("Please provide at least a name and either a phone number or email")
            return
        }
        
        // Validate phone number if provided
        if (!state.contactPhoneNumber.isNullOrBlank() && !ValidationUtils.isValidPhone(state.contactPhoneNumber)) {
            _saveResult.value = SaveResult.Error("Please enter a valid phone number")
            return
        }
        
        // Validate email if provided
        if (!state.contactEmail.isNullOrBlank() && !ValidationUtils.isValidEmail(state.contactEmail)) {
            _saveResult.value = SaveResult.Error("Please enter a valid email address")
            return
        }
        
        viewModelScope.launch {
            try {
                android.util.Log.d("AddEditViewModel", "Starting save process...")
                // Ensure nextReminderDate is set - if not provided, set to today
                val nextDate = state.nextReminderDate ?: Date()
                android.util.Log.d("AddEditViewModel", "Next reminder date: $nextDate")
                
                val connection = ScheduledConnection(
                    id = connectionId ?: 0,
                    contactName = state.contactName,
                    contactPhoneNumber = state.contactPhoneNumber,
                    contactEmail = state.contactEmail,
                    contactPhotoUri = state.contactPhotoUri,
                    contactId = state.contactId,
                    reminderFrequencyDays = state.reminderFrequencyDays,
                    preferredMethod = state.preferredMethod,
                    reminderTime = state.reminderTime,
                    notes = state.notes,
                    birthday = state.birthday,
                    promptOnBirthday = state.promptOnBirthday,
                    nextReminderDate = nextDate,
                    isActive = true
                )
                
                android.util.Log.d("AddEditViewModel", "Created connection object: ${connection.contactName}, ID: ${connection.id}, Active: ${connection.isActive}")
                
                if (connectionId != null) {
                    android.util.Log.d("AddEditViewModel", "Updating existing connection")
                    connectionRepository.updateConnection(connection)
                } else {
                    android.util.Log.d("AddEditViewModel", "Inserting new connection")
                    val insertedId = connectionRepository.insertConnection(connection)
                    android.util.Log.d("AddEditViewModel", "Insert returned ID: $insertedId")
                    // Verify the insert succeeded
                    if (insertedId <= 0) {
                        android.util.Log.e("AddEditViewModel", "Insert failed: returned ID <= 0")
                        throw Exception("Failed to insert connection")
                    }
                }
                
                android.util.Log.d("AddEditViewModel", "Save successful, waiting before setting result...")
                // Small delay to ensure database transaction completes
                kotlinx.coroutines.delay(200)
                _saveResult.value = SaveResult.Success
                android.util.Log.d("AddEditViewModel", "Save result set to Success")
            } catch (e: Exception) {
                android.util.Log.e("AddEditViewModel", "Save failed with exception", e)
                _saveResult.value = SaveResult.Error(e.message ?: "Failed to save connection")
            }
        }
    }
    
    fun clearSaveResult() {
        _saveResult.value = null
    }
}

data class AddEditUiState(
    val contactName: String,
    val contactPhoneNumber: String? = null,
    val contactEmail: String? = null,
    val contactPhotoUri: String? = null,
    val contactId: String? = null,
    val reminderFrequencyDays: Int,
    val preferredMethod: ConnectionMethod,
    val reminderTime: String?,
    val notes: String?,
    val birthday: Date? = null,
    val promptOnBirthday: Boolean = true,
    val nextReminderDate: Date? = null
)

sealed class SaveResult {
    object Success : SaveResult()
    data class Error(val message: String) : SaveResult()
}
