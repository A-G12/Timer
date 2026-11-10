package com.timer.app.presentation.screen.synergy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timer.app.data.repository.AppConfigRepository
import com.timer.app.data.repository.SynergyRepository
import com.timer.app.domain.model.AppInfo
import com.timer.app.domain.model.SynergyRule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SynergyViewModel @Inject constructor(
    private val synergyRepo: SynergyRepository,
    private val appConfigRepo: AppConfigRepository
) : ViewModel() {

    val rules: StateFlow<List<SynergyRule>> = synergyRepo.getAllRules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _leechApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val leechApps: StateFlow<List<AppInfo>> = _leechApps.asStateFlow()

    init {
        viewModelScope.launch {
            _leechApps.value = appConfigRepo.getInstalledApps().filter { it.isLeech && it.customTimeLimitMinutes > 0 }
        }
    }

    fun saveRule(rule: SynergyRule) {
        viewModelScope.launch { synergyRepo.saveRule(rule) }
    }

    fun deleteRule(rule: SynergyRule) {
        viewModelScope.launch { synergyRepo.deleteRule(rule) }
    }
}
