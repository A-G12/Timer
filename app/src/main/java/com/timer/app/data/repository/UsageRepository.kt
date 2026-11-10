package com.timer.app.data.repository

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.timer.app.data.database.dao.UsageRecordDao
import com.timer.app.data.database.entity.UsageRecordEntity
import com.timer.app.data.datastore.SettingsDataStore
import com.timer.app.domain.model.DailyUsage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: UsageRecordDao,
    private val settings: SettingsDataStore,
    private val appConfigRepo: AppConfigRepository,
    private val synergyRepo: SynergyRepository
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun refreshUsageFromSystem() {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val startMs = getTodayStartMs()
        val endMs = System.currentTimeMillis()
        val noCountPkgs = appConfigRepo.getNoCountPackages()

        // Compute foreground time from raw events — most reliable across OEMs.
        // queryUsageStats / queryAndAggregateUsageStats aggregate stats in ways that
        // can include time outside the query window on Samsung and other OEMs.
        val usageMs = mutableMapOf<String, Long>()
        val foregroundSince = mutableMapOf<String, Long>()

        val events = usm.queryEvents(startMs, endMs)
        val ev = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(ev)
            val pkg = ev.packageName
            if (pkg == context.packageName || pkg in noCountPkgs) continue
            @Suppress("DEPRECATION")
            when (ev.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    foregroundSince[pkg] = ev.timeStamp
                }
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    // If no FOREGROUND seen in this window, the app was open at startMs
                    val since = foregroundSince.remove(pkg) ?: startMs
                    usageMs[pkg] = (usageMs[pkg] ?: 0L) + (ev.timeStamp - since)
                }
            }
        }
        // Apps still in foreground at query time
        foregroundSince.forEach { (pkg, since) ->
            usageMs[pkg] = (usageMs[pkg] ?: 0L) + (endMs - since)
        }

        // Wipe today's stale records before re-inserting clean computed values
        dao.deleteRecordsForDate(today())
        usageMs.forEach { (pkg, ms) ->
            val minutes = ms / 60_000L
            if (minutes > 0) dao.upsert(UsageRecordEntity(pkg, today(), minutes))
        }

        dao.deleteOldRecords(daysAgo(30))
    }

    private suspend fun getTodayStartMs(): Long {
        val resetHour = settings.resetHour.first()
        val resetMinute = settings.resetMinute.first()
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, resetHour)
        cal.set(Calendar.MINUTE, resetMinute)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        // If reset time is in the future today, the period started yesterday at reset time
        if (cal.timeInMillis > System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return cal.timeInMillis
    }

    suspend fun computeDailyUsage(): DailyUsage {
        val records = dao.getRecordsForDateOnce(today())
        val noCountPkgs = appConfigRepo.getNoCountPackages()
        val totalUsage = records
            .filter { it.packageName !in noCountPkgs }
            .sumOf { it.usageMinutes }

        val dailyLimit = settings.dailyLimitMinutes.first()
        val synergyPenalty = calculateSynergyPenalty(records.associateBy({ it.packageName }, { it.usageMinutes }))
        val couponBonus = getCouponBonusMinutes()

        return DailyUsage(
            totalUsageMinutes = totalUsage,
            dailyLimitMinutes = dailyLimit,
            synergyPenaltyMinutes = synergyPenalty,
            couponBonusMinutes = couponBonus
        )
    }

    private suspend fun calculateSynergyPenalty(usageMap: Map<String, Long>): Int {
        val leechApps = appConfigRepo.getLeechAppsWithLimits()
            .associateBy { it.packageName }
        val rules = synergyRepo.getAllRulesOnce()

        return rules
            .filter { rule ->
                rule.appPackageNames.all { pkg ->
                    val leechConfig = leechApps[pkg]
                    if (leechConfig == null || leechConfig.customTimeLimitMinutes <= 0) return@all false
                    val usage = usageMap[pkg] ?: 0L
                    usage >= leechConfig.customTimeLimitMinutes
                }
            }
            .sumOf { it.penaltyMinutes }
    }

    private suspend fun getCouponBonusMinutes(): Int {
        val unlockUntil = settings.couponUnlockUntil.first()
        val now = System.currentTimeMillis()
        return if (now < unlockUntil) ((unlockUntil - now) / 60_000L).toInt().coerceAtLeast(0) else 0
    }

    suspend fun getTodayUsageMap(): Map<String, Long> =
        dao.getRecordsForDateOnce(today()).associateBy({ it.packageName }, { it.usageMinutes })

    fun today(): String = dateFormat.format(Date())

    private fun daysAgo(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return dateFormat.format(cal.time)
    }
}
