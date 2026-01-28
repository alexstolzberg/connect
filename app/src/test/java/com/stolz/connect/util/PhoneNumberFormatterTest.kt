package com.stolz.connect.util

import org.junit.Assert.*
import org.junit.Test

class PhoneNumberFormatterTest {

    @Test
    fun `format returns empty string for null or blank`() {
        assertEquals("", PhoneNumberFormatter.format(null))
        assertEquals("", PhoneNumberFormatter.format(""))
        assertEquals("", PhoneNumberFormatter.format("   "))
    }

    @Test
    fun `format formats 10 digit number with dashes`() {
        assertEquals("123-456-7890", PhoneNumberFormatter.format("1234567890"))
        assertEquals("123-456-7890", PhoneNumberFormatter.format("123-456-7890"))
        assertEquals("123-456-7890", PhoneNumberFormatter.format("(123) 456-7890"))
    }

    @Test
    fun `format formats 7 digit number in a readable way`() {
        // For 7 digits we just assert that formatting adds at least one dash and preserves digits
        val result = PhoneNumberFormatter.format("1234567")
        assertTrue(result.contains("-"))
        assertTrue(result.replace("-", "").endsWith("1234567"))
    }

    @Test
    fun `format formats 3 digit number`() {
        assertEquals("123", PhoneNumberFormatter.format("123"))
    }

    @Test
    fun `format formats 6 digit number`() {
        assertEquals("123-456", PhoneNumberFormatter.format("123456"))
    }

    @Test
    fun `format handles US number with country code`() {
        assertEquals("+1 (123) 456-7890", PhoneNumberFormatter.format("11234567890"))
        assertEquals("+1 (123) 456-7890", PhoneNumberFormatter.format("+11234567890"))
    }

    @Test
    fun `format handles international numbers`() {
        val result = PhoneNumberFormatter.format("+441234567890")
        assertTrue(result.startsWith("+44"))
        assertTrue(result.contains("("))
        assertTrue(result.contains(")"))
    }

    @Test
    fun `format removes non-digit characters except plus`() {
        assertEquals("123-456-7890", PhoneNumberFormatter.format("1a2b3c4d5e6f7g8h9i0j"))
        // When starting with + and letters, we expect the plus to be preserved and a formatted number
        val result = PhoneNumberFormatter.format("+1a2b3c4d5e6f7g8h9i0j")
        assertTrue(result.startsWith("+"))
        assertTrue(result.contains("-"))
    }

    @Test
    fun `format preserves plus sign at start`() {
        assertTrue(PhoneNumberFormatter.format("+1234567890").startsWith("+"))
    }
}
