package com.timer.app.data.database.dao

import androidx.room.*
import com.timer.app.data.database.entity.UsageRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageRecordDao {
    @Query("SELECT * FROM usage_records WHERE date = :date")
    fun getRecordsForDate(date: String): Flow<List<UsageRecordEntity>>

    @Query("SELECT * FROM usage_records WHERE date = :date")
    suspend fun getRecordsForDateOnce(date: String): List<UsageRecordEntity>

    @Query("SELECT SUM(usageMinutes) FROM usage_records WHERE date = :date")
    fun getTotalUsageForDate(date: String): Flow<Long?>

    @Upsert
    suspend fun upsert(record: UsageRecordEntity)

    @Query("DELETE FROM usage_records WHERE date < :cutoffDate")
    suspend fun deleteOldRecords(cutoffDate: String)

    @Query("DELETE FROM usage_records WHERE date = :date")
    suspend fun deleteRecordsForDate(date: String)
}
