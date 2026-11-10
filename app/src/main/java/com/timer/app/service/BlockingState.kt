package com.timer.app.service

import android.content.Context
import android.content.SharedPreferences

object BlockingState {
    private const val PREFS_NAME = "timer_blocking_state"
    private const val KEY_IS_BLOCKED = "is_blocked"
    private const val KEY_JOKER_PACKAGES = "joker_packages"
    private const val KEY_COUPON_UNTIL = "coupon_until"
    private const val KEY_INDIVIDUAL_BLOCKED = "individual_blocked_pkgs"

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Global daily limit ────────────────────────────────────────────────
    fun setBlocked(ctx: Context, blocked: Boolean) {
        prefs(ctx).edit().putBoolean(KEY_IS_BLOCKED, blocked).apply()
    }
    fun isBlocked(ctx: Context): Boolean =
        prefs(ctx).getBoolean(KEY_IS_BLOCKED, false)

    // ── Per-app (leech individual limit exceeded) ─────────────────────────
    fun setIndividuallyBlockedPackages(ctx: Context, packages: Set<String>) {
        prefs(ctx).edit().putStringSet(KEY_INDIVIDUAL_BLOCKED, packages).apply()
    }
    fun getIndividuallyBlockedPackages(ctx: Context): Set<String> =
        prefs(ctx).getStringSet(KEY_INDIVIDUAL_BLOCKED, emptySet()) ?: emptySet()

    // ── Joker (always allowed) ────────────────────────────────────────────
    fun setJokerPackages(ctx: Context, packages: Set<String>) {
        prefs(ctx).edit().putStringSet(KEY_JOKER_PACKAGES, packages).apply()
    }
    fun getJokerPackages(ctx: Context): Set<String> =
        prefs(ctx).getStringSet(KEY_JOKER_PACKAGES, emptySet()) ?: emptySet()

    // ── Coupon ────────────────────────────────────────────────────────────
    fun setCouponUnlockUntil(ctx: Context, timestamp: Long) {
        prefs(ctx).edit().putLong(KEY_COUPON_UNTIL, timestamp).apply()
    }
    fun isCouponActive(ctx: Context): Boolean =
        System.currentTimeMillis() < prefs(ctx).getLong(KEY_COUPON_UNTIL, 0L)
}
