package com.dayflow.planner

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class PlannerLogicTest {
 @Test fun recurrenceMovesByRequestedPeriod() {
  val d=LocalDate.of(2025,1,31)
  assertEquals(d.plusDays(1),PlannerLogic.nextOccurrence(d,"daily"))
  assertEquals(d.plusWeeks(1),PlannerLogic.nextOccurrence(d,"weekly"))
  assertEquals(d.plusMonths(1),PlannerLogic.nextOccurrence(d,"monthly"))
  assertNull(PlannerLogic.nextOccurrence(d,"none"))
 }
 @Test fun habitStreakCountsConsecutiveDatesAndAllowsYesterdayStart() {
  val today=LocalDate.of(2025,5,10)
  assertEquals(3,PlannerLogic.habitStreak(listOf(today,today.minusDays(1),today.minusDays(2)),today))
  assertEquals(2,PlannerLogic.habitStreak(listOf(today.minusDays(1),today.minusDays(2)),today))
  assertEquals(0,PlannerLogic.habitStreak(listOf(today.minusDays(3)),today))
 }
 @Test fun taskCreationAndCompletionDataState() {
  val created=Task(title="Plan the day")
  assertFalse(created.done)
  assertEquals("Plan the day",created.title)
  assertTrue(created.copy(done=true).done)
 }
}
