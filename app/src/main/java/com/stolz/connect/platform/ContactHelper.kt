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
}
