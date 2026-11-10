package com.timer.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.timer.app.data.database.dao.*
import com.timer.app.data.database.entity.*

@Database(
    entities = [
        AppConfigEntity::class,
        SynergyRuleEntity::class,
        CouponEntity::class,
        UsageRecordEntity::class,
        UsedNonceEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appConfigDao(): AppConfigDao
    abstract fun synergyRuleDao(): SynergyRuleDao
    abstract fun couponDao(): CouponDao
    abstract fun usageRecordDao(): UsageRecordDao
    abstract fun usedNonceDao(): UsedNonceDao
}
