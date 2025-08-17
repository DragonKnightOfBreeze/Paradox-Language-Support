package icu.windea.pls.ep.priority

import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

abstract class FilePathBasedParadoxPriorityProvider : ParadoxPriorityProvider {
    abstract fun getFilePathMap(gameType: ParadoxGameType): Map<String, ParadoxPriority>

    override fun getPriority(target: Any): ParadoxPriority? {
        val forcedDefinitionPriority = getForcedDefinitionPriority(target)
        if (forcedDefinitionPriority != null) return forcedDefinitionPriority

        val filePathPatterns = getFilePathPatterns(target)
        if (filePathPatterns.isEmpty()) return null
        val gameType = selectGameType(target) ?: return null
        val filePathMap = getFilePathMap(gameType)
        val priority = doGetPriority(filePathPatterns, filePathMap)
        return priority
    }

    override fun getPriority(searchParameters: ParadoxSearchParameters<*>): ParadoxPriority? {
        val forcedDefinitionPriority = getForcedDefinitionPriority(searchParameters)
        if (forcedDefinitionPriority != null) return forcedDefinitionPriority

        val filePathPatterns = getFilePathPatterns(searchParameters)
        if (filePathPatterns.isEmpty()) return null
        val gameType = searchParameters.selector.gameType ?: return null
        val filePathMap = getFilePathMap(gameType)
        val priority = doGetPriority(filePathPatterns, filePathMap)
        return priority
    }

    private fun getForcedDefinitionPriority(target: Any): ParadoxPriority? {
        if (target !is ParadoxScriptProperty) return null
        val definitionInfo = target.definitionInfo ?: return null
        val typeConfig = definitionInfo.typeConfig
        return doGetForcedDefinitionPriority(typeConfig)
    }

    private fun getForcedDefinitionPriority(searchParameters: ParadoxSearchParameters<*>): ParadoxPriority? {
        if (searchParameters !is ParadoxDefinitionSearch.SearchParameters) return null
        val definitionType = searchParameters.typeExpression?.substringBefore('.') ?: return null
        val gameType = searchParameters.selector.gameType ?: return null
        val configGroup = PlsFacade.getConfigGroup(searchParameters.project, gameType)
        val typeConfig = configGroup.types.get(definitionType) ?: return null
        return doGetForcedDefinitionPriority(typeConfig)
    }

    private fun doGetForcedDefinitionPriority(typeConfig: CwtTypeConfig): ParadoxPriority? {
        //event namespace -> ORDERED (don't care)
        if (typeConfig.name == "event_namespace") return ParadoxPriority.ORDERED
        //swapped type -> ORDERED (don't care)
        if (typeConfig.baseType != null) return ParadoxPriority.ORDERED
        //anonymous -> ORDERED (don't care)
        if (typeConfig.typeKeyFilter != null && typeConfig.nameField == null) return ParadoxPriority.ORDERED
        return null
    }

    private fun getFilePathPatterns(target: Any): Set<String> {
        return when {
            target is ParadoxScriptScriptedVariable -> {
                val targetPath = target.fileInfo?.path?.path ?: return emptySet()
                val p = "common/scripted_variables"
                p.takeIf { targetPath.matchesAntPattern(it) }.singleton().setOrEmpty()
            }
            target is ParadoxScriptDefinitionElement -> {
                val definitionInfo = target.definitionInfo ?: return emptySet()
                val definitionType = definitionInfo.type
                val configGroup = definitionInfo.configGroup
                val config = configGroup.types[definitionType] ?: return emptySet()
                config.filePathPatternsForPriority
            }
            target is ParadoxComplexEnumValueElement -> {
                val enumName = target.enumName
                val configGroup = PlsFacade.getConfigGroup(target.project, target.gameType)
                val config = configGroup.complexEnums[enumName] ?: return emptySet()
                config.filePathPatternsForPriority
            }
            target is ParadoxLocalisationProperty -> {
                val localisationCategory = target.localisationInfo?.category ?: return emptySet()
                val targetPath = target.fileInfo?.path?.path ?: return emptySet()
                val p = when (localisationCategory) {
                    ParadoxLocalisationCategory.Normal -> "localisation"
                    ParadoxLocalisationCategory.Synced -> "localisation_synced"
                }
                p.takeIf { targetPath.matchesAntPattern(it) }.singleton().setOrEmpty()
            }
            else -> emptySet()
        }
    }

    private fun getFilePathPatterns(searchParameters: ParadoxSearchParameters<*>): Set<String> {
        return when {
            searchParameters is ParadoxGlobalScriptedVariableSearch.SearchParameters -> {
                val p = "common/scripted_variables"
                p.singleton().set()
            }
            searchParameters is ParadoxDefinitionSearch.SearchParameters -> {
                val definitionType = searchParameters.typeExpression?.substringBefore('.') ?: return emptySet()
                val gameType = searchParameters.selector.gameType ?: return emptySet()
                val configGroup = PlsFacade.getConfigGroup(searchParameters.project, gameType)
                val config = configGroup.types.get(definitionType) ?: return emptySet()
                config.filePathPatternsForPriority
            }
            searchParameters is ParadoxComplexEnumValueSearch.SearchParameters -> {
                val enumName = searchParameters.enumName
                val gameType = searchParameters.selector.gameType ?: return emptySet()
                val configGroup = PlsFacade.getConfigGroup(searchParameters.project, gameType)
                val config = configGroup.complexEnums.get(enumName) ?: return emptySet()
                config.filePathPatternsForPriority
            }
            searchParameters is ParadoxLocalisationSearch.SearchParameters -> {
                val p = "localisation"
                p.singleton().set()
            }
            searchParameters is ParadoxSyncedLocalisationSearch.SearchParameters -> {
                val p = "localisation_synced"
                p.singleton().set()
            }
            else -> emptySet()
        }
    }

    private fun doGetPriority(filePathPatterns: Set<String>, filePathMap: Map<String, ParadoxPriority>): ParadoxPriority? {
        //TODO 1.3.35+ check performance

        val fastResult = filePathPatterns.firstNotNullOfOrNull { filePathMap[it] }
        if (fastResult != null) return fastResult
        val result = filePathPatterns.firstNotNullOfOrNull {
            if (it.none { c -> c == '*' || c == '?' }) return null
            filePathMap.firstNotNullOfOrNull { (k, v) ->
                if (k.matchesAntPattern(it)) v else null
            }
        }
        return result
    }
}
