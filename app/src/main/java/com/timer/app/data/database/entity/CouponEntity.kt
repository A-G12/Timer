package com.timer.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val generatedAt: Long,
    val usedAt: Long? = null,
    val durationMinutes: Int = 30
)
