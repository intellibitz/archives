package cc.thevar.blukit.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "peers")
data class PeerEntity(
    @PrimaryKey
    val endpointId: String,
    val name: String?,
    val publicKey: String, // Base64 encoded public key
    val lastSeen: Long
)
