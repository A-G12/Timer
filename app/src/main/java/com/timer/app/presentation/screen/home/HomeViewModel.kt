package com.timer.app.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timer.app.data.repository.AppConfigRepository
import com.timer.app.data.repository.CouponRepository
import com.timer.app.data.repository.UsageRepository
import com.timer.app.domain.model.AppInfo
import com.timer.app.domain.model.DailyUsage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val usageRepo: UsageRepository,
    private val couponRepo: CouponRepository,
    private val appConfigRepo: AppConfigRepository
) : ViewModel() {

    private val _usage = MutableStateFlow<DailyUsage?>(null)
    val usage: StateFlow<DailyUsage?> = _usage.asStateFlow()

    private val _blockedLeechApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val blockedLeechApps: StateFlow<List<AppInfo>> = _blockedLeechApps.asStateFlow()

    val availableCoupons: StateFlow<Int> = couponRepo.getAvailableCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            usageRepo.refreshUsageFromSystem()
            val usage = usageRepo.computeDailyUsage()
            _usage.value = usage

            val usageMap = usageRepo.getTodayUsageMap()
            val leechApps = appConfigRepo.getLeechAppsWithLimits()
            _blockedLeechApps.value = leechApps
                .filter { it.customTimeLimitMinutes > 0 }
                .filter { app -> (usageMap[app.packageName] ?: 0L) >= app.customTimeLimitMinutes }
                .map { app ->
                    AppInfo(
                        packageName = app.packageName,
                        appName = app.appName,
                        isLeech = true,
                        customTimeLimitMinutes = app.customTimeLimitMinutes,
                        todayUsageMinutes = usageMap[app.packageName] ?: 0L
                    )
                }
        }
    }
}
