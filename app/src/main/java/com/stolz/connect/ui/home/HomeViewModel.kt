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
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _refreshTrigger = MutableStateFlow(0L)
    
    // Track manually added connections that might be outside the 7-day window
    private val _manuallyAddedConnections = MutableStateFlow<Map<Long, ScheduledConnection>>(emptyMap())
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            combine(
                connectionRepository.getAllActiveConnections(),
                connectionRepository.getTodayConnections(),
                _searchQuery,
                _refreshTrigger,
                _manuallyAddedConnections
            ) { allConnections, todayConnections, query, refreshTrigger, manuallyAdded ->
                // Filter by search query if present
                val filteredAll = if (query.isBlank()) {
                    allConnections
                } else {
                    allConnections.filter { connection ->
                        connection.contactName.contains(query, ignoreCase = true) ||
                        connection.contactPhoneNumber?.contains(query, ignoreCase = true) == true ||
                        connection.contactEmail?.contains(query, ignoreCase = true) == true
                    }
                }
                
                val filteredToday = if (query.isBlank()) {
                    todayConnections
                } else {
                    todayConnections.filter { connection ->
                        connection.contactName.contains(query, ignoreCase = true) ||
                        connection.contactPhoneNumber?.contains(query, ignoreCase = true) == true ||
                        connection.contactEmail?.contains(query, ignoreCase = true) == true
                    }
                }
                
                // Merge manually added connections with todayConnections
                // This ensures connections that were just marked as contacted appear even if outside 7-day window
                // Use manually added version if it exists (more recent), otherwise use Room Flow version
                val currentState = _uiState.value
                
                // If we have manually added connections, prefer the current state's todayConnections
                // (which may have been manually updated) over the Room Flow version
                val baseTodayConnections = if (manuallyAdded.isNotEmpty() && currentState.todayConnections.isNotEmpty()) {
                    // Prefer current state (which has manual updates) over Room Flow
                    currentState.todayConnections
                } else {
                    filteredToday
                }
                
                // Merge manually added versions into the base list
                val finalTodayConnections = (
                    baseTodayConnections.map { conn ->
                        manuallyAdded[conn.id] ?: conn
                    } + manuallyAdded.values.filter { it.id !in baseTodayConnections.map { it.id } }
                ).distinctBy { it.id }
                
                // Recalculate sections with the merged todayConnections
                val roomConnectionIds = finalTodayConnections.map { it.id }.toSet()
                val finalAllInboxConnections = (
                    finalTodayConnections.map { connection ->
                        manuallyAdded[connection.id] ?: connection
                    } + manuallyAdded.values.filter { it.id !in roomConnectionIds }
                ).distinctBy { it.id }
                
                // Organize inbox connections into sections
                val inboxSections = organizeIntoSections(finalAllInboxConnections)
                
                HomeUiState(
                    allConnections = filteredAll,
                    todayConnections = finalTodayConnections,
                    inboxSections = inboxSections,
                    refreshCounter = currentState.refreshCounter // Preserve refresh counter
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    // Public function to refresh connections if needed
    // Triggers the combine flow to re-evaluate by updating the refresh trigger
    fun refreshConnections() {
        _refreshTrigger.value = System.currentTimeMillis()
    }
    
    fun deleteConnection(connection: ScheduledConnection) {
        viewModelScope.launch {
            connectionRepository.deleteConnection(connection)
        }
    }
    
    fun markAsContacted(connection: ScheduledConnection) {
        viewModelScope.launch {
            // Calculate the new nextReminderDate
            val now = Date()
            val calendar = Calendar.getInstance().apply {
                time = now
                add(Calendar.DAY_OF_MONTH, connection.reminderFrequencyDays)
            }
            val nextReminderDate = calendar.time
            
            // Update the database
            connectionRepository.markAsContacted(connection)
            
            // Create updated connection
            val updatedConnection = connection.copy(
                lastContactedDate = now,
                nextReminderDate = nextReminderDate
            )
            
            // Add to manually added connections so it appears immediately
            // This ensures it shows up even if it's outside the 7-day window
            val currentManuallyAdded = _manuallyAddedConnections.value.toMutableMap()
            currentManuallyAdded[connection.id] = updatedConnection
            _manuallyAddedConnections.value = currentManuallyAdded
            
            // Wait a moment for the combine flow to process the manuallyAdded update
            // Then immediately update UI state to show the connection in "Upcoming"
            kotlinx.coroutines.delay(50) // Small delay to let combine flow process manuallyAdded
            
            val currentState = _uiState.value
            
            // Remove the old connection from todayConnections and add the updated one
            val updatedTodayConnections = currentState.todayConnections.filter { it.id != connection.id } + updatedConnection
            val updatedRoomConnectionIds = updatedTodayConnections.map { it.id }.toSet()
            
            val allInboxConnections = (
                updatedTodayConnections.map { conn ->
                    currentManuallyAdded[conn.id] ?: conn
                } + currentManuallyAdded.values.filter { it.id !in updatedRoomConnectionIds }
            ).distinctBy { it.id }
            
            // Organize into sections
            val updatedSections = organizeIntoSections(allInboxConnections)
            
            // Update state immediately with both updated sections and todayConnections
            // Increment refreshCounter to force UI recomposition
            _uiState.value = currentState.copy(
                todayConnections = updatedTodayConnections,
                inboxSections = updatedSections,
                refreshCounter = System.currentTimeMillis() // Force recomposition
            )
            
            // Force refresh to ensure Room Flow updates are reflected when they arrive
            refreshConnections()
            
            // Remove from manually added after a delay to let Room Flow catch up
            // If the connection is within 7 days, Room Flow will include it
            // If it's outside 7 days, we'll keep it in manually added so it stays visible
            viewModelScope.launch {
                kotlinx.coroutines.delay(2000) // Wait 2 seconds for Room Flow to emit
                val roomConnections = _uiState.value.todayConnections
                val isInRoomFlow = roomConnections.any { it.id == connection.id }
                
                // Only remove from manually added if it's now in Room Flow results
                // This means Room Flow has picked it up and will maintain it
                if (isInRoomFlow) {
                    val updatedManuallyAdded = _manuallyAddedConnections.value.toMutableMap()
                    updatedManuallyAdded.remove(connection.id)
                    _manuallyAddedConnections.value = updatedManuallyAdded
                }
                // If it's not in Room Flow (outside 7-day window), keep it in manually added
            }
        }
    }
    
    fun snoozeReminder(connection: ScheduledConnection, snoozeDate: Date) {
        viewModelScope.launch {
            connectionRepository.snoozeReminder(connection, snoozeDate)
        }
    }
    
    private fun organizeIntoSections(connections: List<ScheduledConnection>): List<InboxViewSection> {
        val now = Date()
        val calendar = Calendar.getInstance().apply {
            time = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val todayStart = calendar.time
        
        val tomorrowCalendar = Calendar.getInstance().apply {
            time = todayStart
            add(Calendar.DAY_OF_MONTH, 1)
        }
        val tomorrowStart = tomorrowCalendar.time
        
        val pastDue = mutableListOf<ScheduledConnection>()
        val today = mutableListOf<ScheduledConnection>()
        val upcoming = mutableListOf<ScheduledConnection>()
        
        connections.forEach { connection ->
            val reminderCalendar = Calendar.getInstance().apply {
                time = connection.nextReminderDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val reminderStart = reminderCalendar.time
            
            when {
                reminderStart.before(todayStart) -> pastDue.add(connection)
                reminderStart.before(tomorrowStart) -> today.add(connection)
                else -> upcoming.add(connection)
            }
        }
        
        val sections = mutableListOf<InboxViewSection>()
        if (pastDue.isNotEmpty()) {
            sections.add(InboxViewSection("Past Due", pastDue))
        }
        if (today.isNotEmpty()) {
            sections.add(InboxViewSection("Today", today))
        }
        if (upcoming.isNotEmpty()) {
            sections.add(InboxViewSection("Upcoming", upcoming))
        }
        
        return sections
    }
}

data class InboxViewSection(
    val title: String,
    val connections: List<ScheduledConnection>
)

data class HomeUiState(
    val allConnections: List<ScheduledConnection> = emptyList(),
    val todayConnections: List<ScheduledConnection> = emptyList(),
    val inboxSections: List<InboxViewSection> = emptyList(),
    val refreshCounter: Long = 0L // Force recomposition when changed
)
