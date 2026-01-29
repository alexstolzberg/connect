package com.stolz.connect.ui.addedit

import android.Manifest
import android.content.ContentUris
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Edit
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
import coil.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Photo
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.stolz.connect.ui.theme.AvatarColors
import com.stolz.connect.ui.theme.Dimensions
import com.stolz.connect.ui.shared.PillButton
import com.stolz.connect.util.NameUtils
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    // Also validate that preferred method matches available data
    val hasPhone = !uiState.contactPhoneNumber.isNullOrBlank()
    val hasEmail = !uiState.contactEmail.isNullOrBlank()
    val preferredMethodValid = when (uiState.preferredMethod) {
        ConnectionMethod.CALL, ConnectionMethod.MESSAGE -> hasPhone
        ConnectionMethod.EMAIL -> hasEmail
        ConnectionMethod.BOTH -> hasPhone || hasEmail
    }
    val isFormValid = uiState.contactName.isNotBlank() && 
                     (hasPhone || hasEmail) &&
                     preferredMethodValid
    
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
                .padding(Dimensions.medium),
            verticalArrangement = Arrangement.spacedBy(Dimensions.medium)
        ) {
            // Contact photo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var showPhotoOptions by remember { mutableStateOf(false) }
                var photoUri by remember { mutableStateOf<Uri?>(null) }
                
                // Track if we've initialized the color to avoid regenerating on every recomposition
                var colorInitialized by remember { mutableStateOf(false) }
                
                // Get avatar color - use stored color or pick a random one initially
                val avatarColor = remember(uiState.avatarColor) {
                    val storedColor = uiState.avatarColor
                    if (storedColor != null) {
                        androidx.compose.ui.graphics.Color(storedColor)
                    } else {
                        // Use a random color from the palette
                        val randomIndex = (System.currentTimeMillis() % AvatarColors.colors.size).toInt()
                        AvatarColors.colors[randomIndex]
                    }
                }
                
                // Set initial random color only once when screen loads and no color is set
                LaunchedEffect(uiState.avatarColor) {
                    if (!colorInitialized && uiState.avatarColor == null) {
                        val randomIndex = (System.currentTimeMillis() % AvatarColors.colors.size).toInt()
                        val selectedColor = AvatarColors.colors[randomIndex]
                        val colorInt = android.graphics.Color.argb(
                            (selectedColor.alpha * 255).toInt(),
                            (selectedColor.red * 255).toInt(),
                            (selectedColor.green * 255).toInt(),
                            (selectedColor.blue * 255).toInt()
                        )
                        viewModel.updateAvatarColor(colorInt)
                        colorInitialized = true
                    } else if (uiState.avatarColor != null) {
                        colorInitialized = true
                    }
                }
                
                // Get initials
                val initials = remember(uiState.contactName) {
                    NameUtils.getInitials(uiState.contactName)
                }
                
                // Update photoUri when uiState changes
                LaunchedEffect(uiState.contactPhotoUri) {
                    if (uiState.contactPhotoUri != null) {
                        photoUri = Uri.parse(uiState.contactPhotoUri)
                    } else {
                        photoUri = null
                    }
                }
                
                // Photo picker launcher (for gallery) - modern API
                val photoPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia()
                ) { uri: Uri? ->
                    uri?.let {
                        // Get the file path or content URI
                        val photoUriString = it.toString()
                        viewModel.updateContactPhotoUri(photoUriString)
                        photoUri = it
                    }
                }
                
                // Fallback photo picker for older Android versions
                val legacyPhotoPickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    uri?.let {
                        val photoUriString = it.toString()
                        viewModel.updateContactPhotoUri(photoUriString)
                        photoUri = it
                    }
                }
                
                Box(
                    modifier = Modifier
                        .size(120.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .clickable { showPhotoOptions = true }
                    ) {
                        if (photoUri != null) {
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Contact Photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                shape = CircleShape,
                                color = avatarColor
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = initials,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = androidx.compose.ui.graphics.Color.White
                                    )
                                }
                            }
                        }
                    }
                    // Edit icon in bottom right corner - positioned on top of avatar, outside the clipped area
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = (-4).dp, y = (-4).dp)
                            .clickable { showPhotoOptions = true },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = Dimensions.xxsmall
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Photo",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .size(36.dp)
                                .padding(Dimensions.xsmall)
                        )
                    }
                }
                
                // Photo options dialog
                if (showPhotoOptions) {
                    AlertDialog(
                        onDismissRequest = { showPhotoOptions = false },
                        title = { Text("Select Photo") },
                        text = {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(Dimensions.xsmall)
                            ) {
                                TextButton(
                                    onClick = {
                                        showPhotoOptions = false
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest.Builder()
                                                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                    .build()
                                            )
                                        } else {
                                            legacyPhotoPickerLauncher.launch("image/*")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Photo, contentDescription = null)
                                    Spacer(modifier = Modifier.width(Dimensions.xsmall))
                                    Text("Choose from Gallery")
                                }
                                if (photoUri != null) {
                                    TextButton(
                                        onClick = {
                                            showPhotoOptions = false
                                            viewModel.updateContactPhotoUri(null)
                                            photoUri = null
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Remove Photo", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                                
                                Divider(modifier = Modifier.padding(vertical = Dimensions.xsmall))
                                
                                Text(
                                    text = "Choose Avatar Color",
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(bottom = Dimensions.xxsmall)
                                )
                                
                                // Color palette grid - wrapped to multiple rows
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(6),
                                    horizontalArrangement = Arrangement.spacedBy(Dimensions.xsmall),
                                    verticalArrangement = Arrangement.spacedBy(Dimensions.xsmall),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                ) {
                                    items(AvatarColors.colors.size) { index ->
                                        val color = AvatarColors.colors[index]
                                        val colorInt = android.graphics.Color.argb(
                                            (color.alpha * 255).toInt(),
                                            (color.red * 255).toInt(),
                                            (color.green * 255).toInt(),
                                            (color.blue * 255).toInt()
                                        )
                                        val isSelected = uiState.avatarColor == colorInt
                                        
                                        Surface(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    viewModel.updateAvatarColor(colorInt)
                                                }
                                                .then(
                                                    if (isSelected) {
                                                        Modifier
                                                            .border(
                                                                width = 3.dp,
                                                                color = MaterialTheme.colorScheme.primary,
                                                                shape = CircleShape
                                                            )
                                                    } else {
                                                        Modifier
                                                    }
                                                ),
                                            shape = CircleShape,
                                            color = color
                                        ) {}
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showPhotoOptions = false }) {
                                Text("Done")
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Dimensions.xsmall))
            
            // Contact selection
            OutlinedButton(
                onClick = {
                    launchContactPicker()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Person, contentDescription = null)
                Spacer(modifier = Modifier.width(Dimensions.xsmall))
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
                horizontalArrangement = Arrangement.spacedBy(Dimensions.xsmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PillButton(
                    label = "Weekly",
                    isSelected = uiState.reminderFrequencyDays == 7,
                    onClick = { viewModel.updateReminderFrequencyDays(7) }
                )
                PillButton(
                    label = "Bi-weekly",
                    isSelected = uiState.reminderFrequencyDays == 14,
                    onClick = { viewModel.updateReminderFrequencyDays(14) }
                )
                PillButton(
                    label = "Monthly",
                    isSelected = uiState.reminderFrequencyDays == 30,
                    onClick = { viewModel.updateReminderFrequencyDays(30) }
                )
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
            
            // Preferred method - always show all pills
            val phoneNumber = uiState.contactPhoneNumber
            val email = uiState.contactEmail
            val hasPhone = !phoneNumber.isNullOrBlank()
            val hasEmail = !email.isNullOrBlank()
            
            Text("Preferred Method", style = MaterialTheme.typography.labelLarge)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.xsmall),
                verticalArrangement = Arrangement.spacedBy(Dimensions.xsmall)
            ) {
                // Always show Call and Message, but disable if no phone
                PillButton(
                    label = "Call",
                    isSelected = uiState.preferredMethod == ConnectionMethod.CALL,
                    onClick = { viewModel.updatePreferredMethod(ConnectionMethod.CALL) },
                    enabled = hasPhone
                )
                PillButton(
                    label = "Message",
                    isSelected = uiState.preferredMethod == ConnectionMethod.MESSAGE,
                    onClick = { viewModel.updatePreferredMethod(ConnectionMethod.MESSAGE) },
                    enabled = hasPhone
                )
                // Always show Email, but disable if no email
                PillButton(
                    label = "Email",
                    isSelected = uiState.preferredMethod == ConnectionMethod.EMAIL,
                    onClick = { viewModel.updatePreferredMethod(ConnectionMethod.EMAIL) },
                    enabled = hasEmail
                )
                // Always show No Preference if at least one contact method is available
                if (hasPhone || hasEmail) {
                    PillButton(
                        label = "No Preference",
                        isSelected = uiState.preferredMethod == ConnectionMethod.BOTH,
                        onClick = { viewModel.updatePreferredMethod(ConnectionMethod.BOTH) }
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.xsmall)
                    ) {
                        TextButton(onClick = { showDatePicker = true }) {
                            Text("Edit")
                        }
                        TextButton(onClick = { viewModel.updateBirthday(null) }) {
                            Text("Clear")
                        }
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
                                    // DatePicker returns UTC milliseconds, but we want to interpret it as a local date
                                    // Convert to local calendar to get the date components
                                    val utcCalendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                                        timeInMillis = millis
                                    }
                                    // Create a new calendar in local timezone with the same date components
                                    val localCalendar = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, utcCalendar.get(Calendar.YEAR))
                                        set(Calendar.MONTH, utcCalendar.get(Calendar.MONTH))
                                        set(Calendar.DAY_OF_MONTH, utcCalendar.get(Calendar.DAY_OF_MONTH))
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                    viewModel.updateBirthday(localCalendar.time)
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
            
            Spacer(modifier = Modifier.height(Dimensions.medium))
            
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


