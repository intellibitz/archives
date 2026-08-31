package cc.thevar.blukit.data.local.dao

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import cc.thevar.blukit.domain.model.Group
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups")
    fun getAllGroups(): Flow<List<Group>>

    @Query("SELECT * FROM groups WHERE id = :id")
    suspend fun getGroupById(id: String): Group?

    @Upsert
    suspend fun upsertGroup(group: Group)

    @Query("DELETE FROM groups WHERE id = :id")
    suspend fun deleteGroup(id: String)
}
