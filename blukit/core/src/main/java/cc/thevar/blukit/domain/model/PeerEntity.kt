package cc.thevar.blukit.domain.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "peers")
@Serializable
data class PeerEntity(
    @PrimaryKey val endpointId: String,
    val name: String?,
    val publicKey: String, // Base64 encoded public key
    val lastSeen: Long
)
