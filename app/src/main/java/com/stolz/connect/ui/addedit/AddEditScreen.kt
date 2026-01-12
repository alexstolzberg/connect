package com.stolz.connect.ui.addedit

import android.Manifest
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.stolz.connect.domain.model.ConnectionMethod
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.stolz.connect.platform.PermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    connectionId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: AddEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()
    val context = LocalContext.current
    
    var shouldLaunchContactPicker by remember { mutableStateOf(false) }
    
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let {
            // Check permission before querying
            if (PermissionHelper.hasReadContactsPermission(context)) {
                val cursor = context.contentResolver.query(it, null, null, null, null)
                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        val name = c.getString(nameIndex)
                        
                        val idIndex = c.getColumnIndex(ContactsContract.Contacts._ID)
                        val contactId = c.getString(idIndex)
                        
                        val phoneCursor = context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(contactId),
                            null
                        )
                        phoneCursor?.use { pc ->
                            if (pc.moveToFirst()) {
                                val phoneIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                val phoneNumber = pc.getString(phoneIndex)
                                viewModel.updateContactName(name)
                                viewModel.updateContactPhoneNumber(phoneNumber)
                            }
                        }
                    }
                }
            }
        }
        shouldLaunchContactPicker = false
    }
    
    // Permission launcher for READ_CONTACTS
    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            shouldLaunchContactPicker = true
        }
    }
    
    LaunchedEffect(shouldLaunchContactPicker) {
        if (shouldLaunchContactPicker) {
            contactPickerLauncher.launch(null)
        }
    }
    
    fun launchContactPicker() {
        if (PermissionHelper.hasReadContactsPermission(context)) {
            contactPickerLauncher.launch(null)
        } else {
            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }
    
    LaunchedEffect(saveResult) {
        if (saveResult is SaveResult.Success) {
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (connectionId != null) "Edit Connection" else "Add Connection") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Contact selection
            OutlinedButton(
                onClick = {
                    launchContactPicker()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Person, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pick from Contacts")
            }
            
            // Contact name
            OutlinedTextField(
                value = uiState.contactName,
                onValueChange = viewModel::updateContactName,
                label = { Text("Contact Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Phone number
            OutlinedTextField(
                value = uiState.contactPhoneNumber,
                onValueChange = viewModel::updateContactPhoneNumber,
                label = { Text("Phone Number *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Reminder frequency
            Text("Reminder Frequency (days)", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.updateReminderFrequencyDays(7) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.reminderFrequencyDays == 7) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Text("Weekly")
                }
                Button(
                    onClick = { viewModel.updateReminderFrequencyDays(14) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.reminderFrequencyDays == 14) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Text("Bi-weekly")
                }
                Button(
                    onClick = { viewModel.updateReminderFrequencyDays(30) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.reminderFrequencyDays == 30) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Text("Monthly")
                }
            }
            OutlinedTextField(
                value = uiState.reminderFrequencyDays.toString(),
                onValueChange = { viewModel.updateReminderFrequencyDays(it.toIntOrNull() ?: 7) },
                label = { Text("Custom (days)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Preferred method
            Text("Preferred Method", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.preferredMethod == ConnectionMethod.CALL,
                    onClick = { viewModel.updatePreferredMethod(ConnectionMethod.CALL) },
                    label = { Text("Call") }
                )
                FilterChip(
                    selected = uiState.preferredMethod == ConnectionMethod.MESSAGE,
                    onClick = { viewModel.updatePreferredMethod(ConnectionMethod.MESSAGE) },
                    label = { Text("Message") }
                )
                FilterChip(
                    selected = uiState.preferredMethod == ConnectionMethod.BOTH,
                    onClick = { viewModel.updatePreferredMethod(ConnectionMethod.BOTH) },
                    label = { Text("Both") }
                )
            }
            
            // Birthday (optional)
            var showDatePicker by remember { mutableStateOf(false) }
            
            Text("Birthday (optional)", style = MaterialTheme.typography.labelLarge)
            if (uiState.birthday != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(uiState.birthday),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextButton(onClick = { viewModel.updateBirthday(null) }) {
                        Text("Clear")
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Set Birthday")
                }
            }
            
            // Date picker dialog
            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = uiState.birthday?.time
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let {
                                    viewModel.updateBirthday(Date(it))
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
            
            // Notes
            OutlinedTextField(
                value = uiState.notes ?: "",
                onValueChange = { viewModel.updateNotes(if (it.isBlank()) null else it) },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Error message
            if (saveResult is SaveResult.Error) {
                Text(
                    text = (saveResult as SaveResult.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Save button
            Button(
                onClick = viewModel::saveConnection,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Connection")
            }
        }
    }
}
