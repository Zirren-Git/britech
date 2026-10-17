package com.dayflow.planner

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/** Pure date logic shared by task scheduling and habit progress. */
object PlannerLogic {
    fun nextOccurrence(date: LocalDate, recurrence: String): LocalDate? = when (recurrence.lowercase()) {
        "daily" -> date.plusDays(1)
        "weekly" -> date.plusWeeks(1)
        "monthly" -> date.plusMonths(1)
        else -> null
    }
    fun habitStreak(checkInDates: Collection<LocalDate>, today: LocalDate = LocalDate.now()): Int {
        val dates = checkInDates.toSet()
        var cursor = if (today in dates) today else today.minusDays(1)
        var count = 0
        while (cursor in dates) { count++; cursor = cursor.minusDays(1) }
        return count
    }
    fun occursOn(selectedWeekdays: Set<DayOfWeek>, date: LocalDate) = date.dayOfWeek in selectedWeekdays
    fun daysInMonth(date: LocalDate): Int = YearMonth.from(date).lengthOfMonth()
}
