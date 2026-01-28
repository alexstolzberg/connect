package com.stolz.connect.util

object NameUtils {
    /**
     * Extracts initials from a name.
     * - Single name: returns first letter
     * - Multiple names: returns first letter of first name + first letter of last name
     * - Empty name: returns "?"
     */
    fun getInitials(name: String): String {
        val parts = name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        return when {
            parts.isEmpty() -> "?"
            parts.size == 1 -> parts[0].take(1).uppercase()
            else -> "${parts[0].first()}${parts.last().first()}".uppercase()
        }
    }
}

object StringUtils {
    /**
     * Formats a count with proper pluralization.
     * @param count The count value
     * @param singular The singular form of the word (e.g., "day")
     * @param plural The plural form of the word (e.g., "days")
     * @return Formatted string like "1 day" or "5 days"
     */
    fun pluralize(count: Int, singular: String, plural: String = "${singular}s"): String {
        return if (count == 1) {
            "1 $singular"
        } else {
            "$count $plural"
        }
    }
}
