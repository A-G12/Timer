package com.timer.app.data.database.dao

import androidx.room.*
import com.timer.app.data.database.entity.AppConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppConfigDao {
    @Query("SELECT * FROM app_configs ORDER BY appName ASC")
    fun getAllConfigs(): Flow<List<AppConfigEntity>>

    @Query("SELECT * FROM app_configs")
    suspend fun getAllOnce(): List<AppConfigEntity>

    @Query("SELECT * FROM app_configs WHERE packageName = :pkg LIMIT 1")
    suspend fun getConfig(pkg: String): AppConfigEntity?

    @Query("SELECT * FROM app_configs WHERE isLeech = 1")
    suspend fun getLeechApps(): List<AppConfigEntity>

    @Query("SELECT * FROM app_configs WHERE isJoker = 1")
    suspend fun getJokerApps(): List<AppConfigEntity>

    @Query("SELECT * FROM app_configs WHERE isNoCount = 1")
    suspend fun getNoCountApps(): List<AppConfigEntity>

    @Upsert
    suspend fun upsert(config: AppConfigEntity)

    @Delete
    suspend fun delete(config: AppConfigEntity)

    @Query("DELETE FROM app_configs WHERE packageName = :pkg")
    suspend fun deleteByPackage(pkg: String)
}
