package com.timer.app.domain.model

data class Coupon(
    val id: Long = 0,
    val generatedAt: Long,
    val usedAt: Long? = null,
    val durationMinutes: Int = 30
) {
    val isAvailable: Boolean get() = usedAt == null
}
