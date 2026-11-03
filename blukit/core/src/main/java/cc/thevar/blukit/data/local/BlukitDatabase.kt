package cc.thevar.blukit.data.local

import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.ColumnTypeConverters
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import cc.thevar.blukit.data.local.dao.MessageDao
import cc.thevar.blukit.data.local.dao.GroupDao
import cc.thevar.blukit.data.local.dao.ContactDao
import cc.thevar.blukit.domain.model.Message
import cc.thevar.blukit.domain.model.Group
import cc.thevar.blukit.domain.model.ContactEntity
import cc.thevar.blukit.domain.model.PeerEntity

@Database(
    entities = [Message::class, Group::class, ContactEntity::class, PeerEntity::class],
    version = 1
)
@ColumnTypeConverters(RoomConverters::class)
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
abstract class BlukitDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun groupDao(): GroupDao
    abstract fun contactDao(): ContactDao
}
