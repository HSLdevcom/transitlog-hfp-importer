package fi.hsl.hfp

import fi.hsl.hfp.domain.HfpArchive
import fi.hsl.hfp.utils.roundToString
import fi.hsl.hfp.utils.toDate
import fi.hsl.hfp.utils.toTime
import fi.hsl.hfp.utils.toTimestamp
import mu.KotlinLogging
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Types
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@ExperimentalTime
class DbBatchInserter(private val connection: Connection) {
    companion object {
        private const val INSERT_BATCH_SIZE = 10000
    }

    private val log = KotlinLogging.logger {}

    private fun createQuery(tableName: String): String = """
                INSERT INTO $tableName (
                    acc,
                    desi,
                    dir,
                    direction_id,
                    dl,
                    dr_type,
                    drst,
                    event_type,
                    geohash_level,
                    hdg,
                    headsign,
                    is_ongoing,
                    journey_start_time,
                    jrn,
                    lat,
                    line,
                    loc,
                    long,
                    mode,
                    next_stop_id,
                    occu,
                    oday,
                    odo,
                    oper,
                    owner_operator_id,
                    received_at,
                    route,
                    seq,
                    spd,
                    start,
                    stop,
                    topic_latitude,
                    topic_longitude,
                    topic_prefix,
                    topic_version,
                    tsi,
                    tst,
                    unique_vehicle_id,
                    uuid,
                    veh,
                    vehicle_number,
                    version
                ) VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?);
            """.trimIndent()

    fun insertEvents(hfpArchive: HfpArchive) {
        val dbTable = when(hfpArchive.eventType) {
            "DUE", "ARR", "ARS", "DEP", "PDE", "PAS", "WAIT" -> "stopevent"
            "VP" -> "vehicleposition"
            else -> "otherevent"
        }

        val duration = measureTime {
            val statement = connection.prepareStatement(createQuery(dbTable))

            val batches = hfpArchive.events.chunked(INSERT_BATCH_SIZE)
            for (eventBatch in batches) {
                for (event in eventBatch) {
                    statement.setDouble(1, event.acc)
                    statement.setString(2, event.desi)
                    statement.setInt(3, event.dir)
                    statement.setInt(4, event.directionId)
                    statement.setInt(5, event.dl)
                    statement.setInt(6, event.drType)
                    statement.setBoolean(7, event.drst)
                    statement.setString(8, event.eventType)
                    statement.setInt(9, event.geohashLevel)
                    statement.setInt(10, event.hdg)
                    statement.setString(11, event.headsign)
                    statement.setBoolean(12, event.isOngoing)
                    statement.setTime(13, event.journeyStartTime?.toTime())
                    statement.setInt(14, event.jrn)
                    statement.setDouble(15, event.latitude)
                    statement.setInt(16, event.line)
                    statement.setString(17, event.locationQualityMethod)
                    statement.setDouble(18, event.longitude)
                    statement.setString(19, event.mode)
                    statement.setString(20, event.nextStopId)
                    statement.setInt(21, event.occu)
                    statement.setDate(22, event.oday?.toDate())
                    statement.setDouble(23, event.odo)
                    statement.setInt(24, event.oper)
                    statement.setInt(25, event.ownerOperatorId)
                    statement.setTimestamp(26, event.receivedAt?.toTimestamp())
                    statement.setString(27, event.route)
                    statement.setInt(28, event.seq)
                    statement.setDouble(29, event.spd)
                    statement.setTime(30, event.start?.toTime())
                    statement.setInt(31, event.stop)
                    statement.setDouble(32, event.topicLatitude)
                    statement.setDouble(33, event.topicLongitude)
                    statement.setString(34, event.topicPrefix)
                    statement.setString(35, event.topicVersion)
                    statement.setLong(36, event.tsi)
                    statement.setObject(37, event.tst, Types.TIMESTAMP_WITH_TIMEZONE)
                    statement.setString(38, event.uniqueVehicleId)
                    statement.setObject(39, event.uuid, Types.OTHER)
                    statement.setInt(40, event.veh)
                    statement.setInt(41, event.vehicleNumber)
                    statement.setInt(42, 1)

                    statement.addBatch()
                }

                statement.executeBatch()
            }
        }

        val rowsPerSecond = ((hfpArchive.events.size.toDouble() / duration.inWholeMilliseconds) * 1000.0).roundToString(1)

        log.info { "${hfpArchive.events.size} rows inserted to $dbTable in ${duration.inWholeSeconds} seconds ($rowsPerSecond rows per second)" }
    }

    private fun PreparedStatement.setLong(index: Int, value: Long?) {
        if (value == null) {
            this.setNull(index, Types.BIGINT)
        } else {
            this.setLong(index, value)
        }
    }

    private fun PreparedStatement.setBoolean(index: Int, value: Boolean?) {
        if (value == null) {
            this.setNull(index, Types.BOOLEAN)
        } else {
            this.setBoolean(index, value)
        }
    }

    private fun PreparedStatement.setInt(index: Int, value: Int?) {
        if (value == null) {
            this.setNull(index, Types.INTEGER)
        } else {
            this.setInt(index, value)
        }
    }

    private fun PreparedStatement.setDouble(index: Int, value: Double?) {
        if (value == null) {
            this.setNull(index, Types.DOUBLE)
        } else {
            this.setDouble(index, value)
        }
    }
}
