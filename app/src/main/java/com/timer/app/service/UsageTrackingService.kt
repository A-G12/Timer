package com.timer.app.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.timer.app.MainActivity
import com.timer.app.data.datastore.SettingsDataStore
import com.timer.app.data.repository.AppConfigRepository
import com.timer.app.data.repository.CouponRepository
import com.timer.app.data.repository.UsageRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class UsageTrackingService : Service() {

    @Inject lateinit var usageRepo: UsageRepository
    @Inject lateinit var appConfigRepo: AppConfigRepository
    @Inject lateinit var couponRepo: CouponRepository
    @Inject lateinit var settings: SettingsDataStore

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val channelId = "timer_tracking"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, buildNotification("Monitoreando uso..."))
        startTracking()
    }

    private fun startTracking() {
        scope.launch {
            couponRepo.generateWeeklyCouponsIfNeeded()
            while (isActive) {
                try {
                    usageRepo.refreshUsageFromSystem()
                    val usage = usageRepo.computeDailyUsage()
                    val usageMap = usageRepo.getTodayUsageMap()

                    // Joker packages (from DB + auto-exempt home/keyboard)
                    val jokerPackages = appConfigRepo.getJokerPackages()
                    BlockingState.setJokerPackages(applicationContext, jokerPackages)

                    // Global daily limit
                    BlockingState.setBlocked(applicationContext, usage.isBlocked)

                    // Per-app leech limits
                    val leechApps = appConfigRepo.getLeechAppsWithLimits()
                    val individuallyBlocked = leechApps
                        .filter { it.customTimeLimitMinutes > 0 }
                        .filter { app -> (usageMap[app.packageName] ?: 0L) >= app.customTimeLimitMinutes }
                        .map { it.packageName }
                        .toSet()
                    BlockingState.setIndividuallyBlockedPackages(applicationContext, individuallyBlocked)

                    // Sync coupon state to SharedPreferences for the AccessibilityService
                    val couponUntil = settings.couponUnlockUntil.first()
                    BlockingState.setCouponUnlockUntil(applicationContext, couponUntil)

                    val remaining = usage.remainingMinutes
                    val msg = when {
                        usage.isBlocked -> "Tiempo diario agotado"
                        individuallyBlocked.isNotEmpty() -> "${individuallyBlocked.size} app(s) bloqueada(s). Quedan ${remaining}min"
                        else -> "Quedan ${remaining}min"
                    }
                    updateNotification(msg)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(60_000L)
            }
        }
    }

    private fun updateNotification(text: String) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(1, buildNotification(text))
    }

    private fun buildNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Timer")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(channelId, "Timer Tracking", NotificationManager.IMPORTANCE_LOW)
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
