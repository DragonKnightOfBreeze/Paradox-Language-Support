package icu.windea.pls.lang.scope.impl

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.scope.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.scope.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.references.*

/**
 * 如果某个`event`在另一个`event`中被调用，
 * 则将此另一个`event`的root作用域推断为此`event`的from作用域，
 * 此调用此另一个`event`的`event`的root作用域推断为此`event`的fromfrom作用域，依此类推直到fromfromfromfrom作用域。
 */
class ParadoxEventFromEventInferredScopeContextProvider : ParadoxDefinitionInferredScopeContextProvider {
    companion object {
        val cachedScopeContextInferenceInfoKey = Key.create<CachedValue<ParadoxScopeContextInferenceInfo>>("paradox.cached.scopeContextInferenceInfo.fromEvent")
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
            val tracker = ParadoxPsiModificationTracker.getInstance(definition.project).ScriptFileTracker("common/events:txt")
            CachedValueProvider.Result.create(value, tracker0, tracker)
        }
    }
    
    private fun doGetScopeContext(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        val definitionInfo = definition.definitionInfo ?: return null
        val scopeContextMap = mutableMapOf<String, String?>()
        val scope = ParadoxEventHandler.getScope(definitionInfo)
        scopeContextMap.put("this", scope)
        scopeContextMap.put("root", scope)
        var hasConflict = false
        //optimize search scope
        val searchScope = runReadAction { ParadoxSearchScope.fromElement(definition) }
            ?.withFilePath("events", "txt")
            ?: return null
        ProgressManager.getInstance().runProcess({
            val result = doProcessQuery(definition, definitionInfo, searchScope, scopeContextMap)
            if(!result) hasConflict = true
        }, EmptyProgressIndicator())
        val resultScopeContextMap = scopeContextMap.takeIf { it.size > 2 } ?: return null
        return ParadoxScopeContextInferenceInfo(resultScopeContextMap, hasConflict)
    }
    
    private fun doProcessQuery(
        definition: ParadoxScriptDefinitionElement,
        definitionInfo: ParadoxDefinitionInfo,
        searchScope: GlobalSearchScope,
        scopeContextMap: MutableMap<String, String?>,
        depth: Int = 1
    ): Boolean {
        return withRecursionGuard("icu.windea.pls.lang.scope.impl.ParadoxEventFromEventInferredScopeContextProvider.doProcessQuery") {
            if(depth == 1) stackTrace.addLast(definitionInfo.name) 
            
            val toRef = "from".repeat(depth)
            ReferencesSearch.search(definition, searchScope).processQueryAsync p@{ ref ->
                ProgressManager.checkCanceled()
                //should be
                if(ref !is ParadoxScriptExpressionPsiReference) return@p true
                val refDefinition = ref.element.findParentDefinition() ?: return@p true
                val refDefinitionInfo = refDefinition.definitionInfo ?: return@p true
                if(refDefinitionInfo.type != "event") return@p true
                withCheckRecursion(refDefinitionInfo.name) {
                    val newRefScope = ParadoxEventHandler.getScope(refDefinitionInfo)
                    val oldRefScope = scopeContextMap.get(toRef)
                    if(oldRefScope == null) {
                        scopeContextMap.put(toRef, newRefScope)
                    } else {
                        val refScope = ParadoxScopeHandler.mergeScopeId(oldRefScope, newRefScope)
                        if(refScope == null) {
                            return@p false
                        }
                        scopeContextMap.put(toRef, refScope)
                    }
                    doProcessQuery(refDefinition, refDefinitionInfo, searchScope, scopeContextMap, depth + 1)
                } ?: false
            }
        } ?: false
    }
    
    private fun getEventScope(definitionInfo: ParadoxDefinitionInfo): String {
        return definitionInfo.subtypeConfigs.firstNotNullOfOrNull { it.pushScope } ?: ParadoxScopeHandler.anyScopeId
    }
    
    override fun getMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, info: ParadoxScopeContextInferenceInfo): String {
        val eventId = definitionInfo.name
        return PlsBundle.message("script.annotator.scopeContext.2", eventId)
    }
    
    override fun getErrorMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, info: ParadoxScopeContextInferenceInfo): String {
        val eventId = definitionInfo.name
        return PlsBundle.message("script.annotator.scopeContext.2.conflict", eventId)
    }
}