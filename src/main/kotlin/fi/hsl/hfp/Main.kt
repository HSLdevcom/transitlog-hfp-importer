package fi.hsl.hfp

import com.azure.storage.blob.BlobServiceClientBuilder
import mu.KotlinLogging
import org.apache.commons.cli.*
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.DriverManager
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException
import kotlin.system.exitProcess
import kotlin.time.ExperimentalTime

fun CommandLine.getOptionValueOrPrintError(option: String): String {
    if (hasOption(option)) {
        return getOptionValue(option)
    } else {
        println("Missing option '$option' from command line arguments")
        exitProcess(1)
    }
}

fun parseDateOrPrintError(dateStr: String): LocalDateTime {
    try {
        return LocalDateTime.parse(dateStr)
    } catch (e: DateTimeParseException) {
        println("Invalid date time: $dateStr")
        exitProcess(1)
    }
}

@ExperimentalTime
fun main(args: Array<String>) {
    val log = KotlinLogging.logger{}

    val options = Options().apply {
        addOption("s", true, "Azure Blob Storage connection string")
        addOption("c", true, "Blob container")
        addOption("d", true, "Database connection string")
        addOption("f", true, "Minimum timestamp for data")
        addOption("t", true, "Maximum timestamp for data")
        addOption("h", "help", false, "Print options")
    }

    val cliParser = DefaultParser()
    val cli = cliParser.parse(options, args)

    if (cli.hasOption("h")) {
        HelpFormatter().printHelp("java -jar transitlog-hfp-importer.jar", options)
        exitProcess(0)
    }

    val blobStorageConnectionString = cli.getOptionValueOrPrintError("s")
    val blobContainer = cli.getOptionValueOrPrintError("c")

    val databaseConnectionString = cli.getOptionValueOrPrintError("d")

    val minDateStr = cli.getOptionValueOrPrintError("f")
    val minDate = parseDateOrPrintError(minDateStr)

    val maxDateStr = cli.getOptionValueOrPrintError("t")
    val maxDate = parseDateOrPrintError(maxDateStr)

    if (minDate >= maxDate) {
        println("Minimum timestamp must be earlier than maximum timestamp")
        exitProcess(1)
    }

    val blobServiceClient = BlobServiceClientBuilder().connectionString(blobStorageConnectionString).buildClient()

    val dataDir = Paths.get("hfp")
    Files.createDirectories(dataDir)

    val connection = DriverManager.getConnection(databaseConnectionString)

    HfpImporter(dataDir, blobServiceClient, blobContainer, connection)
        .importDataFrom(
            minDate.atZone(ZoneId.of("UTC")),
            maxDate.atZone(ZoneId.of("UTC"))
        )
}