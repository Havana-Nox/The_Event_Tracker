package com.alex.eventtracker.utils

import com.alex.eventtracker.data.model.Event
import java.time.DateTimeException
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object DateUtils {
    fun calculateDaysUntilNextEvent(
        event: Event,
        currentDate: LocalDate = LocalDate.now()
    ): Long {
        val eventMonth = event.date.monthValue
        val eventDay = event.date.dayOfMonth

        var nextDate = try {
            LocalDate.of(currentDate.year, eventMonth, eventDay)
        } catch (e: DateTimeException) {
            // Fallback for Feb 29 → Mar 1
            LocalDate.of(currentDate.year, 3, 1)
        }

        if (nextDate.isBefore(currentDate)) {
            nextDate = nextDate.plusYears(1)
        }

        return ChronoUnit.DAYS.between(currentDate, nextDate)
    }

    /**
     * Theres a bug if they make an event same year as current year but later date
     * shows 0
     * probably should just selfdestruct the whole app if they do that
     * something like remove system32
     */
    fun calculateAge(event: Event, currentDate: LocalDate = LocalDate.now()): Int? {
        if (!event.yearKnown) return null

        val birthYear = event.date.year
        val eventMonth = event.date.monthValue
        val eventDay = event.date.dayOfMonth

        var nextDate = try {
            LocalDate.of(currentDate.year, eventMonth, eventDay)
        } catch (e: DateTimeException) {
            LocalDate.of(currentDate.year, 3, 1)
        }

        if (nextDate.isBefore(currentDate)) {
            nextDate = nextDate.plusYears(1)
        }

        return nextDate.year - birthYear
    }


fun getEventProximityColor(daysUntil: Long): Long {
    return when (daysUntil) {
        0L -> 0xFFE53935L  // Red (Today)
        1L -> 0xFFEF5350L  // Light Red (Tomorrow)
        2L -> 0xFFFF8A65L  // Orange
        3L -> 0xFFFFB74DL  // Pale Orange
        else -> 0x00000000L // Transparent
        }
    }
}