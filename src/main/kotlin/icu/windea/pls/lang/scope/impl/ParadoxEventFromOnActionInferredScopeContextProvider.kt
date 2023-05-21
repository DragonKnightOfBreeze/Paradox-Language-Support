package icu.windea.pls.lang.scope.impl

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.hierarchy.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.scope.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.scope.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

/**
 * 如果某个`event`在某个`on_action`中被调用，
 * 则将此`on_action`的from, fromfrom...作用域推断为此`event`的from, fromfrom...作用域。
 */
class ParadoxEventFromOnActionInferredScopeContextProvider : ParadoxDefinitionInferredScopeContextProvider {
    companion object {
        val cachedScopeContextInferenceInfoKey = Key.create<CachedValue<ParadoxScopeContextInferenceInfo>>("paradox.cached.scopeContextInferenceInfo.fromOnAction")
    }
    
    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContextInferenceInfo? {
        if(!getSettings().inference.eventScopeContext) return null
        if(definitionInfo.type != "event") return null
        return doGetScopeContextFromCache(definition)
    }
    
    private fun doGetScopeContextFromCache(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        return CachedValuesManager.getCachedValue(definition, cachedScopeContextInferenceInfoKey) {
            ProgressManager.checkCanceled()
            val value = doGetScopeContext(definition)
            val tracker0 = ParadoxPsiModificationTracker.DefinitionScopeContextInferenceTracker
            val tracker = ParadoxPsiModificationTracker.getInstance(definition.project).ScriptFileTracker("common/on_actions:txt")
            CachedValueProvider.Result.create(value, tracker0, tracker)
        }
    }
    
    private fun doGetScopeContext(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        val definitionInfo = definition.definitionInfo ?: return null
        //optimize search scope
        val searchScope = runReadAction { ParadoxSearchScope.fromElement(definition) }
            ?.withFilePath("common/on_actions", "txt")
            ?: return null
        val configGroup = definitionInfo.configGroup
        val project = configGroup.project
        val thisEventName = definitionInfo.name
        val thisEventType = ParadoxEventHandler.getType(definitionInfo)
        var scopeContextMap: Map<String, String?>? = null
        var hasConflict = false
        ProgressManager.checkCanceled()
        val r = FileTypeIndex.processFiles(ParadoxScriptFileType, p@{ file ->
            ProgressManager.checkCanceled()
            val data = ParadoxOnActionHierarchyIndex.getData(file, project) ?: return@p true
            val onActionNames = data.eventToOnActionsMap[thisEventName] ?: return@p true
            val psiFile = file.toPsiFile(project) ?: return@p true
            if(psiFile !is ParadoxScriptFile) return@p true
            onActionNames.forEach { onActionName ->
                //这里使用psiFile作为contextElement
                val config = configGroup.onActions.getByTemplate(onActionName, psiFile, configGroup)
                if(config == null) return@p true //missing
                if(config.eventType != thisEventType) return@p true //invalid (mismatch)
                val map = config.config.replaceScopes ?: return@p true
                if(scopeContextMap != null) {
                    val mergedMap = ParadoxScopeHandler.mergeScopeContextMap(scopeContextMap!!, map)
                    if(mergedMap != null) {
                        scopeContextMap = mergedMap
                    } else {
                        return@p false
                    }
                } else {
                    scopeContextMap = map
                }
            }
            true
        }, searchScope)
        if(!r) hasConflict = true
        val resultScopeContextMap = scopeContextMap ?: return null
        return ParadoxScopeContextInferenceInfo(resultScopeContextMap, hasConflict)
    }
    
    override fun getMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, info: ParadoxScopeContextInferenceInfo): String {
        val eventId = definitionInfo.name
        return PlsBundle.message("script.annotator.scopeContext.1", eventId)
    }
    
    override fun getErrorMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, info: ParadoxScopeContextInferenceInfo): String {
        val eventId = definitionInfo.name
        return PlsBundle.message("script.annotator.scopeContext.1.conflict", eventId)
    }
}

