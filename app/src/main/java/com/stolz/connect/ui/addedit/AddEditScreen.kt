package com.stolz.connect.ui.addedit

import android.Manifest
import android.content.ContentUris
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.stolz.connect.util.ValidationUtils
import com.stolz.connect.util.PhoneNumberTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
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
                        
                        // Get email
                        val emailCursor = context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                            arrayOf(contactId),
                            null
                        )
                        emailCursor?.use { ec ->
                            if (ec.moveToFirst()) {
                                val emailIndex = ec.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)
                                val email = ec.getString(emailIndex)
                                viewModel.updateContactEmail(email)
                            }
                        }
                        
                        // Get photo URI and contact ID
                        val contactUri = android.content.ContentUris.withAppendedId(
                            ContactsContract.Contacts.CONTENT_URI,
                            contactId.toLong()
                        )
                        
                        // Check if contact has a photo
                        val photoInputStream = ContactsContract.Contacts.openContactPhotoInputStream(
                            context.contentResolver,
                            contactUri
                        )
                        
                        if (photoInputStream != null) {
                            photoInputStream.close()
                            // Get lookup URI for stable photo reference (survives contact ID changes)
                            val lookupUri = ContactsContract.Contacts.getLookupUri(
                                context.contentResolver,
                                contactUri
                            )
                            // Construct photo URI by appending /photo to lookup URI
                            // If lookup URI is null, fall back to contact URI
                            val photoUriString = if (lookupUri != null) {
                                android.net.Uri.withAppendedPath(lookupUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY).toString()
                            } else {
                                android.net.Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY).toString()
                            }
                            viewModel.updateContactPhotoUri(photoUriString)
                            viewModel.updateContactId(contactId)
                        } else {
                            // No photo, clear any existing photo URI but keep contact ID
                            viewModel.updateContactPhotoUri(null)
                            viewModel.updateContactId(contactId)
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
    
    // Check if form is valid (name required, and either phone or email required)
    val isFormValid = uiState.contactName.isNotBlank() && 
                     (!uiState.contactPhoneNumber.isNullOrBlank() || !uiState.contactEmail.isNullOrBlank())
    
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
                },
                actions = {
                    TextButton(
                        onClick = viewModel::saveConnection,
                        enabled = isFormValid
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
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
            
            // Phone number (optional if email provided)
            val phoneError = ValidationUtils.getPhoneError(uiState.contactPhoneNumber)
            OutlinedTextField(
                value = uiState.contactPhoneNumber ?: "",
                onValueChange = { newValue ->
                    // Filter to only allow digits
                    val digitsOnly = newValue.filter { it.isDigit() }
                    viewModel.updateContactPhoneNumber(digitsOnly)
                },
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Optional if email provided") },
                isError = phoneError != null,
                supportingText = phoneError?.let { { Text(it) } },
                visualTransformation = PhoneNumberTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            
            // Email (optional if phone provided)
            val emailError = ValidationUtils.getEmailError(uiState.contactEmail)
            OutlinedTextField(
                value = uiState.contactEmail ?: "",
                onValueChange = { viewModel.updateContactEmail(it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Optional if phone provided") },
                isError = emailError != null,
                supportingText = emailError?.let { { Text(it) } }
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
            var customDaysText by remember { mutableStateOf(uiState.reminderFrequencyDays.toString()) }
            LaunchedEffect(uiState.reminderFrequencyDays) {
                customDaysText = uiState.reminderFrequencyDays.toString()
            }
            OutlinedTextField(
                value = customDaysText,
                onValueChange = { 
                    customDaysText = it
                    viewModel.updateReminderFrequencyDaysFromString(it)
                },
                label = { Text("Custom (days)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Preferred method
            val phoneNumber = uiState.contactPhoneNumber
            val email = uiState.contactEmail
            val hasPhone = !phoneNumber.isNullOrBlank()
            val hasEmail = !email.isNullOrBlank()
            
            Text("Preferred Method", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (hasPhone) {
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
                }
                if (hasEmail) {
                    FilterChip(
                        selected = uiState.preferredMethod == ConnectionMethod.EMAIL,
                        onClick = { viewModel.updatePreferredMethod(ConnectionMethod.EMAIL) },
                        label = { Text("Email") }
                    )
                }
                if (hasPhone && hasEmail) {
                    FilterChip(
                        selected = uiState.preferredMethod == ConnectionMethod.BOTH,
                        onClick = { viewModel.updatePreferredMethod(ConnectionMethod.BOTH) },
                        label = { Text("Any") }
                    )
                }
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
                    initialSelectedDateMillis = uiState.birthday?.let { birthday ->
                        // Convert Date to Calendar and normalize to local midnight for display
                        Calendar.getInstance().apply {
                            time = birthday
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                    }
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    // Normalize to midnight in local timezone to avoid timezone issues
                                    val calendar = Calendar.getInstance().apply {
                                        timeInMillis = millis
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    viewModel.updateBirthday(calendar.time)
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
            
            // Prompt on birthday checkbox (only show if birthday is set)
            if (uiState.birthday != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Prompt for connection on birthday",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Checkbox(
                        checked = uiState.promptOnBirthday,
                        onCheckedChange = viewModel::updatePromptOnBirthday
                    )
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Error message
            if (saveResult is SaveResult.Error) {
                Text(
                    text = (saveResult as SaveResult.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
