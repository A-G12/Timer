package com.timer.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.timer.app.data.database.AppDatabase
import com.timer.app.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """CREATE TABLE IF NOT EXISTS `used_nonces` (
                `nonce` TEXT NOT NULL,
                `usedAt` INTEGER NOT NULL,
                PRIMARY KEY(`nonce`)
            )"""
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "timer_db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides fun provideAppConfigDao(db: AppDatabase): AppConfigDao = db.appConfigDao()
    @Provides fun provideSynergyRuleDao(db: AppDatabase): SynergyRuleDao = db.synergyRuleDao()
    @Provides fun provideCouponDao(db: AppDatabase): CouponDao = db.couponDao()
    @Provides fun provideUsageRecordDao(db: AppDatabase): UsageRecordDao = db.usageRecordDao()
    @Provides fun provideUsedNonceDao(db: AppDatabase): UsedNonceDao = db.usedNonceDao()
}
