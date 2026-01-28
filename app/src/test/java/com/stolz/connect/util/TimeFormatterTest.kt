package com.stolz.connect.util

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class TimeFormatterTest {

    @Test
    fun `formatRelativeTime returns just now for recent time`() {
        val now = Date()
        val result = TimeFormatter.formatRelativeTime(now)
        assertEquals("just now", result)
    }

    @Test
    fun `formatRelativeTime returns minutes ago for recent time`() {
        val past = Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5))
        val result = TimeFormatter.formatRelativeTime(past)
        assertEquals("5 minutes ago", result)
    }

    @Test
    fun `formatRelativeTime returns singular minute`() {
        val past = Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1))
        val result = TimeFormatter.formatRelativeTime(past)
        assertEquals("1 minute ago", result)
    }

    @Test
    fun `formatRelativeTime returns hours ago`() {
        val past = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3))
        val result = TimeFormatter.formatRelativeTime(past)
        assertEquals("3 hours ago", result)
    }

    @Test
    fun `formatRelativeTime returns singular hour`() {
        val past = Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1))
        val result = TimeFormatter.formatRelativeTime(past)
        assertEquals("1 hour ago", result)
    }

    @Test
    fun `formatRelativeTime returns days ago`() {
        val past = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(5))
        val result = TimeFormatter.formatRelativeTime(past)
        assertEquals("5 days ago", result)
    }

    @Test
    fun `formatRelativeTime returns singular day`() {
        val past = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1))
        val result = TimeFormatter.formatRelativeTime(past)
        assertEquals("1 day ago", result)
    }

    @Test
    fun `formatRelativeTime returns weeks ago`() {
        val past = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(14))
        val result = TimeFormatter.formatRelativeTime(past)
        assertEquals("2 weeks ago", result)
    }

    @Test
    fun `formatRelativeTime returns months ago`() {
        val past = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(60))
        val result = TimeFormatter.formatRelativeTime(past)
        assertTrue(result.contains("month"))
    }

    @Test
    fun `formatRelativeTime returns years ago`() {
        val past = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(400))
        val result = TimeFormatter.formatRelativeTime(past)
        assertTrue(result.contains("year"))
    }

    @Test
    fun `getLastContactedColorCategory returns RED for null date`() {
        val result = TimeFormatter.getLastContactedColorCategory(null, 7)
        assertEquals(ContactColorCategory.RED, result)
    }

    @Test
    fun `getLastContactedColorCategory returns GREEN for recent contact`() {
        val recentDate = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(3))
        val result = TimeFormatter.getLastContactedColorCategory(recentDate, 7)
        assertEquals(ContactColorCategory.GREEN, result)
    }

    @Test
    fun `getLastContactedColorCategory returns GREEN for contact within frequency`() {
        val recentDate = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7))
        val result = TimeFormatter.getLastContactedColorCategory(recentDate, 7)
        assertEquals(ContactColorCategory.GREEN, result)
    }

    @Test
    fun `getLastContactedColorCategory returns YELLOW for overdue by one period`() {
        val overdueDate = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10))
        val result = TimeFormatter.getLastContactedColorCategory(overdueDate, 7)
        assertEquals(ContactColorCategory.YELLOW, result)
    }

    @Test
    fun `getLastContactedColorCategory returns YELLOW for overdue by exactly two periods`() {
        val overdueDate = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(14))
        val result = TimeFormatter.getLastContactedColorCategory(overdueDate, 7)
        assertEquals(ContactColorCategory.YELLOW, result)
    }

    @Test
    fun `getLastContactedColorCategory returns RED for overdue by more than two periods`() {
        val overdueDate = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(21))
        val result = TimeFormatter.getLastContactedColorCategory(overdueDate, 7)
        assertEquals(ContactColorCategory.RED, result)
    }
}
