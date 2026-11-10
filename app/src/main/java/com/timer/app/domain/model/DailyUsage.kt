package com.timer.app.domain.model

data class DailyUsage(
    val totalUsageMinutes: Long,
    val dailyLimitMinutes: Int,
    val synergyPenaltyMinutes: Int,
    val couponBonusMinutes: Int
) {
    val effectiveLimitMinutes: Int
        get() = (dailyLimitMinutes - synergyPenaltyMinutes + couponBonusMinutes).coerceAtLeast(1)
    val remainingMinutes: Long
        get() = (effectiveLimitMinutes - totalUsageMinutes).coerceAtLeast(0)
    val isBlocked: Boolean
        get() = totalUsageMinutes >= effectiveLimitMinutes
    val progressFraction: Float
        get() = (totalUsageMinutes.toFloat() / effectiveLimitMinutes).coerceIn(0f, 1f)
}
