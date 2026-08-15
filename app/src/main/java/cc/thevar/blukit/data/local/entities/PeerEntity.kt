package cc.thevar.blukit.data.local.entities

import kotlinx.serialization.Serializable

@Serializable
data class PeerEntity(
    val endpointId: String,
    val name: String?,
    val publicKey: String, // Base64 encoded public key
    val lastSeen: Long
)
