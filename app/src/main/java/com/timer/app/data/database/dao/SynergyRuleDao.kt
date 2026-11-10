package com.timer.app.data.database.dao

import androidx.room.*
import com.timer.app.data.database.entity.SynergyRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SynergyRuleDao {
    @Query("SELECT * FROM synergy_rules ORDER BY name ASC")
    fun getAllRules(): Flow<List<SynergyRuleEntity>>

    @Query("SELECT * FROM synergy_rules")
    suspend fun getAllRulesOnce(): List<SynergyRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: SynergyRuleEntity): Long

    @Update
    suspend fun update(rule: SynergyRuleEntity)

    @Delete
    suspend fun delete(rule: SynergyRuleEntity)
}
