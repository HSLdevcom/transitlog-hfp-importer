package fi.hsl.hfp.utils

import org.junit.jupiter.api.assertTimeout
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.ExperimentalTime

@ExperimentalTime
class RetryTest {
    @Test
    fun `Test retry`() {
        var counter = 0

        val func = {
            counter++

            if (counter < 5) {
                throw IllegalStateException("Counter less than 5")
            }
        }

        assertTimeout(Duration.ofMillis(2000)) {
            retry(func, Duration.ofMillis(100))
        }
    }

    @Test
    fun `Test max retries`() {
        assertFailsWith<java.lang.Exception> {
            val func = { throw java.lang.IllegalStateException("Error") }

            retry(func, Duration.ofMillis( 100), 4)
        }
    }
}