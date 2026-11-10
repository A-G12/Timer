package com.timer.app.service

import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent
import android.accessibilityservice.AccessibilityService
import com.timer.app.presentation.screen.blocked.BlockedActivity

class TimerAccessibilityService : AccessibilityService() {

    private var lastBlockedPackage: String? = null
    private var cachedHomePackage: String? = null
    private var cachedImePackage: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        refreshSystemPackages()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return

        if (shouldBlock(pkg)) {
            if (lastBlockedPackage != pkg) {
                lastBlockedPackage = pkg
                launchBlockedScreen(pkg)
            }
        } else {
            if (lastBlockedPackage != null) lastBlockedPackage = null
        }
    }

    private fun shouldBlock(pkg: String): Boolean {
        // Coupon unlocks absolutely everything
        if (BlockingState.isCouponActive(this)) return false
        // System-critical apps are never blocked
        if (isAlwaysExempt(pkg)) return false
        // Joker apps are never blocked
        if (pkg in BlockingState.getJokerPackages(this)) return false
        // Global daily limit exceeded → block everything non-exempt
        if (BlockingState.isBlocked(this)) return true
        // Individual leech limit exceeded for this specific app
        if (pkg in BlockingState.getIndividuallyBlockedPackages(this)) return true

        return false
    }

    private fun isAlwaysExempt(pkg: String): Boolean {
        if (pkg == packageName) return true
        if (pkg == "android") return true
        if (pkg.contains("systemui", ignoreCase = true)) return true
        if (pkg.contains("launcher3", ignoreCase = true)) return true
        if (pkg == cachedHomePackage) return true
        if (pkg == cachedImePackage) return true
        refreshSystemPackages()
        return pkg == cachedHomePackage || pkg == cachedImePackage
    }

    private fun refreshSystemPackages() {
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            cachedHomePackage = packageManager
                .resolveActivity(homeIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
                ?.activityInfo?.packageName
        } catch (_: Exception) {}
        try {
            val imeFlat = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
            cachedImePackage = imeFlat?.split("/")?.firstOrNull()
        } catch (_: Exception) {}
    }

    private fun launchBlockedScreen(blockedPkg: String) {
        val intent = Intent(this, BlockedActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(BlockedActivity.EXTRA_BLOCKED_PACKAGE, blockedPkg)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {
        lastBlockedPackage = null
    }
}
