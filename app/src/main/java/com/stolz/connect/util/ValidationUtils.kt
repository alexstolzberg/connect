package com.stolz.connect.util

object ValidationUtils {
    private val EMAIL_PATTERN = android.util.Patterns.EMAIL_ADDRESS
    private val PHONE_PATTERN = Regex("""^[\+]?[(]?[0-9]{1,4}[)]?[-\s\.]?[(]?[0-9]{1,4}[)]?[-\s\.]?[0-9]{1,9}$""")
    
    fun isValidEmail(email: String?): Boolean {
        if (email.isNullOrBlank()) return true // Optional field
        return EMAIL_PATTERN.matcher(email).matches()
    }
    
    fun isValidPhone(phone: String?): Boolean {
        if (phone.isNullOrBlank()) return true // Optional field
        // Remove common formatting characters for validation
        val cleaned = phone.replace(Regex("[^+\\d]"), "")
        // Check if it has at least 7 digits (minimum for a valid phone number)
        return cleaned.length >= 7 && cleaned.length <= 15
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
