package fi.hsl.hfp

import com.azure.storage.blob.BlobServiceClient
import fi.hsl.hfp.domain.HfpArchive
import mu.KotlinLogging
import java.nio.file.Path
import java.sql.Connection
import java.time.ZonedDateTime
import java.util.concurrent.LinkedBlockingQueue
import kotlin.time.ExperimentalTime

@ExperimentalTime
class HfpImporter(private val dataDirectory: Path, private val blobServiceClient: BlobServiceClient, private val blobContainer: String, private val databaseConnection: Connection) {
    private val log = KotlinLogging.logger {}

    fun importDataFrom(minDateTime: ZonedDateTime, maxDateTime: ZonedDateTime) {
        log.info { "Importing data from $minDateTime to $maxDateTime" }

        //TODO: limit queue size to avoid running out of memory
        val blobQueue = LinkedBlockingQueue<BlobQueueItem>()
        val hfpArchiveQueue = LinkedBlockingQueue<HfpArchiveQueueItem>()

        val threadGroup = object : ThreadGroup("HfpImporterThreadGroup") {
            override fun uncaughtException(t: Thread, e: Throwable) {
                log.warn(e) { "Uncaught exception in thread group" }

                super.uncaughtException(t, e)
            }
        }

        val blobDownloaderThread = Thread(threadGroup) {
            BlobDownloader(blobServiceClient, blobContainer).downloadBlobs(minDateTime, maxDateTime, dataDirectory) {
                blobQueue.put(BlobQueueItem.Blob(it))
            }
            log.info { "All blobs downloaded" }
            blobQueue.put(BlobQueueItem.End)
        }
        blobDownloaderThread.name = "BlobDownloaderThread"

        val hfpParserThread = Thread(threadGroup) {
            val hfpParser = HfpParser()

            while (true) {
                when(val blobQueueItem = blobQueue.take()) {
                    is BlobQueueItem.End -> {
                        hfpArchiveQueue.put(HfpArchiveQueueItem.End)
                        break
                    }
                    is BlobQueueItem.Blob -> {
                        hfpArchiveQueue.put(HfpArchiveQueueItem.Archive(hfpParser.parseHfpCsvArchive(blobQueueItem.path)))
                    }
                }
            }

            log.info { "All HFP archives parsed" }
        }
        hfpParserThread.name = "HfpParserThread"

        val dbInserter = Thread(threadGroup) {
            val dbBatchInserter = DbBatchInserter(databaseConnection)

            while (true) {
                when(val hfpArchiveQueueItem = hfpArchiveQueue.take()) {
                    is HfpArchiveQueueItem.End -> {
                        break
                    }
                    is HfpArchiveQueueItem.Archive -> {
                        dbBatchInserter.insertEvents(hfpArchiveQueueItem.hfpArchive)
                    }
                }
            }

            log.info { "All data inserted to DB" }
        }
        dbInserter.name = "DbInserterThread"

        blobDownloaderThread.start()
        hfpParserThread.start()
        dbInserter.start()

        dbInserter.join()
    }

    private sealed class BlobQueueItem {
        object End : BlobQueueItem()
        class Blob(val path: Path) : BlobQueueItem()
    }

    private sealed class HfpArchiveQueueItem {
        object End : HfpArchiveQueueItem()
        class Archive(val hfpArchive: HfpArchive): HfpArchiveQueueItem()
    }
}