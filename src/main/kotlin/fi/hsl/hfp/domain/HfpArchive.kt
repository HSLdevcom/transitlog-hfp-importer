package fi.hsl.hfp.domain

import fi.hsl.transitlog.hfp.domain.Event

data class HfpArchive(val eventType: String, val events: List<Event>)