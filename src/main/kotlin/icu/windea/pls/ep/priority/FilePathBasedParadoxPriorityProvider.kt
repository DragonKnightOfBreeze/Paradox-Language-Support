package icu.windea.pls.ep.priority

import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

abstract class FilePathBasedParadoxPriorityProvider : ParadoxPriorityProvider {
    abstract fun getFilePathMap(gameType: ParadoxGameType): Map<String, ParadoxPriority>
    
    override fun getPriority(target: Any): ParadoxPriority? {
        val forcedDefinitionPriority = getForcedDefinitionPriority(target)
        if(forcedDefinitionPriority != null) return forcedDefinitionPriority
        
        val filePaths = mutableSetOf<String>()
        getFilePaths(target, filePaths)
        if(filePaths.isEmpty()) return null
        val gameType = selectGameType(target) ?: return null
        val filePathMap = getFilePathMap(gameType)
        return filePaths.firstNotNullOfOrNull { filePathMap[it] }
    }
    
    override fun getPriority(searchParameters: ParadoxSearchParameters<*>): ParadoxPriority? {
        val forcedDefinitionPriority = getForcedDefinitionPriority(searchParameters)
        if(forcedDefinitionPriority != null) return forcedDefinitionPriority
        
        val filePaths = mutableSetOf<String>()
        getFilePaths(searchParameters, filePaths)
        if(filePaths.isEmpty()) return null
        val gameType = searchParameters.selector.gameType ?: return null
        val filePathMap = getFilePathMap(gameType)
        return filePaths.firstNotNullOfOrNull { filePathMap[it] }
    }
    
    private fun getForcedDefinitionPriority(target: Any): ParadoxPriority? {
        if(target !is ParadoxScriptProperty) return null
        val definitionInfo = target.definitionInfo ?: return null
        val typeConfig = definitionInfo.typeConfig
        return doGetForcedDefinitionPriority(typeConfig)
    }
    
    private fun getForcedDefinitionPriority(searchParameters: ParadoxSearchParameters<*>): ParadoxPriority? {
        if(searchParameters !is ParadoxDefinitionSearch.SearchParameters) return null
        val definitionType = searchParameters.typeExpression?.substringBefore('.') ?: return null
        val gameType = searchParameters.selector.gameType ?: return null
        val configGroup = getConfigGroup(searchParameters.project, gameType)
        val typeConfig = configGroup.types.get(definitionType) ?: return null
        return doGetForcedDefinitionPriority(typeConfig)
    }
    
    private fun doGetForcedDefinitionPriority(typeConfig: CwtTypeConfig): ParadoxPriority? {
        //event namespace -> ORDERED (don't care)
        if(typeConfig.name == "event_namespace") return ParadoxPriority.ORDERED
        //swapped type -> ORDERED (don't care)
        if(typeConfig.baseType != null) return ParadoxPriority.ORDERED
        //anonymous -> ORDERED (don't care)
        if(typeConfig.typeKeyFilter != null && typeConfig.nameField == null) return ParadoxPriority.ORDERED
        return null
    }
    
    private fun getFilePaths(target: Any, filePaths: MutableSet<String>) {
        when {
            target is ParadoxScriptScriptedVariable -> {
                val targetPath = target.fileInfo?.path?.path ?: return
                filePaths += ("common/scripted_variables".takeIf { it.matchesPath(targetPath) } ?: return)
            }
            target is ParadoxScriptDefinitionElement -> {
                val definitionInfo = target.definitionInfo ?: return
                val definitionType = definitionInfo.type
                val configGroup = definitionInfo.configGroup
                val config = configGroup.types[definitionType] ?: return
                getFilePathsFromConfig(config, filePaths)
            }
            target is ParadoxComplexEnumValueElement -> {
                val enumName = target.enumName
                val configGroup = getConfigGroup(target.project, target.gameType)
                val config = configGroup.complexEnums[enumName] ?: return
                getFilePathsFromConfig(config, filePaths)
            }
            target is ParadoxLocalisationProperty -> {
                val localisationCategory = target.localisationInfo?.category ?: return
                val path = when(localisationCategory) {
                    ParadoxLocalisationCategory.Localisation -> "localisation"
                    ParadoxLocalisationCategory.SyncedLocalisation -> "localisation_synced"
                }
                filePaths += path
            }
        }
    }
    
    private fun getFilePaths(searchParameters: ParadoxSearchParameters<*>, filePaths: MutableSet<String>) {
        when {
            searchParameters is ParadoxGlobalScriptedVariableSearch.SearchParameters -> {
                filePaths += "common/scripted_variables"
            }
            searchParameters is ParadoxDefinitionSearch.SearchParameters -> {
                val definitionType = searchParameters.typeExpression?.substringBefore('.') ?: return
                val gameType = searchParameters.selector.gameType ?: return
                val configGroup = getConfigGroup(searchParameters.project, gameType)
                val config = configGroup.types.get(definitionType) ?: return
                getFilePathsFromConfig(config, filePaths)
            }
            searchParameters is ParadoxComplexEnumValueSearch.SearchParameters -> {
                val enumName = searchParameters.enumName
                val gameType = searchParameters.selector.gameType ?: return
                val configGroup = getConfigGroup(searchParameters.project, gameType)
                val config = configGroup.complexEnums.get(enumName) ?: return
                getFilePathsFromConfig(config, filePaths)
            }
            searchParameters is ParadoxLocalisationSearch.SearchParameters -> {
                filePaths += "localisation"
            }
            searchParameters is ParadoxSyncedLocalisationSearch.SearchParameters -> {
                filePaths += "localisation_synced"
            }
        }
    }
    
    private fun getFilePathsFromConfig(config: CwtTypeConfig, filePaths: MutableSet<String>) {
        val path = buildString {
            config.paths?.let {
                append(it)
            }
            config.pathFile?.let {
                if(isNotEmpty()) append("/")
                append(it)
            }
        }.orNull() ?: return
        filePaths += path
        val wildcardPath = config.paths ?: return
        filePaths += wildcardPath
    }
    
    private fun getFilePathsFromConfig(config: CwtComplexEnumConfig, filePaths: MutableSet<String>) {
        config.paths.forEach { p ->
            val path = buildString {
                append(p)
                config.pathFile?.let {
                    if(isNotEmpty()) append("/")
                    append(it)
                }
            }.orNull() ?: return
            filePaths += path
        }
        config.paths.forEach { p ->
            val wildcardPath = p
            filePaths += wildcardPath
        }
    }
}
