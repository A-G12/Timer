package com.timer.app.data.database.dao

import androidx.room.*
import com.timer.app.data.database.entity.CouponEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CouponDao {
    @Query("SELECT * FROM coupons ORDER BY generatedAt DESC")
    fun getAllCoupons(): Flow<List<CouponEntity>>

    @Query("SELECT * FROM coupons WHERE usedAt IS NULL ORDER BY generatedAt ASC")
    suspend fun getAvailableCoupons(): List<CouponEntity>

    @Query("SELECT COUNT(*) FROM coupons WHERE usedAt IS NULL")
    fun getAvailableCount(): Flow<Int>

    @Insert
    suspend fun insert(coupon: CouponEntity): Long

    @Update
    suspend fun update(coupon: CouponEntity)

    @Query("DELETE FROM coupons WHERE id = :id")
    suspend fun deleteById(id: Long)
}
