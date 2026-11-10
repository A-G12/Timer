package com.timer.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_configs")
data class AppConfigEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val isLeech: Boolean = false,
    val isJoker: Boolean = false,
    val isNoCount: Boolean = false,
    val customTimeLimitMinutes: Int = 0
)
