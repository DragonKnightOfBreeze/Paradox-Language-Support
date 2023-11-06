package icu.windea.pls.lang.priority

import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

abstract class ParadoxFilePathBasedPriorityProvider : ParadoxPriorityProvider {
    open val fiosPaths: List<String> = emptyList()
    
    open val orderedPaths: List<String> = emptyList()
    
    override fun getPriority(target: Any): ParadoxPriority? {
        val forcedDefinitionPriority = getForcedDefinitionPriority(target)
        if(forcedDefinitionPriority != null) return forcedDefinitionPriority
        
        val filePath = getFilePath(target) ?: return null
        return when {
            filePath in fiosPaths -> ParadoxPriority.FIOS
            filePath in orderedPaths -> ParadoxPriority.ORDERED
            else -> null
        }
    }
    
    override fun getPriority(searchParameters: ParadoxSearchParameters<*>): ParadoxPriority? {
        val forcedDefinitionPriority = getForcedDefinitionPriority(searchParameters)
        if(forcedDefinitionPriority != null) return forcedDefinitionPriority
        
        val filePath = getFilePath(searchParameters) ?: return null
        return when {
            filePath in fiosPaths -> ParadoxPriority.FIOS
            filePath in orderedPaths -> ParadoxPriority.ORDERED
            else -> null
        }
    }
    
    private fun getFilePath(target: Any): String? {
        return when {
            target is VirtualFile -> null //ignore
            target is PsiFileSystemItem -> null //ignore
            target is ParadoxScriptScriptedVariable -> {
                val targetPath = target.fileInfo?.pathToEntry?.path ?: return null
                "common/scripted_variables".takeIf { it.matchesPath(targetPath) }
            }
            target is ParadoxScriptDefinitionElement -> {
                val definitionInfo = target.definitionInfo ?: return null
                val definitionType = definitionInfo.type
                val configGroup = definitionInfo.configGroup
                configGroup.types.get(definitionType)?.path
            }
            target is ParadoxLocalisationProperty -> {
                val localisationCategory = target.localisationInfo?.category ?: return null
                return when(localisationCategory) {
                    ParadoxLocalisationCategory.Localisation -> "localisation"
                    ParadoxLocalisationCategory.SyncedLocalisation -> "localisation_synced"
                }
            }
            else -> null
        }
    }
    
    private fun getFilePath(searchParameters: ParadoxSearchParameters<*>): String? {
        return when {
            searchParameters is ParadoxFilePathSearch.SearchParameters -> null //ignore
            searchParameters is ParadoxGlobalScriptedVariableSearch.SearchParameters -> {
                "common/scripted_variables"
            }
            searchParameters is ParadoxDefinitionSearch.SearchParameters -> {
                val definitionType = searchParameters.typeExpression?.substringBefore('.') ?: return null
                val gameType = searchParameters.selector.gameType ?: return null
                val configGroup = getConfigGroup(searchParameters.project, gameType)
                val typeConfig = configGroup.types.get(definitionType) ?: return null
                typeConfig.path
            }
            searchParameters is ParadoxLocalisationSearch.SearchParameters -> {
                "localisation"
            }
            searchParameters is ParadoxSyncedLocalisationSearch.SearchParameters -> {
                "localisation_synced"
            }
            else -> null
        }
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
}
