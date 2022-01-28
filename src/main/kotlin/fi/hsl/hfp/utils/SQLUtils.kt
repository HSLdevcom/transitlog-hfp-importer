package fi.hsl.hfp.utils

import java.sql.Date
import java.sql.Time
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

fun LocalTime.toTime(): Time = Time.valueOf(this)

fun LocalDate.toDate(): Date = Date.valueOf(this)

fun Instant.toTimestamp(): Timestamp = Timestamp.from(this)
