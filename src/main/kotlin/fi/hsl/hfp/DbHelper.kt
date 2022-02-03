package fi.hsl.hfp

import java.sql.Connection
import java.sql.Types
import java.time.ZonedDateTime

class DbHelper(private val connection: Connection) {
    /**
     * @return Number of rows deleted
     */
    fun deleteDataFromPeriod(tableName: String, minTst: ZonedDateTime, maxTst: ZonedDateTime): Int {
        val statement = connection.prepareStatement("DELETE FROM $tableName WHERE tst >= ? AND tst <= ?")

        statement.use {
            it.setObject(1, minTst.toOffsetDateTime(), Types.TIMESTAMP_WITH_TIMEZONE)
            it.setObject(2, maxTst.toOffsetDateTime(), Types.TIMESTAMP_WITH_TIMEZONE)

            return it.executeUpdate()
        }
    }
}