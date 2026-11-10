package com.timer.app.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "synergy_rules")
data class SynergyRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val penaltyMinutes: Int,
    val packagesJson: String
)
