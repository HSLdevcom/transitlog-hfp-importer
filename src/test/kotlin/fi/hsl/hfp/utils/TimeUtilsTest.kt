package fi.hsl.hfp.utils

import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertTrue

class TimeUtilsTest {
    @Test
    fun `Test 2022-01-01 is between 2021-12-31 and 2022-01-02`() {
        assertTrue {
            val date = OffsetDateTime.of(2022, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

            date.isBetween(
                OffsetDateTime.of(2021, 12, 31, 0, 0, 0, 0, ZoneOffset.UTC),
                OffsetDateTime.of(2022, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC)
            )
        }
    }
}