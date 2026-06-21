package com.example.grocerymanager.core.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class DateUtilsTest {

    private val utc = TimeZone.getTimeZone("UTC")

    @Test fun `today range covers the full day`() {
        val now = utcCalendar(2025, 6, 15, 12, 0).timeInMillis
        val range = DateUtils.today(now, utc)
        assertThat(range.startInclusive).isEqualTo(utcCalendar(2025, 6, 15, 0, 0).timeInMillis)
        assertThat(range.endInclusive).isEqualTo(utcCalendar(2025, 6, 15, 23, 59, 59, 999).timeInMillis)
    }

    @Test fun `this month range covers first millisecond to last`() {
        val now = utcCalendar(2025, 6, 15, 12, 0).timeInMillis
        val range = DateUtils.thisMonth(now, utc)
        assertThat(range.startInclusive).isEqualTo(utcCalendar(2025, 6, 1, 0, 0).timeInMillis)
        assertThat(range.endInclusive).isEqualTo(utcCalendar(2025, 6, 30, 23, 59, 59, 999).timeInMillis)
    }

    @Test fun `previous month range is the month before`() {
        val now = utcCalendar(2025, 6, 15, 12, 0).timeInMillis
        val range = DateUtils.previousMonth(now, utc)
        assertThat(range.startInclusive).isEqualTo(utcCalendar(2025, 5, 1, 0, 0).timeInMillis)
        assertThat(range.endInclusive).isEqualTo(utcCalendar(2025, 5, 31, 23, 59, 59, 999).timeInMillis)
    }

    @Test fun `days in month for june is 30`() {
        val now = utcCalendar(2025, 6, 1, 0, 0).timeInMillis
        assertThat(DateUtils.daysInMonth(now, utc)).isEqualTo(30)
    }

    @Test fun `month and year returns correct tuple`() {
        val now = utcCalendar(2025, 6, 15, 12, 0).timeInMillis
        val (month, year) = DateUtils.monthAndYear(now, utc)
        assertThat(month).isEqualTo(6)
        assertThat(year).isEqualTo(2025)
    }

    private fun utcCalendar(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int = 0, milli: Int = 0): Calendar =
        Calendar.getInstance(utc).apply {
            clear()
            set(year, month - 1, day, hour, minute, second)
            set(Calendar.MILLISECOND, milli)
        }
}
