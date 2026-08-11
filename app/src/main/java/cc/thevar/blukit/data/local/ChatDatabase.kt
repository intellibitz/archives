package cc.thevar.blukit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cc.thevar.blukit.data.local.dao.ContactDao
import cc.thevar.blukit.data.local.dao.MessageDao
import cc.thevar.blukit.data.local.dao.PeerDao
import cc.thevar.blukit.data.local.entities.ContactEntity
import cc.thevar.blukit.data.local.entities.MessageEntity
import cc.thevar.blukit.data.local.entities.PeerEntity

@Database(
    entities = [MessageEntity::class, ContactEntity::class, PeerEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {
    abstract val messageDao: MessageDao
    abstract val peerDao: PeerDao
    abstract val contactDao: ContactDao

    companion object {
        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getInstance(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    "chat.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
