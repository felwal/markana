package com.felwal.markana.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

val FORMATTER_TODAY: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
val FORMATTER_THIS_YEAR: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM")
val FORMATTER_EARLIER: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")

// epoch

fun Long.fromEpochSecond(): LocalDateTime =
    LocalDateTime.ofEpochSecond(this, 0, ZoneOffset.UTC)

fun LocalDateTime.toEpochSecond(): Long =
    atZone(ZoneId.of("UTC")).toEpochSecond()

// convert

fun LocalDate.atStartOfWeek(): LocalDateTime =
    minusDays((dayOfWeek.value - 1).toLong()).atStartOfDay()

fun LocalDate.atEndOfWeek(): LocalDateTime =
    plusDays((7 - dayOfWeek.value).toLong()).atTime(23, 59, 59)

fun LocalDate.atStartOfMonth(): LocalDateTime =
    minusDays((dayOfMonth - 1).toLong()).atStartOfDay()

fun LocalDate.atEndOfMonth(): LocalDateTime =
    plusDays((lengthOfMonth() - dayOfMonth).toLong()).atTime(23, 59, 59)

fun LocalDate.atStartOfYear(): LocalDateTime =
    minusDays((dayOfYear - 1).toLong()).atStartOfDay()

fun LocalDate.atEndOfYear(): LocalDateTime =
    plusDays((lengthOfYear() - dayOfYear).toLong()).atTime(23, 59, 59)