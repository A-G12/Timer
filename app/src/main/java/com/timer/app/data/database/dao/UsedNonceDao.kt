package com.timer.app.data.database.dao

import androidx.room.*
import com.timer.app.data.database.entity.UsedNonceEntity

@Dao
interface UsedNonceDao {
    @Query("SELECT COUNT(*) FROM used_nonces WHERE nonce = :nonce")
    suspend fun exists(nonce: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: UsedNonceEntity)

    @Query("DELETE FROM used_nonces WHERE usedAt < :cutoff")
    suspend fun deleteOld(cutoff: Long)
}
