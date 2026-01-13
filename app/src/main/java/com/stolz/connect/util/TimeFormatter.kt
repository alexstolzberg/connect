package com.stolz.connect.util

import java.util.Date
import java.util.concurrent.TimeUnit

object TimeFormatter {
    fun formatRelativeTime(date: Date): String {
        val now = Date()
        val diffInMillis = now.time - date.time
        val diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis)
        val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
        val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)
        val diffInWeeks = diffInDays / 7
        val diffInMonths = diffInDays / 30
        val diffInYears = diffInDays / 365
        
        return when {
            diffInSeconds < 60 -> "just now"
            diffInMinutes < 60 -> "$diffInMinutes ${if (diffInMinutes == 1L) "minute" else "minutes"} ago"
            diffInHours < 24 -> "$diffInHours ${if (diffInHours == 1L) "hour" else "hours"} ago"
            diffInDays < 7 -> "$diffInDays ${if (diffInDays == 1L) "day" else "days"} ago"
            diffInWeeks < 4 -> "$diffInWeeks ${if (diffInWeeks == 1L) "week" else "weeks"} ago"
            diffInMonths < 12 -> "$diffInMonths ${if (diffInMonths == 1L) "month" else "months"} ago"
            else -> "$diffInYears ${if (diffInYears == 1L) "year" else "years"} ago"
        }
    }
    
    fun getLastContactedColorCategory(lastContactedDate: Date?, reminderFrequencyDays: Int): ContactColorCategory {
        if (lastContactedDate == null) {
            return ContactColorCategory.RED // Never contacted
        }
        
        val now = Date()
        val diffInDays = TimeUnit.MILLISECONDS.toDays(now.time - lastContactedDate.time)
        
        // Green: contacted within reminder frequency (not overdue)
        // Yellow: overdue by up to one reminder frequency period
        // Red: overdue by more than one reminder frequency period
        val greenThreshold = reminderFrequencyDays.toDouble()
        val yellowThreshold = reminderFrequencyDays * 2.0
        
        return when {
            diffInDays <= greenThreshold -> ContactColorCategory.GREEN
            diffInDays <= yellowThreshold -> ContactColorCategory.YELLOW
            else -> ContactColorCategory.RED
        }
    }
}

enum class ContactColorCategory {
    GREEN,   // Recent contact
    YELLOW,  // Medium time since contact
    RED      // Long time since contact
}
