package com.splitsnap.domain.util

import android.content.SharedPreferences
import androidx.core.content.edit
import java.time.LocalDate

/**
 * Manages API rate limiting for Gemini API calls
 * Free tier limit: 1500 requests per day
 */
class RateLimitManager(private val prefs: SharedPreferences) {

    companion object {
        private const val MAX_PER_DAY = 1400
        private const val PREF_LAST_DATE = "rate_limit_last_date"
        private const val PREF_REQUEST_COUNT = "rate_limit_request_count"
    }

    /**
     * Check if we can make another API request today
     */
    fun canMakeRequest(): Boolean {
        resetIfNewDay()
        return getTodayCount() < MAX_PER_DAY
    }

    /**
     * Record that an API request was made
     */
    fun recordRequest() {
        resetIfNewDay()
        prefs.edit {
            putInt(PREF_REQUEST_COUNT, getTodayCount() + 1)
        }
    }

    /**
     * Get remaining requests for today
     */
    fun getRemainingRequests(): Int {
        resetIfNewDay()
        return MAX_PER_DAY - getTodayCount()
    }

    /**
     * Get current request count for today
     */
    fun getTodayCount(): Int {
        return prefs.getInt(PREF_REQUEST_COUNT, 0)
    }

    /**
     * Reset counter if it's a new day
     */
    private fun resetIfNewDay() {
        val lastDate = prefs.getString(PREF_LAST_DATE, "")
        val today = LocalDate.now().toString()
        if (lastDate != today) {
            prefs.edit {
                putString(PREF_LAST_DATE, today)
                    .putInt(PREF_REQUEST_COUNT, 0)
            }
        }
    }

    /**
     * Manually reset the counter (for testing)
     */
    fun reset() {
        prefs.edit {
            putString(PREF_LAST_DATE, LocalDate.now().toString())
                .putInt(PREF_REQUEST_COUNT, 0)
        }
    }
}
