package cc.thevar.blukit.domain.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "contacts")
@Serializable
data class ContactEntity(
    @PrimaryKey val id: String,
    val nickname: String,
    val emoji: String,
    val publicKey: String, // Base64
    val lastPulseAt: Long
)
