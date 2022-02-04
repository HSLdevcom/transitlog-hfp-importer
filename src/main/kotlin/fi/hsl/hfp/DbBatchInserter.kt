package fi.hsl.hfp

import fi.hsl.hfp.domain.HfpArchive
import fi.hsl.hfp.utils.roundToString
import mu.KotlinLogging
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.postgresql.copy.CopyManager
import org.postgresql.copy.PGCopyOutputStream
import org.postgresql.core.BaseConnection
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.sql.Connection
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@ExperimentalTime
class DbBatchInserter(private val connection: Connection) {
    private val log = KotlinLogging.logger {}

    private fun createQuery(dbTable: String): String = """
        COPY $dbTable (acc,desi,dir,direction_id,dl,dr_type,drst,event_type,geohash_level,hdg,headsign,is_ongoing,journey_start_time,jrn,lat,line,loc,long,mode,next_stop_id,occu,oday,odo,oper,owner_operator_id,received_at,route,seq,spd,start,stop,topic_latitude,topic_longitude,topic_prefix,topic_version,tsi,tst,unique_vehicle_id,uuid,veh,vehicle_number) FROM STDIN (FORMAT csv, HEADER)
    """.trimIndent()

    private fun getCsvHeader(): Array<String> = arrayOf("acc", "desi", "dir", "direction_id", "dl", "dr_type", "drst", "event_type", "geohash_level", "hdg", "headsign", "is_ongoing", "journey_start_time", "jrn", "lat", "line", "loc", "long", "mode", "next_stop_id", "occu", "oday", "odo", "oper", "owner_operator_id", "received_at", "route", "seq", "spd", "start", "stop", "topic_latitude", "topic_longitude", "topic_prefix", "topic_version", "tsi", "tst", "unique_vehicle_id", "uuid", "veh", "vehicle_number")

    fun insertEvents(hfpArchive: HfpArchive) {
        val dbTable = DbTableHelper.getTableForEventType(hfpArchive.eventType)

        val duration = measureTime {
            val copyIn = CopyManager(connection.unwrap(BaseConnection::class.java)).copyIn(createQuery(dbTable))

            val csvPrinter = CSVPrinter(OutputStreamWriter(PGCopyOutputStream(copyIn), StandardCharsets.UTF_8), CSVFormat.RFC4180.withHeader(*getCsvHeader()))
            csvPrinter.use {
                for (event in hfpArchive.events) {
                    it.printRecord(
                        event.acc,
                        event.desi,
                        event.dir,
                        event.directionId,
                        event.dl,
                        event.drType,
                        event.drst,
                        event.eventType,
                        event.geohashLevel,
                        event.hdg,
                        event.headsign,
                        event.isOngoing,
                        event.journeyStartTime,
                        event.jrn,
                        event.latitude,
                        event.line,
                        event.locationQualityMethod,
                        event.longitude,
                        event.mode,
                        event.nextStopId,
                        event.occu,
                        event.oday,
                        event.odo?.toBigDecimal(),
                        event.oper,
                        event.ownerOperatorId,
                        event.receivedAt,
                        event.route,
                        event.seq,
                        event.spd,
                        event.start,
                        event.stop,
                        event.topicLatitude,
                        event.topicLongitude,
                        event.topicPrefix,
                        event.topicVersion,
                        event.tsi,
                        event.tst,
                        event.uniqueVehicleId,
                        event.uuid,
                        event.veh,
                        event.vehicleNumber
                    )
                }

                it.flush()
            }
        }

        val rowsPerSecond = ((hfpArchive.events.size.toDouble() / duration.inWholeMilliseconds) * 1000.0).roundToString(1)

        log.info { "${hfpArchive.events.size} rows inserted to $dbTable in ${duration.inWholeSeconds} seconds ($rowsPerSecond rows per second)" }
    }
}
