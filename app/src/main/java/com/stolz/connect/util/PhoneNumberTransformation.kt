package com.stolz.connect.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

object PhoneNumberFormatter {
    fun format(phoneNumber: String?): String {
        if (phoneNumber.isNullOrBlank()) return ""
        
        // Remove all non-digits except +
        val digits = phoneNumber.replace(Regex("[^+\\d]"), "")
        
        // If it starts with +, keep it
        val hasPlus = digits.startsWith("+")
        val cleanDigits = if (hasPlus) digits.substring(1) else digits
        
        return when {
            cleanDigits.isEmpty() -> phoneNumber // Return original if no digits
            cleanDigits.length <= 3 -> if (hasPlus) "+$cleanDigits" else cleanDigits
            cleanDigits.length <= 6 -> {
                val formatted = "${cleanDigits.substring(0, 3)}-${cleanDigits.substring(3)}"
                if (hasPlus) "+$formatted" else formatted
            }
            cleanDigits.length <= 10 -> {
                val formatted = "${cleanDigits.substring(0, 3)}-${cleanDigits.substring(3, 6)}-${cleanDigits.substring(6)}"
                if (hasPlus) "+$formatted" else formatted
            }
            cleanDigits.length == 11 && cleanDigits.startsWith("1") -> {
                // US number with country code: +1 (XXX) XXX-XXXX
                "+1 (${cleanDigits.substring(1, 4)}) ${cleanDigits.substring(4, 7)}-${cleanDigits.substring(7)}"
            }
            else -> {
                // International format: +X (XXX) XXX-XXXX
                val countryCode = cleanDigits.substring(0, cleanDigits.length - 10)
                val areaCode = cleanDigits.substring(cleanDigits.length - 10, cleanDigits.length - 7)
                val firstPart = cleanDigits.substring(cleanDigits.length - 7, cleanDigits.length - 4)
                val lastPart = cleanDigits.substring(cleanDigits.length - 4)
                "+$countryCode ($areaCode) $firstPart-$lastPart"
            }
        }
    }
}

class PhoneNumberTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val formatted = PhoneNumberFormatter.format(text.text)
        
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // Count digits in original up to offset
                val digitsBeforeOffset = text.text.take(offset).count { it.isDigit() || it == '+' }
                // Find position in formatted string
                var formattedPos = 0
                var digitCount = 0
                for (i in formatted.indices) {
                    if (formatted[i].isDigit() || formatted[i] == '+') {
                        digitCount++
                        if (digitCount >= digitsBeforeOffset) {
                            formattedPos = i + 1
                            break
                        }
                    }
                    formattedPos = i + 1
                }
                return formattedPos
            }
            
            override fun transformedToOriginal(offset: Int): Int {
                // Count digits in formatted string up to offset
                val digitsBeforeOffset = formatted.take(offset).count { it.isDigit() || it == '+' }
                // Find position in original string
                var originalPos = 0
                var digitCount = 0
                for (i in text.text.indices) {
                    if (text.text[i].isDigit() || text.text[i] == '+') {
                        digitCount++
                        if (digitCount >= digitsBeforeOffset) {
                            originalPos = i + 1
                            break
                        }
                    }
                    originalPos = i + 1
                }
                return originalPos
            }
        }
        
        return TransformedText(
            AnnotatedString(formatted),
            offsetMapping
        )
    }
}
