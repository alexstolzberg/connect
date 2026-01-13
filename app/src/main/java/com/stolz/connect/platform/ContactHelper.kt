package com.stolz.connect.platform

import android.content.Context
import android.content.Intent
import android.net.Uri

object ContactHelper {
    fun makeCall(context: Context, phoneNumber: String, onPermissionNeeded: () -> Unit) {
        // Clean phone number (remove non-digits except +)
        val cleanNumber = phoneNumber.replace(Regex("[^+\\d]"), "")
        
        if (PermissionHelper.hasCallPhonePermission(context)) {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$cleanNumber")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                android.util.Log.e("ContactHelper", "Failed to make call", e)
                // Fallback to ACTION_DIAL if CALL fails
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$cleanNumber")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(dialIntent)
            }
        } else {
            onPermissionNeeded()
        }
    }
    
    fun sendMessage(context: Context, phoneNumber: String) {
        // Clean phone number (remove non-digits except +)
        val cleanNumber = phoneNumber.replace(Regex("[^+\\d]"), "")
        android.util.Log.d("ContactHelper", "sendMessage called with: $phoneNumber, cleaned: $cleanNumber")
        
        // ACTION_SENDTO opens the default messaging app
        // SEND_SMS permission is not required for this intent
        val uri = Uri.parse("smsto:$cleanNumber")
        android.util.Log.d("ContactHelper", "URI: $uri")
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = uri
            // Don't use FLAG_ACTIVITY_NEW_TASK for SENDTO - it should use the current task
        }
        
        try {
            val resolveInfo = intent.resolveActivity(context.packageManager)
            android.util.Log.d("ContactHelper", "Resolve info: $resolveInfo")
            
            if (resolveInfo != null) {
                android.util.Log.d("ContactHelper", "Starting messaging activity")
                context.startActivity(intent)
            } else {
                android.util.Log.w("ContactHelper", "No app found for ACTION_SENDTO, trying fallback")
                // Fallback: try ACTION_VIEW with sms: scheme
                val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("sms:$cleanNumber")
                }
                val fallbackResolve = fallbackIntent.resolveActivity(context.packageManager)
                android.util.Log.d("ContactHelper", "Fallback resolve info: $fallbackResolve")
                
                if (fallbackResolve != null) {
                    android.util.Log.d("ContactHelper", "Starting fallback messaging activity")
                    context.startActivity(fallbackIntent)
                } else {
                    android.util.Log.e("ContactHelper", "No messaging app found on device")
                    // Last resort: try ACTION_SEND with text/plain
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra("address", cleanNumber)
                        putExtra(Intent.EXTRA_TEXT, "")
                    }
                    if (sendIntent.resolveActivity(context.packageManager) != null) {
                        android.util.Log.d("ContactHelper", "Starting ACTION_SEND as last resort")
                        context.startActivity(Intent.createChooser(sendIntent, "Send message"))
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ContactHelper", "Failed to open messaging app", e)
            e.printStackTrace()
        }
    }
    
    fun sendEmail(context: Context, email: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
        }
        try {
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Fallback to ACTION_SEND
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                }
                if (sendIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(Intent.createChooser(sendIntent, "Send email"))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ContactHelper", "Failed to open email app", e)
        }
    }
    
    fun getContactPhotoUri(context: Context, contactId: String?): Uri? {
        if (contactId == null) return null
        return try {
            val contentUri = android.provider.ContactsContract.Contacts.CONTENT_URI
            val contactUri = android.content.ContentUris.withAppendedId(contentUri, contactId.toLong())
            val photoUri = android.provider.ContactsContract.Contacts.openContactPhotoInputStream(
                context.contentResolver,
                contactUri
            )
            if (photoUri != null) {
                photoUri.close()
                android.provider.ContactsContract.Contacts.getLookupUri(
                    context.contentResolver,
                    contactUri
                )?.let { lookupUri ->
                    android.provider.ContactsContract.Contacts.getLookupUri(
                        context.contentResolver,
                        lookupUri
                    )
                }
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("ContactHelper", "Failed to get contact photo", e)
            null
        }
    }
    
    fun openContactInPhone(context: Context, contactId: String) {
        try {
            android.util.Log.d("ContactHelper", "Opening contact with ID: $contactId")
            
            // Build contact URI
            val contactUri = android.content.ContentUris.withAppendedId(
                android.provider.ContactsContract.Contacts.CONTENT_URI,
                contactId.toLong()
            )
            
            // Get lookup URI for stable reference
            val lookupUri = android.provider.ContactsContract.Contacts.getLookupUri(
                context.contentResolver,
                contactUri
            )
            
            android.util.Log.d("ContactHelper", "Contact URI: $contactUri, Lookup URI: $lookupUri")
            
            // Use lookup URI if available, otherwise use contact URI
            val finalUri = lookupUri ?: contactUri
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = finalUri
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            val resolveInfo = intent.resolveActivity(context.packageManager)
            android.util.Log.d("ContactHelper", "Resolve info: $resolveInfo")
            
            if (resolveInfo != null) {
                context.startActivity(intent)
                android.util.Log.d("ContactHelper", "Successfully started contact view activity")
            } else {
                android.util.Log.e("ContactHelper", "No activity found to handle contact view")
                // Fallback: try with tel: scheme if we have a phone number
                // But we don't have phone number here, so just log the error
            }
        } catch (e: Exception) {
            android.util.Log.e("ContactHelper", "Failed to open contact: ${e.message}", e)
            e.printStackTrace()
        }
    }
    
    fun addContactToPhone(
        context: Context,
        name: String,
        phoneNumber: String?,
        email: String?
    ) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                type = android.provider.ContactsContract.Contacts.CONTENT_TYPE
                putExtra(android.provider.ContactsContract.Intents.Insert.NAME, name)
                if (phoneNumber != null) {
                    putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, phoneNumber)
                }
                if (email != null) {
                    putExtra(android.provider.ContactsContract.Intents.Insert.EMAIL, email)
                }
            }
            
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                android.util.Log.e("ContactHelper", "No app found to add contact")
            }
        } catch (e: Exception) {
            android.util.Log.e("ContactHelper", "Failed to add contact", e)
        }
    }
}
