package fi.hsl.hfp.domain

import fi.hsl.transitlog.hfp.domain.IEvent

data class HfpArchive(val eventType: String, val events: List<IEvent>)