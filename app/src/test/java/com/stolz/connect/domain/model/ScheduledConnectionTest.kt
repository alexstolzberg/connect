package com.stolz.connect.domain.model

import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.Date

class ScheduledConnectionTest {

    @Test
    fun `isDueToday returns true when nextReminderDate is today`() {
        val today = Date()
        val connection = ScheduledConnection(
            contactName = "Test",
            reminderFrequencyDays = 7,
            preferredMethod = ConnectionMethod.CALL,
            nextReminderDate = today
        )

        assertTrue(connection.isDueToday)
    }

    @Test
    fun `isDueToday returns true when nextReminderDate is before today`() {
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -1)
        }.time

        val connection = ScheduledConnection(
            contactName = "Test",
            reminderFrequencyDays = 7,
            preferredMethod = ConnectionMethod.CALL,
            nextReminderDate = yesterday
        )

        assertTrue(connection.isDueToday)
    }

    @Test
    fun `isDueToday returns false when nextReminderDate is tomorrow`() {
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.time

        val connection = ScheduledConnection(
            contactName = "Test",
            reminderFrequencyDays = 7,
            preferredMethod = ConnectionMethod.CALL,
            nextReminderDate = tomorrow
        )

        assertFalse(connection.isDueToday)
    }

    @Test
    fun `isPastDue returns true when nextReminderDate is before today`() {
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -1)
        }.time

        val connection = ScheduledConnection(
            contactName = "Test",
            reminderFrequencyDays = 7,
            preferredMethod = ConnectionMethod.CALL,
            nextReminderDate = yesterday
        )

        assertTrue(connection.isPastDue)
    }

    @Test
    fun `isPastDue returns false when nextReminderDate is today`() {
        val today = Date()
        val connection = ScheduledConnection(
            contactName = "Test",
            reminderFrequencyDays = 7,
            preferredMethod = ConnectionMethod.CALL,
            nextReminderDate = today
        )

        assertFalse(connection.isPastDue)
    }

    @Test
    fun `isPastDue returns false when nextReminderDate is in future`() {
        val tomorrow = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
        }.time

        val connection = ScheduledConnection(
            contactName = "Test",
            reminderFrequencyDays = 7,
            preferredMethod = ConnectionMethod.CALL,
            nextReminderDate = tomorrow
        )

        assertFalse(connection.isPastDue)
    }

    @Test
    fun `isPastDue ignores time component`() {
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
        }.time

        val connection = ScheduledConnection(
            contactName = "Test",
            reminderFrequencyDays = 7,
            preferredMethod = ConnectionMethod.CALL,
            nextReminderDate = yesterday
        )

        assertTrue(connection.isPastDue)
    }
}
