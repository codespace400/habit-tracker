package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DayInfo(
    val dateString: String, // "yyyy-MM-dd"
    val dayLetter: String, // "M", "T", "W", etc.
    val dayOfMonth: Int,
    val isToday: Boolean,
    val dayOfWeekNumber: Int // 1=Mon, 2=Tue, ..., 7=Sun
)

data class MonthDayInfo(
    val dateString: String,
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isFuture: Boolean,
    val isToday: Boolean,
    val dayOfWeek: Int
)

object DateUtils {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("EEEE, MMMM d", Locale.US)
    private val shortMonthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
    private val dayLetterFormat = SimpleDateFormat("EEEEE", Locale.US) // single letter day

    fun getTodayString(): String {
        return isoFormat.format(Date())
    }

    fun getTodayDisplayString(): String {
        return displayFormat.format(Date())
    }

    fun getMonthDisplayString(calendar: Calendar): String {
        return shortMonthFormat.format(calendar.time)
    }

    fun parseDate(dateString: String): Date? {
        return try {
            isoFormat.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    fun formatDate(date: Date): String {
        return isoFormat.format(date)
    }

    /**
     * Returns the last [count] days ending with today.
     */
    fun getLastNDays(count: Int = 7): List<DayInfo> {
        val list = mutableListOf<DayInfo>()
        val cal = Calendar.getInstance()
        val todayStr = getTodayString()

        // Rewind to count - 1 days ago
        cal.add(Calendar.DAY_OF_YEAR, -(count - 1))

        for (i in 0 until count) {
            val dateStr = isoFormat.format(cal.time)
            val calDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            // Convert to 1=Mon ... 7=Sun
            val standardDayOfWeek = when (calDayOfWeek) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 1
            }

            val dayLetter = when (standardDayOfWeek) {
                1 -> "M"
                2 -> "T"
                3 -> "W"
                4 -> "T"
                5 -> "F"
                6 -> "S"
                7 -> "S"
                else -> "M"
            }

            list.add(
                DayInfo(
                    dateString = dateStr,
                    dayLetter = dayLetter,
                    dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
                    isToday = (dateStr == todayStr),
                    dayOfWeekNumber = standardDayOfWeek
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    /**
     * Generates a monthly grid for GitHub-style heatmap (weeks x 7 days)
     */
    fun getCalendarGridForMonth(year: Int, month: Int): List<MonthDayInfo> {
        val list = mutableListOf<MonthDayInfo>()
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        val todayCal = Calendar.getInstance()
        todayCal.set(Calendar.HOUR_OF_DAY, 0)
        todayCal.set(Calendar.MINUTE, 0)
        todayCal.set(Calendar.SECOND, 0)
        todayCal.set(Calendar.MILLISECOND, 0)
        val todayStr = getTodayString()

        // Days in this month
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // What day of week does the month start on? (Assuming Monday start)
        val startDayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0
            Calendar.TUESDAY -> 1
            Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3
            Calendar.FRIDAY -> 4
            Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6
            else -> 0
        }

        // Previous month fill
        if (startDayOfWeek > 0) {
            val prevCal = cal.clone() as Calendar
            prevCal.add(Calendar.DAY_OF_MONTH, -startDayOfWeek)
            for (i in 0 until startDayOfWeek) {
                val dStr = isoFormat.format(prevCal.time)
                list.add(
                    MonthDayInfo(
                        dateString = dStr,
                        dayOfMonth = prevCal.get(Calendar.DAY_OF_MONTH),
                        isCurrentMonth = false,
                        isFuture = prevCal.after(todayCal),
                        isToday = (dStr == todayStr),
                        dayOfWeek = i
                    )
                )
                prevCal.add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        // Current month days
        for (day in 1..daysInMonth) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            val dStr = isoFormat.format(cal.time)
            val dow = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> 0
                Calendar.TUESDAY -> 1
                Calendar.WEDNESDAY -> 2
                Calendar.THURSDAY -> 3
                Calendar.FRIDAY -> 4
                Calendar.SATURDAY -> 5
                Calendar.SUNDAY -> 6
                else -> 0
            }
            list.add(
                MonthDayInfo(
                    dateString = dStr,
                    dayOfMonth = day,
                    isCurrentMonth = true,
                    isFuture = cal.after(todayCal),
                    isToday = (dStr == todayStr),
                    dayOfWeek = dow
                )
            )
        }

        // Trailing days to round up to full week rows (multiples of 7)
        val remainder = list.size % 7
        if (remainder != 0) {
            val needed = 7 - remainder
            val nextCal = cal.clone() as Calendar
            for (i in 0 until needed) {
                nextCal.add(Calendar.DAY_OF_MONTH, 1)
                val dStr = isoFormat.format(nextCal.time)
                list.add(
                    MonthDayInfo(
                        dateString = dStr,
                        dayOfMonth = nextCal.get(Calendar.DAY_OF_MONTH),
                        isCurrentMonth = false,
                        isFuture = nextCal.after(todayCal),
                        isToday = (dStr == todayStr),
                        dayOfWeek = (list.size) % 7
                    )
                )
            }
        }

        return list
    }

    /**
     * Formats a given dateString (yyyy-MM-dd) to e.g. "Oct 14, 2026"
     */
    fun formatReadableDate(dateString: String): String {
        val date = parseDate(dateString) ?: return dateString
        val format = SimpleDateFormat("MMM d, yyyy", Locale.US)
        return format.format(date)
    }
}
