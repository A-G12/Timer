package com.timer.app.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import com.timer.app.data.database.dao.AppConfigDao
import com.timer.app.data.database.entity.AppConfigEntity
import com.timer.app.domain.model.AppInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppConfigRepository @Inject constructor(
    private val dao: AppConfigDao,
    @ApplicationContext private val context: Context
) {
    fun getAllConfiguredApps(): Flow<List<AppInfo>> = dao.getAllConfigs().map { entities ->
        entities.map { it.toAppInfo() }
    }

    suspend fun getInstalledApps(includeSystemApps: Boolean = false): List<AppInfo> {
        val pm = context.packageManager
        val configs = dao.getAllOnce().associateBy { it.packageName }

        val allPackages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.packageName != context.packageName }

        val launchablePackages = allPackages
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .toMutableList()

        // Always add home launcher even if no launch intent
        getHomePackage(pm)?.let { homePkg ->
            if (launchablePackages.none { it.packageName == homePkg }) {
                allPackages.find { it.packageName == homePkg }?.let { launchablePackages.add(it) }
            }
        }

        // Always add default keyboard even if no launch intent
        getImePackage()?.let { imePkg ->
            if (launchablePackages.none { it.packageName == imePkg }) {
                allPackages.find { it.packageName == imePkg }?.let { launchablePackages.add(it) }
            }
        }

        val pool = if (includeSystemApps) allPackages else launchablePackages

        return pool
            .distinctBy { it.packageName }
            .map { appInfo ->
                val config = configs[appInfo.packageName]
                AppInfo(
                    packageName = appInfo.packageName,
                    appName = pm.getApplicationLabel(appInfo).toString(),
                    isLeech = config?.isLeech ?: false,
                    isJoker = config?.isJoker ?: false,
                    isNoCount = config?.isNoCount ?: false,
                    customTimeLimitMinutes = config?.customTimeLimitMinutes ?: 0
                )
            }
            .sortedWith(compareBy({ !it.isLeech && !it.isJoker && !it.isNoCount }, { it.appName }))
    }

    suspend fun updateAppConfig(info: AppInfo) {
        if (!info.isLeech && !info.isJoker && !info.isNoCount && info.customTimeLimitMinutes == 0) {
            dao.deleteByPackage(info.packageName)
        } else {
            dao.upsert(AppConfigEntity(
                packageName = info.packageName,
                appName = info.appName,
                isLeech = info.isLeech,
                isJoker = info.isJoker,
                isNoCount = info.isNoCount,
                customTimeLimitMinutes = info.customTimeLimitMinutes
            ))
        }
    }

    suspend fun getNoCountPackages(): Set<String> =
        dao.getNoCountApps().map { it.packageName }.toSet()

    suspend fun getJokerPackages(): Set<String> {
        val fromDb = dao.getJokerApps().map { it.packageName }.toSet()
        val pm = context.packageManager
        val autoExempt = mutableSetOf<String>()
        getHomePackage(pm)?.let { autoExempt.add(it) }
        getImePackage()?.let { autoExempt.add(it) }
        return fromDb + autoExempt
    }

    suspend fun getLeechAppsWithLimits(): List<AppConfigEntity> =
        dao.getLeechApps()

    private fun getHomePackage(pm: PackageManager): String? = try {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
    } catch (_: Exception) { null }

    private fun getImePackage(): String? = try {
        val flat = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
        flat?.split("/")?.firstOrNull()
    } catch (_: Exception) { null }

    private fun AppConfigEntity.toAppInfo() = AppInfo(
        packageName = packageName,
        appName = appName,
        isLeech = isLeech,
        isJoker = isJoker,
        isNoCount = isNoCount,
        customTimeLimitMinutes = customTimeLimitMinutes
    )
}
