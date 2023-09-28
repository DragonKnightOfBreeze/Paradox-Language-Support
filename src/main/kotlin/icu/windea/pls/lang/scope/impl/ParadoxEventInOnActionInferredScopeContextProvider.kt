package icu.windea.pls.lang.scope.impl

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.scope.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expressionIndex.*
import icu.windea.pls.lang.scope.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

private val cachedScopeContextInferenceInfoKey = createKey<CachedValue<ParadoxScopeContextInferenceInfo>>("paradox.cached.scopeContextInferenceInfo.event.in.onAction")

/**
 * 如果某个event在某个on_action中被调用，
 * 则将此on_action的from, fromfrom...作用域推断为此event的from, fromfrom...作用域。
 */
class ParadoxEventInOnActionInferredScopeContextProvider : ParadoxDefinitionInferredScopeContextProvider {
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == "event"
    }
    
    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContextInferenceInfo? {
        if(!getSettings().inference.eventScopeContext) return null
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
        ProgressManager.checkCanceled()
        val definitionInfo = definition.definitionInfo ?: return null
        val configGroup = definitionInfo.configGroup
        val thisEventName = definitionInfo.name
        val thisEventType = ParadoxEventHandler.getType(definitionInfo)
        //optimize search scope
        val searchScope = runReadAction { ParadoxSearchScope.fromElement(definition) }
            ?.withFilePath("common/on_actions", "txt")
            ?: return null
        val scopeContextMap = mutableMapOf<String, String?>()
        var hasConflict = false
        val r = doProcessQuery(thisEventName, thisEventType, searchScope, scopeContextMap, configGroup)
        if(!r) hasConflict = true
        val resultScopeContextMap = scopeContextMap.orNull() ?: return null
        return ParadoxScopeContextInferenceInfo(resultScopeContextMap, hasConflict)
    }
    
    private fun doProcessQuery(
        thisEventName: String,
        thisEventType: String?,
        searchScope: GlobalSearchScope,
        scopeContextMap: MutableMap<String, String?>,
        configGroup: CwtConfigGroup,
        depth: Int = 1
    ): Boolean {
        ProgressManager.checkCanceled()
        val project = configGroup.project
        val gameType = configGroup.gameType ?: return true
        return withRecursionGuard("icu.windea.pls.lang.scope.impl.ParadoxEventInEventInferredScopeContextProvider.doProcessQuery") {
            if(depth == 1) stackTrace.addLast(thisEventName)
            
            val indexId = ParadoxExpressionIndexId.EventInOnAction
            ParadoxExpressionIndexHandler.processQuery(indexId, project, gameType, searchScope) p@{ file, infos ->
                val psiFile = file.toPsiFile(project) ?: return@p true
                infos.forEachFast f@{ info ->
                    val eventName = info.eventName
                    if(eventName != thisEventName) return@f
                    val containingOnActionName = info.containingOnActionName
                    withCheckRecursion(containingOnActionName) {
                        //这里使用psiFile作为contextElement
                        val config = configGroup.onActions.getByTemplate(containingOnActionName, psiFile, configGroup)
                        if(config == null) return@f //missing
                        if(config.eventType != thisEventType) return@f //invalid (mismatch)
                        val map = config.config.replaceScopes ?: return@f
                        if(scopeContextMap.isNotEmpty()) {
                            val mergedMap = ParadoxScopeHandler.mergeScopeContextMap(scopeContextMap, map, true)
                            if(mergedMap != null) {
                                scopeContextMap.clear()
                                scopeContextMap.putAll(mergedMap)
                            } else {
                                return@p false
                            }
                        } else {
                            scopeContextMap.putAll(map)
                        }
                    }
                }
                true
            }
        } ?: false
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

