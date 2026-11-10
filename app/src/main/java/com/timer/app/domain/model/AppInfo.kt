package com.timer.app.domain.model

data class AppInfo(
    val packageName: String,
    val appName: String,
    val isLeech: Boolean = false,
    val isJoker: Boolean = false,
    val isNoCount: Boolean = false,
    val customTimeLimitMinutes: Int = 0,
    val todayUsageMinutes: Long = 0
) {
    val countsTowardLimit: Boolean get() = !isNoCount
    val isAlwaysAllowed: Boolean get() = isJoker
    val hasExceededLeechLimit: Boolean
        get() = isLeech && customTimeLimitMinutes > 0 && todayUsageMinutes >= customTimeLimitMinutes
}
