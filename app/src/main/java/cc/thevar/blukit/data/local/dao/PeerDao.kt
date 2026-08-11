package cc.thevar.blukit.data.local.dao

import androidx.room.*
import cc.thevar.blukit.data.local.entities.PeerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PeerDao {
    @Query("SELECT * FROM peers WHERE endpointId = :id")
    suspend fun getPeer(id: String): PeerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeer(peer: PeerEntity)

    @Query("DELETE FROM peers")
    suspend fun clearPeers()
}
