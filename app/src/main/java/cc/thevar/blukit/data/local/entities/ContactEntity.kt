package cc.thevar.blukit.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey
    val contactId: String,
    val name: String,
    val bluetoothAddress: String,
    val lastSeen: Long,
    val avatarUri: String? = null
)
