package fi.hsl.transitlog.hfp.domain

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.util.*

data class Event(
    override val uuid: UUID,
    override val tst: OffsetDateTime,
    override val uniqueVehicleId: String?,
    override val eventType: String?,
    override val journeyType: String?,
    override val receivedAt: Instant?,
    override val topicPrefix: String?,
    override val topicVersion: String?,
    override val isOngoing: Boolean?,
    override val mode: String?,
    override val ownerOperatorId: Int?,
    override val vehicleNumber: Int?,
    override val routeId: String?,
    override val directionId: Int?,
    override val headsign: String?,
    override val journeyStartTime: LocalTime?,
    override val nextStopId: String?,
    override val geohashLevel: Int?,
    override val topicLatitude: Double?,
    override val topicLongitude: Double?,
    override val latitude: Double?,
    override val longitude: Double?,
    override val desi: String?,
    override val dir: Int?,
    override val oper: Int?,
    override val veh: Int?,
    override val tsi: Long?,
    override val spd: Double?,
    override val hdg: Int?,
    override val acc: Double?,
    override val dl: Int?,
    override val odo: Double?,
    override val drst: Boolean?,
    override val oday: LocalDate?,
    override val jrn: Int?,
    override val line: Int?,
    override val start: LocalTime?,
    override val locationQualityMethod: String?,
    override val stop: Int?,
    override val route: String?,
    override val occu: Int?,
    override val seq: Int?,
    override val drType: Int?
) : IEvent()