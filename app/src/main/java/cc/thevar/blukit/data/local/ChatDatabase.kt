package cc.thevar.blukit.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import cc.thevar.blukit.data.local.dao.MessageDao
import cc.thevar.blukit.data.local.entities.ContactEntity
import cc.thevar.blukit.data.local.entities.MessageEntity

@Database(
    entities = [MessageEntity::class, ContactEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {
    abstract val messageDao: MessageDao
}
