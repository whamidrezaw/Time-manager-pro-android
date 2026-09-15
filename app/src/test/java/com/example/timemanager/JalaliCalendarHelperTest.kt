package com.example.timemanager

import com.example.timemanager.util.JalaliCalendarHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class JalaliCalendarHelperTest {

    @Test
    fun testNowruzConversion() {
        // March 21, 2026 is 1 Farvardin 1405 (Nowruz)
        val jDate = JalaliCalendarHelper.gregorianToJalali(2026, 3, 21)
        assertEquals(1405, jDate.year)
        assertEquals(1, jDate.month)
        assertEquals(1, jDate.day)
        assertEquals("Farvardin", jDate.monthName())
    }

    @Test
    fun testRoundTripConversion() {
        // Test conversion from Gregorian to Jalali and back to Gregorian
        val originalYear = 2026
        val originalMonth = 9
        val originalDay = 14

        val jDate = JalaliCalendarHelper.gregorianToJalali(originalYear, originalMonth, originalDay)
        val (gYear, gMonth, gDay) = JalaliCalendarHelper.jalaliToGregorian(jDate.year, jDate.month, jDate.day)

        assertEquals(originalYear, gYear)
        assertEquals(originalMonth, gMonth)
        assertEquals(originalDay, gDay)
    }

    @Test
    fun testCountdownCalculation() {
        val today = JalaliCalendarHelper.getTodayGregorian()
        val cd = JalaliCalendarHelper.getCountdownText(today, null, true)
        assertEquals("Today", cd.label)
        assertEquals(0, cd.diffDays)
    }

    @Test
    fun testJalaliMonthDays() {
        // Farvardin through Shahrivar have 31 days
        assertEquals(31, JalaliCalendarHelper.daysInJalaliMonth(1405, 1))
        assertEquals(31, JalaliCalendarHelper.daysInJalaliMonth(1405, 6))

        // Mehr through Bahman have 30 days
        assertEquals(30, JalaliCalendarHelper.daysInJalaliMonth(1405, 7))
        assertEquals(30, JalaliCalendarHelper.daysInJalaliMonth(1405, 11))

        // Esfand in a non-leap year has 29 days
        assertEquals(false, JalaliCalendarHelper.isJalaliLeapYear(1405))
        assertEquals(29, JalaliCalendarHelper.daysInJalaliMonth(1405, 12))

        // 1403 was a Jalali leap year (30 days in Esfand)
        assertEquals(true, JalaliCalendarHelper.isJalaliLeapYear(1403))
        assertEquals(30, JalaliCalendarHelper.daysInJalaliMonth(1403, 12))
    }
}
