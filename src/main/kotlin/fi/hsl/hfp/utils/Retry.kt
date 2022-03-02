package fi.hsl.hfp.utils

import mu.KotlinLogging
import java.time.Duration
import kotlin.math.pow

private val log = KotlinLogging.logger {}

fun retry(func: () -> (Unit), initialDelay: Duration, maxRetries: Long = -1) {
    var retryCount: Long = 0

    while (true) {
        try {
            func()

            break
        } catch (e: Exception) {
            if (maxRetries > 0 && (retryCount + 1) > maxRetries) {
                throw java.lang.Exception("Retried \"$func\" $retryCount times without success", e)
            }

            val delayMillis = initialDelay.toMillis() * 2.0.pow(retryCount++.toDouble()).toLong()

            log.info { "Error executing \"$func\": ${e.message}, retrying in ${delayMillis}ms" }

            Thread.sleep(delayMillis)
        }
    }
}