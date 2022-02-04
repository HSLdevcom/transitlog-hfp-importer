package fi.hsl.hfp.utils

import java.time.OffsetDateTime

fun OffsetDateTime.isBetween(min: OffsetDateTime, max: OffsetDateTime): Boolean = (this.isAfter(min) or this.isEqual(min)) and (this.isBefore(max) or this.isEqual(max))