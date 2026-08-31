package cc.thevar.blukit.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import cc.thevar.blukit.domain.model.Message
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingSource

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE groupId = :groupId ORDER BY timestamp DESC")
    fun getMessagesForGroupPaging(groupId: String): PagingSource<Int, Message>

    @Upsert
    suspend fun upsertMessage(message: Message)

    @Query("UPDATE messages SET status = :status WHERE messageId = :messageId")
    suspend fun updateStatus(messageId: String, status: Int)

    @Query("UPDATE messages SET connectionWeight = :weight WHERE messageId = :messageId")
    suspend fun updateWeight(messageId: String, weight: Int)

    @Query("DELETE FROM messages WHERE messageId = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM messages")
    suspend fun clearAll()

    @Query("SELECT * FROM messages WHERE timestamp > :timestamp ORDER BY timestamp ASC")
    suspend fun getMessagesSince(timestamp: Long): List<Message>

    @Query("SELECT messageId FROM messages ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMessageId(): String?

    @Query("SELECT * FROM messages WHERE type IN (16, 2, 17) ORDER BY timestamp DESC")
    fun getTimelineMessagesPaging(): PagingSource<Int, Message>

    @Query("SELECT * FROM messages ORDER BY timestamp DESC")
    fun getAllMessagesPaging(): PagingSource<Int, Message>

    @Query("SELECT * FROM messages WHERE parentMessageId = :parentId ORDER BY timestamp ASC")
    fun getChildMessagesPaging(parentId: String): PagingSource<Int, Message>
    
    @Query("SELECT * FROM messages WHERE messageId = :messageId")
    suspend fun getMessageById(messageId: String): Message?
}
