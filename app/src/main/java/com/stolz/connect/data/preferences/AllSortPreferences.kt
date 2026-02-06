package com.stolz.connect.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sort order for the All connections list.
 */
enum class AllSortOrder {
    /** By contact name A–Z */
    A_Z,
    /** By next contact date, soonest first */
    DATE_ASCENDING,
    /** By next contact date, latest first */
    DATE_DESCENDING
}

@Singleton
class AllSortPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "all_sort_preferences",
        Context.MODE_PRIVATE
    )

    private val SORT_ORDER_KEY = "all_sort_order"

    fun getSortOrder(): AllSortOrder {
        val name = prefs.getString(SORT_ORDER_KEY, AllSortOrder.DATE_ASCENDING.name)
        return try {
            AllSortOrder.valueOf(name ?: AllSortOrder.DATE_ASCENDING.name)
        } catch (e: Exception) {
            AllSortOrder.DATE_ASCENDING
        }
    }

    fun getSortOrderFlow(): Flow<AllSortOrder> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == SORT_ORDER_KEY) {
                trySend(getSortOrder())
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(getSortOrder())
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    fun setSortOrder(order: AllSortOrder) {
        prefs.edit {
            putString(SORT_ORDER_KEY, order.name)
        }
    }
}
