package com.stolz.connect.util

import org.junit.Assert.*
import org.junit.Test

class NameUtilsTest {

    @Test
    fun `getInitials returns single letter for single name`() {
        assertEquals("J", NameUtils.getInitials("John"))
        assertEquals("M", NameUtils.getInitials("Mary"))
    }

    @Test
    fun `getInitials returns two letters for first and last name`() {
        assertEquals("JD", NameUtils.getInitials("John Doe"))
        assertEquals("MS", NameUtils.getInitials("Mary Smith"))
    }

    @Test
    fun `getInitials returns first and last for multiple names`() {
        assertEquals("JD", NameUtils.getInitials("John Middle Doe"))
        assertEquals("JS", NameUtils.getInitials("John Middle Name Smith"))
    }

    @Test
    fun `getInitials handles uppercase correctly`() {
        assertEquals("JD", NameUtils.getInitials("john doe"))
        assertEquals("JD", NameUtils.getInitials("JOHN DOE"))
        assertEquals("JD", NameUtils.getInitials("John Doe"))
    }

    @Test
    fun `getInitials handles extra whitespace`() {
        assertEquals("JD", NameUtils.getInitials("  John   Doe  "))
        assertEquals("J", NameUtils.getInitials("  John  "))
    }

    @Test
    fun `getInitials returns question mark for empty string`() {
        assertEquals("?", NameUtils.getInitials(""))
        assertEquals("?", NameUtils.getInitials("   "))
    }

    @Test
    fun `pluralize returns singular for count of 1`() {
        assertEquals("1 day", StringUtils.pluralize(1, "day"))
        assertEquals("1 connection", StringUtils.pluralize(1, "connection"))
    }

    @Test
    fun `pluralize returns plural for count not 1`() {
        assertEquals("5 days", StringUtils.pluralize(5, "day"))
        assertEquals("0 days", StringUtils.pluralize(0, "day"))
        assertEquals("2 connections", StringUtils.pluralize(2, "connection"))
    }

    @Test
    fun `pluralize uses custom plural form when provided`() {
        assertEquals("1 person", StringUtils.pluralize(1, "person", "people"))
        assertEquals("5 people", StringUtils.pluralize(5, "person", "people"))
    }
}
