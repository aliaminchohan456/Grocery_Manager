package com.example.grocerymanager.core.common

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class DateRange(val startInclusive: Long, val endInclusive: Long)

object DateUtils {
    private const val DAY_MILLIS = 24L * 60L * 60L * 1000L

    fun now(): Long = System.currentTimeMillis()

    fun startOfDay(epochMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): Long {
        val cal = java.util.Calendar.getInstance(timeZone)
        cal.timeInMillis = epochMillis
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun endOfDay(epochMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): Long =
        startOfDay(epochMillis, timeZone) + DAY_MILLIS - 1L

    fun startOfWeek(epochMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): Long {
        val cal = java.util.Calendar.getInstance(timeZone)
        cal.timeInMillis = epochMillis
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.set(java.util.Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        return cal.timeInMillis
    }

    fun startOfMonth(epochMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): Long {
        val cal = java.util.Calendar.getInstance(timeZone)
        cal.timeInMillis = epochMillis
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        return cal.timeInMillis
    }

    fun endOfMonth(epochMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): Long {
        val cal = java.util.Calendar.getInstance(timeZone)
        cal.timeInMillis = epochMillis
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
        cal.set(java.util.Calendar.MINUTE, 59)
        cal.set(java.util.Calendar.SECOND, 59)
        cal.set(java.util.Calendar.MILLISECOND, 999)
        cal.set(java.util.Calendar.DAY_OF_MONTH, cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
        return cal.timeInMillis
    }

    fun monthAndYear(epochMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): Pair<Int, Int> {
        val cal = java.util.Calendar.getInstance(timeZone)
        cal.timeInMillis = epochMillis
        val m = cal.get(java.util.Calendar.MONTH) + 1
        val y = cal.get(java.util.Calendar.YEAR)
        return m to y
    }

    fun daysInMonth(epochMillis: Long, timeZone: TimeZone = TimeZone.getDefault()): Int {
        val cal = java.util.Calendar.getInstance(timeZone)
        cal.timeInMillis = epochMillis
        return cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    }

    fun today(now: Long = now(), timeZone: TimeZone = TimeZone.getDefault()): DateRange =
        DateRange(startOfDay(now, timeZone), endOfDay(now, timeZone))

    fun thisWeek(now: Long = now(), timeZone: TimeZone = TimeZone.getDefault()): DateRange =
        DateRange(startOfWeek(now, timeZone), endOfDay(now, timeZone))

    fun thisMonth(now: Long = now(), timeZone: TimeZone = TimeZone.getDefault()): DateRange =
        DateRange(startOfMonth(now, timeZone), endOfMonth(now, timeZone))

    fun previousMonth(now: Long = now(), timeZone: TimeZone = TimeZone.getDefault()): DateRange {
        val cal = java.util.Calendar.getInstance(timeZone)
        cal.timeInMillis = now
        cal.add(java.util.Calendar.MONTH, -1)
        return DateRange(startOfMonth(cal.timeInMillis, timeZone), endOfMonth(cal.timeInMillis, timeZone))
    }
}

/**
 * Emits the current epoch-millis every [intervalMillis] ms until cancelled.
 * Used by ViewModels that derive UI from wall-clock time (greeting, month
 * chip) so the derived value updates when the clock crosses a boundary
 * without a full data refresh.
 */
fun tickerFlow(intervalMillis: Long = 60_000L): Flow<Long> = flow {
    while (true) {
        emit(System.currentTimeMillis())
        delay(intervalMillis)
    }
}

/**
 * Display formatters — central source of truth for any user-visible date or percent.
 * All take epoch millis (or numeric args) and return localized strings via [Locale.getDefault].
 */
object DateFormat {
    private val longDate: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault())

    private val shortDate: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())

    private val dayHeader: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())

    private val monthYear: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

    private val monthShort: DateTimeFormatter =
        DateTimeFormatter.ofPattern("MMM", Locale.getDefault())

    /** "Mon, 16 Jun 2026" — for date pickers, form fields, detail subtitles. */
    fun longDate(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate().format(longDate)

    /** "16 Jun" — compact pill / chip label. */
    fun shortDate(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate().format(shortDate)

    /** "Monday, 16 June" — for record group headers. */
    fun dayHeader(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate().format(dayHeader)

    /** "June 2026" — for month labels, budget headers, chart legends. */
    fun monthYear(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate().format(monthYear)

    /** "Jun" — short month label, e.g. for chart x-axis. */
    fun monthShort(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate().format(monthShort)

    /** "Jun 2026" — for recent-budget list rows. */
    fun monthYear(month: Int, year: Int): String {
        val date = LocalDate.of(year, month.coerceIn(1, 12), 1)
        return date.format(monthYear)
    }

    /** Standalone month name (e.g. "June") — uses [TextStyle.FULL] and respects [Locale]. */
    fun monthName(month: Int, style: TextStyle = TextStyle.FULL, locale: Locale = Locale.getDefault()): String =
        Month.of(month.coerceIn(1, 12)).getDisplayName(style, locale)

    /** "8.5%" or "+12.3%" — for change/delta percentages. */
    fun formatPercent(value: Float, signed: Boolean = false, locale: Locale = Locale.getDefault()): String {
        val sign = if (signed && value > 0f) "+" else ""
        return "$sign${"%.1f".format(locale, value)}%"
    }
}
