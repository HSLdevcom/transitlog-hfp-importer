package fi.hsl.hfp

import com.github.luben.zstd.RecyclingBufferPool
import fi.hsl.hfp.domain.HfpArchive
import fi.hsl.transitlog.hfp.domain.Event
import fi.hsl.transitlog.hfp.domain.IEvent
import fi.hsl.transitlog.hfp.domain.LightPriorityEvent
import mu.KotlinLogging
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.BufferedInputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.*
import java.util.*
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@ExperimentalTime
class HfpParser {
    companion object {
        private const val BUFFER_SIZE = 128 * 1024 //128kb
    }

    private val log = KotlinLogging.logger {}

    private fun createCsvParser(path: Path): CSVParser = CSVParser.parse(
        InputStreamReader(ZstdCompressorInputStream(BufferedInputStream(Files.newInputStream(path), BUFFER_SIZE), RecyclingBufferPool.INSTANCE), StandardCharsets.UTF_8),
        CSVFormat.RFC4180.withFirstRecordAsHeader()
    )

    fun parseHfpCsvArchive(path: Path): HfpArchive {
        val fileSize = Files.size(path) / 1024 / 1024

        var eventType: String? = null

        val timedValue = measureTimedValue {
            createCsvParser(path).use {
                val events = ArrayList<IEvent>()

                it.forEach { csvRecord ->
                    if (eventType == null) {
                        eventType = csvRecord["eventType"]

                        if (eventType == "VP") {
                            events.ensureCapacity(500000)
                        } else {
                            events.ensureCapacity(2000)
                        }
                    }

                    if (eventType == "TLA" || eventType == "TLR") {
                        events += LightPriorityEvent(
                            UUID.fromString(csvRecord["uuid"]),
                            OffsetDateTime.parse(csvRecord["tst"]),
                            csvRecord["uniqueVehicleId"],
                            csvRecord["eventType"],
                            csvRecord["journeyType"],
                            Instant.parse(csvRecord["receivedAt"]),
                            csvRecord["topicPrefix"],
                            csvRecord["topicVersion"],
                            csvRecord["isOngoing"].toBooleanStrictOrNull(),
                            csvRecord["mode"],
                            csvRecord["ownerOperatorId"].toIntOrNull(),
                            csvRecord["vehicleNumber"].toIntOrNull(),
                            csvRecord["routeId"],
                            csvRecord["directionId"].toIntOrNull(),
                            csvRecord["headsign"],
                            LocalTime.parse(csvRecord["journeyStartTime"]),
                            csvRecord["nextStopId"],
                            csvRecord["geohashLevel"].toIntOrNull(),
                            csvRecord["topicLatitude"].toDoubleOrNull(),
                            csvRecord["topicLongitude"].toDoubleOrNull(),
                            csvRecord["latitude"].toDoubleOrNull(),
                            csvRecord["longitude"].toDoubleOrNull(),
                            csvRecord["desi"],
                            csvRecord["dir"].toIntOrNull(),
                            csvRecord["oper"].toIntOrNull(),
                            csvRecord["veh"].toIntOrNull(),
                            csvRecord["tsi"].toLongOrNull(),
                            csvRecord["spd"].toDoubleOrNull(),
                            csvRecord["hdg"].toIntOrNull(),
                            csvRecord["acc"].toDoubleOrNull(),
                            csvRecord["dl"].toIntOrNull(),
                            csvRecord["odo"].toDoubleOrNull(),
                            csvRecord["drst"].toBooleanStrictOrNull(),
                            LocalDate.parse(csvRecord["oday"]),
                            csvRecord["jrn"].toIntOrNull(),
                            csvRecord["line"].toIntOrNull(),
                            LocalTime.parse(csvRecord["start"]),
                            csvRecord["locationQualityMethod"],
                            csvRecord["stop"].toIntOrNull(),
                            csvRecord["route"],
                            csvRecord["occu"].toIntOrNull(),
                            csvRecord["seq"].toIntOrNull(),
                            csvRecord["drType"].toIntOrNull(),
                            csvRecord["tlpRequestId"].toIntOrNull(),
                            csvRecord["tlpRequestType"],
                            csvRecord["tlpPriorityLevel"],
                            csvRecord["tlpReason"],
                            csvRecord["tlpAttSeq"].toIntOrNull(),
                            csvRecord["tlpDecision"],
                            csvRecord["sid"].toIntOrNull(),
                            csvRecord["signalGroupId"].toIntOrNull(),
                            csvRecord["tlpSignalGroupNbr"].toIntOrNull(),
                            csvRecord["tlpLineConfigId"].toIntOrNull(),
                            csvRecord["tlpPointConfigId"].toIntOrNull(),
                            csvRecord["tlpFrequency"].toIntOrNull(),
                            csvRecord["tlpProtocol"]
                        )
                    } else {
                        events += Event(
                            UUID.fromString(csvRecord["uuid"]),
                            OffsetDateTime.parse(csvRecord["tst"]),
                            csvRecord["uniqueVehicleId"],
                            csvRecord["eventType"],
                            csvRecord["journeyType"],
                            Instant.parse(csvRecord["receivedAt"]),
                            csvRecord["topicPrefix"],
                            csvRecord["topicVersion"],
                            csvRecord["isOngoing"].toBooleanStrictOrNull(),
                            csvRecord["mode"],
                            csvRecord["ownerOperatorId"].toIntOrNull(),
                            csvRecord["vehicleNumber"].toIntOrNull(),
                            csvRecord["routeId"],
                            csvRecord["directionId"].toIntOrNull(),
                            csvRecord["headsign"],
                            LocalTime.parse(csvRecord["journeyStartTime"]),
                            csvRecord["nextStopId"],
                            csvRecord["geohashLevel"].toIntOrNull(),
                            csvRecord["topicLatitude"].toDoubleOrNull(),
                            csvRecord["topicLongitude"].toDoubleOrNull(),
                            csvRecord["latitude"].toDoubleOrNull(),
                            csvRecord["longitude"].toDoubleOrNull(),
                            csvRecord["desi"],
                            csvRecord["dir"].toIntOrNull(),
                            csvRecord["oper"].toIntOrNull(),
                            csvRecord["veh"].toIntOrNull(),
                            csvRecord["tsi"].toLongOrNull(),
                            csvRecord["spd"].toDoubleOrNull(),
                            csvRecord["hdg"].toIntOrNull(),
                            csvRecord["acc"].toDoubleOrNull(),
                            csvRecord["dl"].toIntOrNull(),
                            csvRecord["odo"].toDoubleOrNull(),
                            csvRecord["drst"].toBooleanStrictOrNull(),
                            LocalDate.parse(csvRecord["oday"]),
                            csvRecord["jrn"].toIntOrNull(),
                            csvRecord["line"].toIntOrNull(),
                            LocalTime.parse(csvRecord["start"]),
                            csvRecord["locationQualityMethod"],
                            csvRecord["stop"].toIntOrNull(),
                            csvRecord["route"],
                            csvRecord["occu"].toIntOrNull(),
                            csvRecord["seq"].toIntOrNull(),
                            csvRecord["drType"].toIntOrNull()
                        )
                    }
                }

                return@use events.toList()
            }
        }

        log.info { "Processed $path ($fileSize MB, ${timedValue.value.size} rows) in ${timedValue.duration.inWholeSeconds} seconds " }

        return HfpArchive(eventType!!, timedValue.value)
    }
}