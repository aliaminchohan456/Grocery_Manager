package com.example.grocerymanager.core.common

import java.util.Calendar

/** Wall-clock greeting buckets. Independent of the feature layer. */
enum class Greeting { Morning, Afternoon, Evening, Hello }

object GreetingResolver {
    /**
     * Map the current hour-of-day to a [Greeting] bucket. Pure function so
     * it is unit-testable without bringing in a Calendar mock — the
     * caller passes `now` and `hour` if they need deterministic output.
     */
    fun forHour(hour: Int): Greeting = when (hour) {
        in 5..11 -> Greeting.Morning
        in 12..16 -> Greeting.Afternoon
        in 17..21 -> Greeting.Evening
        else -> Greeting.Hello
    }

    /** Convenience: resolve using the current system clock. */
    fun current(now: Long = System.currentTimeMillis()): Greeting {
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        return forHour(cal.get(Calendar.HOUR_OF_DAY))
    }
}
