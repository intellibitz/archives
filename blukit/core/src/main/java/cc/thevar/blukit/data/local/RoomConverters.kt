package cc.thevar.blukit.data.local

import androidx.room3.ColumnTypeConverter
import cc.thevar.blukit.domain.model.GroupEvent
import cc.thevar.blukit.domain.model.RoomConnection
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RoomConverters {
    @ColumnTypeConverter
    fun fromStringSet(value: Set<String>): String = Json.encodeToString(value)
    
    @ColumnTypeConverter
    fun toStringSet(value: String): Set<String> = try { Json.decodeFromString(value) } catch (_: Exception) { emptySet() }

    @ColumnTypeConverter
    fun fromStringMap(value: Map<String, String>): String = Json.encodeToString(value)

    @ColumnTypeConverter
    fun toStringMap(value: String): Map<String, String> = try { Json.decodeFromString(value) } catch (_: Exception) { emptyMap() }

    @ColumnTypeConverter
    fun fromStringListMap(value: Map<String, Set<String>>): String = Json.encodeToString(value)

    @ColumnTypeConverter
    fun toStringListMap(value: String): Map<String, Set<String>> = try { Json.decodeFromString(value) } catch (_: Exception) { emptyMap() }

    @ColumnTypeConverter
    fun fromGroupEventList(value: List<GroupEvent>): String = Json.encodeToString(value)

    @ColumnTypeConverter
    fun toGroupEventList(value: String): List<GroupEvent> = try { Json.decodeFromString(value) } catch (_: Exception) { emptyList() }

    @ColumnTypeConverter
    fun fromRoomConnectionList(value: List<RoomConnection>): String = Json.encodeToString(value)

    @ColumnTypeConverter
    fun toRoomConnectionList(value: String): List<RoomConnection> = try { Json.decodeFromString(value) } catch (_: Exception) { emptyList() }
}
