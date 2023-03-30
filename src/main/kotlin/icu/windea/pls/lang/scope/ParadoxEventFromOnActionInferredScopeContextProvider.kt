package icu.windea.pls.lang.scope

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.psi.search.searches.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.scopes.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.references.*

/**
 * 如果事件在某个`on_action`中被调用，则将其from作用域设为此`on_action`的from作用域，fromfrom作用域也这样处理，依此类推。
 */
class ParadoxEventFromOnActionInferredScopeContextProvider : ParadoxDefinitionInferredScopeContextProvider {
    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContextInferenceInfo? {
        if(!getSettings().inference.eventScopeContext) return null
        ProgressManager.checkCanceled()
        if(definitionInfo.type != "event") return null
        return getInferredScopeContext(definition)
    }
    
    private fun getInferredScopeContext(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        return CachedValuesManager.getCachedValue(definition, PlsKeys.cachedScopeContextInferenceInfoKey) {
            val value = resolveInferredScopeContext(definition)
            val tracker = ParadoxModificationTrackerProvider.getInstance().OnActions
            CachedValueProvider.Result.create(value, tracker)
        }
    }
    
    private fun resolveInferredScopeContext(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        ProgressManager.checkCanceled()
        val definitionInfo = definition.definitionInfo ?: return null
        val configGroup = definitionInfo.configGroup
        var scopeContext: ParadoxScopeContext? = null
        var hasConflict = false
        //optimize search scope
        val searchScope = runReadAction { ParadoxGlobalSearchScope.fromElement(definition) }
            ?.withFilePath("common/on_actions", "txt")
            ?: return null
        ReferencesSearch.search(definition, searchScope).processQuery p@{ ref ->
            ProgressManager.checkCanceled()
            //should be
            if(ref !is ParadoxScriptExpressionPsiReference) return@p true
            val refDefinition = ref.element.findParentDefinition() ?: return@p true
            val refDefinitionInfo = refDefinition.definitionInfo ?: return@p true
            when {
                refDefinitionInfo.type == "on_action" -> {
                    //check on_action is valid and event type is valid
                    val config = configGroup.onActions.getByTemplate(refDefinition.name, definition, configGroup)
                        ?: return@p true //missing
                    if(!definitionInfo.subtypes.contains(config.eventType)) return@p true //invalid
                    val sc = config.scopeContext ?: return@p true
                    if(scopeContext != null && sc != scopeContext) {
                        hasConflict = true
                        return@p false
                    }
                    val inferred = sc.copy()
                    inferred.from = inferred.from?.copyAsInferred()
                    scopeContext = inferred
                }
            }
            true
        }
        return ParadoxScopeContextInferenceInfo(scopeContext ?: return null, hasConflict)
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