package fi.hsl.hfp

object DbTableHelper {
    private const val STOP_EVENT_TABLE = "stopevent"
    private const val OTHER_EVENT_TABLE = "otherevent"
    private const val UNSIGNED_EVENT_TABLE = "unsignedevent"
    private const val LIGHT_PRIORITY_EVENT_TABLE = "lightpriorityevent"
    private const val VEHICLE_POSITION_TABLE = "vehicleposition"

    private val eventToDbTable = mapOf(
        "DUE" to STOP_EVENT_TABLE,
        "ARR" to STOP_EVENT_TABLE,
        "ARS" to STOP_EVENT_TABLE,
        "DEP" to STOP_EVENT_TABLE,
        "PDE" to STOP_EVENT_TABLE,
        "PAS" to STOP_EVENT_TABLE,
        "WAIT" to STOP_EVENT_TABLE,
        "TLR" to LIGHT_PRIORITY_EVENT_TABLE,
        "TLA" to LIGHT_PRIORITY_EVENT_TABLE,
        "VP" to VEHICLE_POSITION_TABLE
    )

    val databaseTables = setOf(STOP_EVENT_TABLE, OTHER_EVENT_TABLE, UNSIGNED_EVENT_TABLE, LIGHT_PRIORITY_EVENT_TABLE, VEHICLE_POSITION_TABLE)

    fun getTableForEventType(eventType: String): String = eventToDbTable.getOrDefault(eventType, OTHER_EVENT_TABLE)
}