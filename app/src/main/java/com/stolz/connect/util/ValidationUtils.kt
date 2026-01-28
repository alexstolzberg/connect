package com.stolz.connect.util

object ValidationUtils {
    // Simple, platform-independent email regex suitable for JVM tests and Android
    // Covers typical emails like local@domain.tld, with +, _, . and - in local part.
    private val EMAIL_REGEX =
        Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    
    fun isValidEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return true // Optional field
        return EMAIL_REGEX.matches(email)
    }
    
    fun isValidPhone(phone: String?): Boolean {
        if (phone.isNullOrBlank()) return true // Optional field
        // Remove all non-digit characters (including +) for length validation
        val digitsOnly = phone.replace(Regex("[^\\d]"), "")
        // Check if it has at least 7 digits (minimum for a valid phone number) and max 15 digits
        return digitsOnly.length >= 7 && digitsOnly.length <= 15
    }
    
    fun getEmailError(email: String?): String? {
        if (email.isNullOrBlank()) return null
        return if (!isValidEmail(email)) "Invalid email format" else null
    }
    
    fun getPhoneError(phone: String?): String? {
        if (phone.isNullOrBlank()) return null
        return if (!isValidPhone(phone)) "Invalid phone number format" else null
    }
}
