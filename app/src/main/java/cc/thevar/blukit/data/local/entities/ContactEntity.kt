package cc.thevar.blukit.data.local.entities

import kotlinx.serialization.Serializable

@Serializable
data class ContactEntity(
    val id: String,
    val nickname: String,
    val emoji: String,
    val publicKey: String, // Base64
    val lastPulseAt: Long
)
