package com.stolz.connect.util

import org.junit.Assert.*
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun `isValidEmail returns true for valid email`() {
        assertTrue(ValidationUtils.isValidEmail("test@example.com"))
        assertTrue(ValidationUtils.isValidEmail("user.name@example.co.uk"))
        assertTrue(ValidationUtils.isValidEmail("test+tag@example.com"))
    }

    @Test
    fun `isValidEmail returns false for invalid email`() {
        assertFalse(ValidationUtils.isValidEmail("invalid"))
        assertFalse(ValidationUtils.isValidEmail("test@"))
        assertFalse(ValidationUtils.isValidEmail("@example.com"))
        assertFalse(ValidationUtils.isValidEmail("test @example.com"))
    }

    @Test
    fun `isValidEmail returns true for null or blank`() {
        assertTrue(ValidationUtils.isValidEmail(null))
        assertTrue(ValidationUtils.isValidEmail(""))
        assertTrue(ValidationUtils.isValidEmail("   "))
    }

    @Test
    fun `isValidPhone returns true for valid phone numbers`() {
        assertTrue(ValidationUtils.isValidPhone("1234567"))
        assertTrue(ValidationUtils.isValidPhone("123-456-7890"))
        assertTrue(ValidationUtils.isValidPhone("(123) 456-7890"))
        assertTrue(ValidationUtils.isValidPhone("+1 123-456-7890"))
        assertTrue(ValidationUtils.isValidPhone("+123456789012345"))
    }

    @Test
    fun `isValidPhone returns false for invalid phone numbers`() {
        assertFalse(ValidationUtils.isValidPhone("123"))
        assertFalse(ValidationUtils.isValidPhone("123456")) // Less than 7 digits
        assertFalse(ValidationUtils.isValidPhone("+1234567890123456")) // More than 15 digits
    }

    @Test
    fun `isValidPhone returns true for null or blank`() {
        assertTrue(ValidationUtils.isValidPhone(null))
        assertTrue(ValidationUtils.isValidPhone(""))
        assertTrue(ValidationUtils.isValidPhone("   "))
    }

    @Test
    fun `getEmailError returns null for valid email`() {
        assertNull(ValidationUtils.getEmailError("test@example.com"))
        assertNull(ValidationUtils.getEmailError(null))
        assertNull(ValidationUtils.getEmailError(""))
    }

    @Test
    fun `getEmailError returns error message for invalid email`() {
        val error = ValidationUtils.getEmailError("invalid")
        assertNotNull(error)
        assertEquals("Invalid email format", error)
    }

    @Test
    fun `getPhoneError returns null for valid phone`() {
        assertNull(ValidationUtils.getPhoneError("1234567"))
        assertNull(ValidationUtils.getPhoneError(null))
        assertNull(ValidationUtils.getPhoneError(""))
    }

    @Test
    fun `getPhoneError returns error message for invalid phone`() {
        val error = ValidationUtils.getPhoneError("123")
        assertNotNull(error)
        assertEquals("Invalid phone number format", error)
    }
}
