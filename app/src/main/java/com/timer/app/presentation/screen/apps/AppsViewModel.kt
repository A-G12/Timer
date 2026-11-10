package com.timer.app.presentation.screen.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timer.app.data.repository.AppConfigRepository
import com.timer.app.domain.model.AppInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppsViewModel @Inject constructor(private val repo: AppConfigRepository) : ViewModel() {

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _search = MutableStateFlow("")
    val search: StateFlow<String> = _search.asStateFlow()

    private val _showSystemApps = MutableStateFlow(false)
    val showSystemApps: StateFlow<Boolean> = _showSystemApps.asStateFlow()

    val filteredApps: StateFlow<List<AppInfo>> = combine(_apps, _search) { apps, query ->
        if (query.isBlank()) apps else apps.filter { it.appName.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init { loadApps() }

    fun loadApps() {
        viewModelScope.launch {
            _isLoading.value = true
            _apps.value = repo.getInstalledApps(includeSystemApps = _showSystemApps.value)
            _isLoading.value = false
        }
    }

    fun setSearch(q: String) { _search.value = q }

    fun toggleSystemApps() {
        _showSystemApps.value = !_showSystemApps.value
        loadApps()
    }

    fun updateApp(app: AppInfo) {
        viewModelScope.launch {
            repo.updateAppConfig(app)
            val updated = _apps.value.map { if (it.packageName == app.packageName) app else it }
            _apps.value = updated
        }
    }
}
