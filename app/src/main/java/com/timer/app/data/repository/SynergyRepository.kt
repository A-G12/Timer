package com.timer.app.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.timer.app.data.database.dao.SynergyRuleDao
import com.timer.app.data.database.entity.SynergyRuleEntity
import com.timer.app.domain.model.SynergyRule
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SynergyRepository @Inject constructor(private val dao: SynergyRuleDao) {
    private val gson = Gson()
    private val stringListType = object : TypeToken<List<String>>() {}.type

    fun getAllRules(): Flow<List<SynergyRule>> = dao.getAllRules().map { entities ->
        entities.map { it.toModel() }
    }

    suspend fun getAllRulesOnce(): List<SynergyRule> =
        dao.getAllRulesOnce().map { it.toModel() }

    suspend fun saveRule(rule: SynergyRule) {
        val entity = SynergyRuleEntity(
            id = rule.id,
            name = rule.name,
            penaltyMinutes = rule.penaltyMinutes,
            packagesJson = gson.toJson(rule.appPackageNames)
        )
        if (rule.id == 0L) dao.insert(entity) else dao.update(entity)
    }

    suspend fun deleteRule(rule: SynergyRule) {
        dao.delete(SynergyRuleEntity(id = rule.id, name = rule.name, penaltyMinutes = 0, packagesJson = ""))
    }

    private fun SynergyRuleEntity.toModel(): SynergyRule {
        val packages: List<String> = try {
            gson.fromJson(packagesJson, stringListType) ?: emptyList()
        } catch (e: Exception) { emptyList() }
        return SynergyRule(id = id, name = name, penaltyMinutes = penaltyMinutes, appPackageNames = packages)
    }
}
