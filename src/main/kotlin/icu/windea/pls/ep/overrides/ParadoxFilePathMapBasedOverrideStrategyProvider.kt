package icu.windea.pls.ep.overrides

import icu.windea.pls.PlsFacade
import icu.windea.pls.config.configGroup.types
import icu.windea.pls.config.filePathPatternsForPriority
import icu.windea.pls.core.matchesAntPattern
import icu.windea.pls.core.util.set
import icu.windea.pls.core.util.setOrEmpty
import icu.windea.pls.core.util.singleton
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.localisationInfo
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy
import icu.windea.pls.lang.search.ParadoxDefineSearch
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.ParadoxSearchParameters
import icu.windea.pls.lang.search.ParadoxSyncedLocalisationSearch
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.model.ParadoxScriptedVariableType
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

abstract class ParadoxFilePathMapBasedOverrideStrategyProvider : ParadoxOverrideStrategyProvider {
    abstract fun getFilePathMap(gameType: ParadoxGameType): Map<String, ParadoxOverrideStrategy>

    override fun get(target: Any): ParadoxOverrideStrategy? {
        val filePathPatterns = getFilePathPatterns(target)
        if (filePathPatterns == null) return null
        val gameType = selectGameType(target) ?: return null
        val filePathMap = getFilePathMap(gameType)
        val overrideStrategy = getOverrideStrategy(filePathPatterns, filePathMap)
        return overrideStrategy
    }

    override fun get(searchParameters: ParadoxSearchParameters<*>): ParadoxOverrideStrategy? {
        val filePathPatterns = getFilePathPatterns(searchParameters)
        if (filePathPatterns == null) return null
        val gameType = searchParameters.selector.gameType ?: return null
        val filePathMap = getFilePathMap(gameType)
        val overrideStrategy = getOverrideStrategy(filePathPatterns, filePathMap)
        return overrideStrategy
    }

    private fun getFilePathPatterns(target: Any): Set<String>? {
        return when {
            target is ParadoxScriptScriptedVariable -> {
                val targetPath = target.fileInfo?.path?.path ?: return null
                val p = "common/scripted_variables"
                p.takeIf { targetPath.matchesAntPattern(it) }.singleton.setOrEmpty()
            }
            target is ParadoxScriptDefinitionElement -> {
                val definitionInfo = target.definitionInfo ?: return null
                val definitionType = definitionInfo.type
                val configGroup = definitionInfo.configGroup
                val config = configGroup.types[definitionType] ?: return emptySet()
                config.filePathPatternsForPriority
            }
            target is ParadoxLocalisationProperty -> {
                val localisationInfo = target.localisationInfo ?: return null
                val localisationType = localisationInfo.type
                val targetPath = target.fileInfo?.path?.path ?: return null
                val p = when (localisationType) {
                    ParadoxLocalisationType.Normal -> "localisation"
                    ParadoxLocalisationType.Synced -> "localisation_synced"
                }
                p.takeIf { targetPath.matchesAntPattern(it) }.singleton.setOrEmpty()
            }
            else -> null
        }
    }

    private fun getFilePathPatterns(searchParameters: ParadoxSearchParameters<*>): Set<String>? {
        return when {
            searchParameters is ParadoxScriptedVariableSearch.SearchParameters -> {
                if (searchParameters.type == ParadoxScriptedVariableType.Local) return null // 排除本地封装变量
                val p = "common/scripted_variables"
                p.singleton.set()
            }
            searchParameters is ParadoxDefinitionSearch.SearchParameters -> {
                val definitionType = searchParameters.typeExpression?.substringBefore('.') ?: return null
                val gameType = searchParameters.selector.gameType ?: return null
                val configGroup = PlsFacade.getConfigGroup(searchParameters.project, gameType)
                val config = configGroup.types.get(definitionType) ?: return emptySet()
                config.filePathPatternsForPriority
            }
            searchParameters is ParadoxLocalisationSearch.SearchParameters -> {
                val p = "localisation"
                p.singleton.set()
            }
            searchParameters is ParadoxSyncedLocalisationSearch.SearchParameters -> {
                val p = "localisation_synced"
                p.singleton.set()
            }
            // 额外兼容
            searchParameters is ParadoxDefineSearch.SearchParameters -> {
                val p = "common/defines"
                p.singleton.set()
            }
            else -> null
        }
    }

    private fun getOverrideStrategy(filePathPatterns: Set<String>, filePathMap: Map<String, ParadoxOverrideStrategy>): ParadoxOverrideStrategy {
        // TODO 1.3.35+ check performance

        if (filePathPatterns.isEmpty()) return ParadoxOverrideStrategy.LIOS // 如果适用覆盖方式，默认使用 `LIOS`
        val fastResult = filePathPatterns.firstNotNullOfOrNull { filePathMap[it] }
        if (fastResult != null) return fastResult
        val result = filePathPatterns.firstNotNullOfOrNull { p ->
            if (p.none { c -> c == '*' || c == '?' }) null
            else filePathMap.firstNotNullOfOrNull { (k, v) ->
                if (k.matchesAntPattern(p)) v else null
            }
        }
        if (result == null) return ParadoxOverrideStrategy.LIOS // 如果适用覆盖方式，默认使用 `LIOS`
        return result
    }
}
