package com.timer.app.domain.model

data class SynergyRule(
    val id: Long = 0,
    val name: String,
    val penaltyMinutes: Int,
    val appPackageNames: List<String>
)
