package com.timer.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "timer_settings")

@Singleton
class SettingsDataStore @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        val DAILY_LIMIT_MINUTES = intPreferencesKey("daily_limit_minutes")
        val COUPON_DURATION_MINUTES = intPreferencesKey("coupon_duration_minutes")
        val RESET_HOUR = intPreferencesKey("reset_hour")
        val RESET_MINUTE = intPreferencesKey("reset_minute")
        val IS_SERVICE_ENABLED = booleanPreferencesKey("is_service_enabled")
        val COUPON_UNLOCK_UNTIL = longPreferencesKey("coupon_unlock_until")
        val LAST_COUPON_WEEK = intPreferencesKey("last_coupon_week")
        val COUPON_SECRET = stringPreferencesKey("coupon_secret")
    }

    val dailyLimitMinutes: Flow<Int> = context.dataStore.data.map {
        it[DAILY_LIMIT_MINUTES] ?: 120
    }

    val couponDurationMinutes: Flow<Int> = context.dataStore.data.map {
        it[COUPON_DURATION_MINUTES] ?: 30
    }

    val resetHour: Flow<Int> = context.dataStore.data.map { it[RESET_HOUR] ?: 0 }
    val resetMinute: Flow<Int> = context.dataStore.data.map { it[RESET_MINUTE] ?: 0 }
    val isServiceEnabled: Flow<Boolean> = context.dataStore.data.map { it[IS_SERVICE_ENABLED] ?: true }

    val couponUnlockUntil: Flow<Long> = context.dataStore.data.map { it[COUPON_UNLOCK_UNTIL] ?: 0L }
    val lastCouponWeek: Flow<Int> = context.dataStore.data.map { it[LAST_COUPON_WEEK] ?: 0 }
    val couponSecret: Flow<String> = context.dataStore.data.map { it[COUPON_SECRET] ?: "TIMER_SECRET_CHANGE_ME" }

    suspend fun setDailyLimitMinutes(minutes: Int) {
        context.dataStore.edit { it[DAILY_LIMIT_MINUTES] = minutes }
    }

    suspend fun setCouponDurationMinutes(minutes: Int) {
        context.dataStore.edit { it[COUPON_DURATION_MINUTES] = minutes }
    }

    suspend fun setResetTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[RESET_HOUR] = hour
            it[RESET_MINUTE] = minute
        }
    }

    suspend fun setServiceEnabled(enabled: Boolean) {
        context.dataStore.edit { it[IS_SERVICE_ENABLED] = enabled }
    }

    suspend fun setCouponUnlockUntil(timestamp: Long) {
        context.dataStore.edit { it[COUPON_UNLOCK_UNTIL] = timestamp }
    }

    suspend fun setLastCouponWeek(week: Int) {
        context.dataStore.edit { it[LAST_COUPON_WEEK] = week }
    }

    suspend fun setCouponSecret(secret: String) {
        context.dataStore.edit { it[COUPON_SECRET] = secret }
    }
}
