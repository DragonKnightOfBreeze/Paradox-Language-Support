package icu.windea.pls.ep.priority

import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

abstract class FilePathBasedParadoxPriorityProvider : ParadoxPriorityProvider {
    object Keys : KeyRegistry() {
        val filePathsForPriority by createKey<Set<String>>(this)
    }

    abstract fun getFilePathMap(gameType: ParadoxGameType): Map<String, ParadoxPriority>

    override fun getPriority(target: Any): ParadoxPriority? {
        val forcedDefinitionPriority = getForcedDefinitionPriority(target)
        if (forcedDefinitionPriority != null) return forcedDefinitionPriority

        val filePaths = getFilePaths(target)
        if (filePaths.isEmpty()) return null
        val gameType = selectGameType(target) ?: return null
        val filePathMap = getFilePathMap(gameType)
        return filePaths.firstNotNullOfOrNull { filePathMap[it] }
    }

    override fun getPriority(searchParameters: ParadoxSearchParameters<*>): ParadoxPriority? {
        val forcedDefinitionPriority = getForcedDefinitionPriority(searchParameters)
        if (forcedDefinitionPriority != null) return forcedDefinitionPriority

        val filePaths = getFilePaths(searchParameters)
        if (filePaths.isEmpty()) return null
        val gameType = searchParameters.selector.gameType ?: return null
        val filePathMap = getFilePathMap(gameType)
        return filePaths.firstNotNullOfOrNull { filePathMap[it] }
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
        val configGroup = getConfigGroup(searchParameters.project, gameType)
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

    private fun getFilePaths(target: Any): Set<String> {
        return when {
            target is ParadoxScriptScriptedVariable -> {
                val targetPath = target.fileInfo?.path?.path ?: return emptySet()
                val filePath = "common/scripted_variables".takeIf { it.matchesPath(targetPath) } ?: return emptySet()
                filePath.toSingletonSet()
            }
            target is ParadoxScriptDefinitionElement -> {
                val definitionInfo = target.definitionInfo ?: return emptySet()
                val definitionType = definitionInfo.type
                val configGroup = definitionInfo.configGroup
                val config = configGroup.types[definitionType] ?: return emptySet()
                CwtConfigManager.getFilePathsForPriority(config)
            }
            target is ParadoxComplexEnumValueElement -> {
                val enumName = target.enumName
                val configGroup = getConfigGroup(target.project, target.gameType)
                val config = configGroup.complexEnums[enumName] ?: return emptySet()
                CwtConfigManager.getFilePathsForPriority(config)
            }
            target is ParadoxLocalisationProperty -> {
                val localisationCategory = target.localisationInfo?.category ?: return emptySet()
                val filePath = when (localisationCategory) {
                    ParadoxLocalisationCategory.Localisation -> "localisation"
                    ParadoxLocalisationCategory.SyncedLocalisation -> "localisation_synced"
                }
                filePath.toSingletonSet()
            }
            else -> emptySet()
        }
    }

    private fun getFilePaths(searchParameters: ParadoxSearchParameters<*>): Set<String> {
        return when {
            searchParameters is ParadoxGlobalScriptedVariableSearch.SearchParameters -> {
                val filePath = "common/scripted_variables"
                filePath.toSingletonSet()
            }
            searchParameters is ParadoxDefinitionSearch.SearchParameters -> {
                val definitionType = searchParameters.typeExpression?.substringBefore('.') ?: return emptySet()
                val gameType = searchParameters.selector.gameType ?: return emptySet()
                val configGroup = getConfigGroup(searchParameters.project, gameType)
                val config = configGroup.types.get(definitionType) ?: return emptySet()
                CwtConfigManager.getFilePathsForPriority(config)
            }
            searchParameters is ParadoxComplexEnumValueSearch.SearchParameters -> {
                val enumName = searchParameters.enumName
                val gameType = searchParameters.selector.gameType ?: return emptySet()
                val configGroup = getConfigGroup(searchParameters.project, gameType)
                val config = configGroup.complexEnums.get(enumName) ?: return emptySet()
                CwtConfigManager.getFilePathsForPriority(config)
            }
            searchParameters is ParadoxLocalisationSearch.SearchParameters -> {
                val filePath = "localisation"
                filePath.toSingletonSet()
            }
            searchParameters is ParadoxSyncedLocalisationSearch.SearchParameters -> {
                val filePath = "localisation_synced"
                filePath.toSingletonSet()
            }
            else -> emptySet()
        }
    }
}
