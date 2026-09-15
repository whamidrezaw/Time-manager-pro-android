package com.example.timemanager.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class JalaliDate(val year: Int, val month: Int, val day: Int) {
    fun format(): String = "%04d/%02d/%02d".format(year, month, day)
    fun monthName(): String = JalaliCalendarHelper.JALALI_MONTH_NAMES[month.coerceIn(1, 12) - 1]
    fun monthNameFa(): String = JalaliCalendarHelper.JALALI_MONTH_NAMES_FA[month.coerceIn(1, 12) - 1]
}

object JalaliCalendarHelper {
    val JALALI_MONTH_NAMES = listOf(
        "Farvardin", "Ordibehesht", "Khordad",
        "Tir", "Mordad", "Shahrivar",
        "Mehr", "Aban", "Azar",
        "Dey", "Bahman", "Esfand"
    )

    val JALALI_MONTH_NAMES_FA = listOf(
        "فروردین", "اردیبهشت", "خرداد",
        "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر",
        "دی", "بهمن", "اسفند"
    )

    val GREGORIAN_MONTH_NAMES = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    /**
     * Converts Gregorian Year, Month (1-12), Day to Jalali Year, Month, Day.
     * Uses the standard Julian Day Number (JDN) algorithm.
     */
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) (gy + 1) else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + gDaysInMonth[gm - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += ((days - 1) / 365)
            days = (days - 1) % 365
        }
        val jm = if (days < 186) 1 + (days / 31) else 7 + ((days - 186) / 30)
        val jd = 1 + (if (days < 186) (days % 31) else ((days - 186) % 30))
        return JalaliDate(jy, jm, jd)
    }

    /**
     * Converts Jalali Year, Month (1-12), Day to Gregorian Year, Month, Day.
     */
    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val jy2 = jy + 1595
        var days = -355668 + (365 * jy2) + ((jy2 / 33) * 8) + (((jy2 % 33) + 3) / 4) + jd +
                (if (jm < 7) ((jm - 1) * 31) else (((jm - 7) * 30) + 186))
        var gy = 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            days--
            gy += 100 * (days / 36524)
            days %= 36524
            if (days >= 365) days++
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            gy += ((days - 1) / 365)
            days = (days - 1) % 365
        }
        val isLeap = (gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)
        val salA = intArrayOf(0, 31, if (isLeap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        while (gm < 13 && days >= salA[gm]) {
            days -= salA[gm]
            gm++
        }
        return Triple(gy, gm, days + 1)
    }

    fun toJalaliString(isoDate: String): String {
        return try {
            val parts = isoDate.split("-")
            if (parts.size == 3) {
                val j = gregorianToJalali(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
                j.format()
            } else isoDate
        } catch (e: Exception) {
            isoDate
        }
    }

    fun daysInJalaliMonth(year: Int, month: Int): Int {
        return when {
            month in 1..6 -> 31
            month in 7..11 -> 30
            isJalaliLeapYear(year) -> 30
            else -> 29
        }
    }

    /**
     * Checks if a Jalali year is a leap year (kabiseh) by verifying if Esfand 30 exists.
     * Guaranteed 100% consistent with the conversion algorithms.
     */
    fun isJalaliLeapYear(year: Int): Boolean {
        val (gy, gm, gd) = jalaliToGregorian(year, 12, 30)
        val jDate = gregorianToJalali(gy, gm, gd)
        return jDate.year == year && jDate.month == 12 && jDate.day == 30
    }

    fun getTodayGregorian(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun getTodayJalali(): JalaliDate {
        val todayIso = getTodayGregorian()
        val parts = todayIso.split("-").mapNotNull { it.toIntOrNull() }
        return if (parts.size == 3) {
            gregorianToJalali(parts[0], parts[1], parts[2])
        } else {
            JalaliDate(1405, 1, 1)
        }
    }

    fun formatJalaliFullFa(year: Int, month: Int, day: Int): String {
        val mName = JALALI_MONTH_NAMES_FA.getOrNull(month.coerceIn(1, 12) - 1) ?: ""
        return "$day $mName $year"
    }

    fun getCountdownText(eventDateIso: String, timeHm: String? = null, isAllDay: Boolean = true): CountdownResult {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val eventDate = sdf.parse(eventDateIso) ?: return CountdownResult("Upcoming", 0, false)
            val calEvent = Calendar.getInstance().apply {
                time = eventDate
                if (!isAllDay && timeHm != null && timeHm.contains(":")) {
                    val timeParts = timeHm.split(":")
                    set(Calendar.HOUR_OF_DAY, timeParts[0].toIntOrNull() ?: 0)
                    set(Calendar.MINUTE, timeParts[1].toIntOrNull() ?: 0)
                } else {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                }
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val calToday = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val diffMillis = calEvent.timeInMillis - calToday.timeInMillis
            val diffDays = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

            when {
                diffDays == 0 -> CountdownResult("Today", 0, false)
                diffDays == 1 -> CountdownResult("Tomorrow", 1, false)
                diffDays == -1 -> CountdownResult("Yesterday", -1, true)
                diffDays > 1 -> CountdownResult("In $diffDays days", diffDays, false)
                else -> CountdownResult("${-diffDays} days ago", diffDays, true)
            }
        } catch (e: Exception) {
            CountdownResult(eventDateIso, 0, false)
        }
    }
}

data class CountdownResult(
    val label: String,
    val diffDays: Int,
    val isPast: Boolean
)
