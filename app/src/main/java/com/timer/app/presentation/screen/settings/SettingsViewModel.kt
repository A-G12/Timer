package com.timer.app.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timer.app.data.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val settings: SettingsDataStore) : ViewModel() {

    val dailyLimitMinutes: StateFlow<Int> = settings.dailyLimitMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 120)

    val couponDurationMinutes: StateFlow<Int> = settings.couponDurationMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)

    val resetHour: StateFlow<Int> = settings.resetHour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val resetMinute: StateFlow<Int> = settings.resetMinute
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val couponSecret: StateFlow<String> = settings.couponSecret
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun setDailyLimit(minutes: Int) {
        viewModelScope.launch { settings.setDailyLimitMinutes(minutes) }
    }

    fun setCouponDuration(minutes: Int) {
        viewModelScope.launch { settings.setCouponDurationMinutes(minutes) }
    }

    fun setResetTime(hour: Int, minute: Int) {
        viewModelScope.launch { settings.setResetTime(hour, minute) }
    }

    fun setCouponSecret(secret: String) {
        viewModelScope.launch { settings.setCouponSecret(secret) }
    }
}
