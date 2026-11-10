package com.timer.app.data.database.entity

import androidx.room.Entity

@Entity(tableName = "usage_records", primaryKeys = ["packageName", "date"])
data class UsageRecordEntity(
    val packageName: String,
    val date: String,
    val usageMinutes: Long
)
