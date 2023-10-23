package moe.ganen.konversi.data.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

/**
 * Interface that manage last success fetch time.
 */
interface FetchTimeManager {
    /**
     * Get last success fetch time.
     */
    fun getLastFetchTime(): Instant?

    /**
     * Update last success fetch time, indicating that network call to sync the data has been done.
     */
    fun updateLastFetchTime(time: Instant)

    /**
     * Indicate that last fetch time is still on valid range or not, if not then it is expected
     * to be updated.
     */
    fun stillOnValidRange(time: Instant?): Boolean {
        if (time == null || time.epochSeconds == 0L) return false
        val timeNow = Clock.System.now()
        // check if it's already past 30 minutes, which the acceptance range.
        return timeNow.minus(30.minutes) <= time
    }
}
