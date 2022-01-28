package fi.hsl.hfp

import com.azure.storage.blob.BlobServiceClient
import com.azure.storage.blob.models.TaggedBlobItem
import fi.hsl.hfp.utils.roundToString
import mu.KotlinLogging
import java.nio.file.Path
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@ExperimentalTime
class BlobDownloader(private val blobServiceClient: BlobServiceClient, private val blobContainer: String) {
    companion object {
        private val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")
    }

    private val log = KotlinLogging.logger{}

    private val blobContainerClient = blobServiceClient.getBlobContainerClient(blobContainer)

    fun downloadBlobs(minTst: ZonedDateTime, maxTst: ZonedDateTime, directory: Path, onBlobDownloaded: (Path) -> Unit) {
        val minTstFormatted = minTst.withZoneSameInstant(ZoneId.of("UTC")).format(DATE_TIME_FORMATTER)
        val maxTstFormatted = maxTst.withZoneSameInstant(ZoneId.of("UTC")).format(DATE_TIME_FORMATTER)

        val blobs = mutableListOf<TaggedBlobItem>()
        //Need to do multiple queries because OR is not supported
        blobs += blobServiceClient.findBlobsByTags("@container='${blobContainer}' AND max_tst <= '${maxTstFormatted}' AND min_tst >= '${minTstFormatted}'")
        blobs += blobServiceClient.findBlobsByTags("@container='${blobContainer}' AND min_tst <= '${minTstFormatted}' AND max_tst >= '${minTstFormatted}'")
        blobs += blobServiceClient.findBlobsByTags("@container='${blobContainer}' AND min_tst <= '${maxTstFormatted}' AND max_tst >= '${maxTstFormatted}'")

        val blobNames = blobs.map { it.name }
            .distinct() //Queries can return same blob more than once
            .sorted() //Download oldest data first

        log.info { "${blobNames.size} blobs found containing data for time period from $minTstFormatted to $maxTstFormatted" }

        for (blobName in blobNames) {
            val file = directory.resolve(blobName).toAbsolutePath()

            val timedValue = measureTimedValue {
                blobContainerClient.getBlobClient(blobName).downloadToFile(file.toString())
            }

            val blobSize = timedValue.value.blobSize / 1024 / 1024
            val downloadSpeed = (((timedValue.value.blobSize.toDouble() / timedValue.duration.inWholeMilliseconds.toDouble()) * 1000.0) / (1024.0 * 1024.0)).roundToString(1)
            log.info { "Downloaded $blobName from blob storage to $file in ${timedValue.duration.inWholeSeconds} seconds ($blobSize MB, $downloadSpeed MB/s)" }

            onBlobDownloaded(file)
        }
    }

}