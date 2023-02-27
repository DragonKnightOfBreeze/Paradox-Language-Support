package icu.windea.pls.lang.provider

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.references.*

/**
 * * 如果事件在某个`on_action`中被调用，则将其from作用域设为此`on_action`的from作用域，fromfrom作用域也这样处理，依此类推。
 * * 如果事件在某个事件中被调用，则将其from作用域设为此事件的root作用域，将其fromfrom作用域设为调用此事件的那个事件的root作用域，依此类推。
 */
class ParadoxEventInferredScopeContextProvider: ParadoxInferredScopeContextProvider {
    override val type: ParadoxInferredScopeContextProvider.Type = ParadoxInferredScopeContextProvider.Type.Definition
    
    override fun infer(contextElement: PsiElement, rawScopeContext: ParadoxScopeContext): Set<ParadoxScopeContext> {
        ProgressManager.checkCanceled()
        if(contextElement !is ParadoxScriptProperty) return emptySet()
        val definitionInfo = contextElement.definitionInfo ?: return emptySet()
        if(definitionInfo.type != "event") return emptySet()
        val inferred = getInferredScopeContext(contextElement)
        return inferred.mapTo(mutableSetOf()) { scopeContext ->
            if(scopeContext.scope.id == ParadoxScopeHandler.unknownScopeId) {
                //event
                rawScopeContext.from = scopeContext.from //event's root scope
                rawScopeContext
            } else {
                //on_action
                scopeContext
            }
        }
    }
    
    private fun getInferredScopeContext(definition: ParadoxScriptDefinitionElement): Set<ParadoxScopeContext> {
        return CachedValuesManager.getCachedValue(definition, PlsKeys.cachedInferredScopeContextsKey) {
            val value = resolveInferredScopeContext(definition)
            val tracker = ParadoxModificationTrackerProvider.getInstance().ScriptFile
            CachedValueProvider.Result.create(value, tracker)
        }
    }
    
    private fun resolveInferredScopeContext(definition: ParadoxScriptDefinitionElement): Set<ParadoxScopeContext> {
        val definitionInfo = definition.definitionInfo ?: return emptySet()
        val configGroup = definitionInfo.configGroup
        val result = mutableSetOf<ParadoxScopeContext>()
        ReferencesSearch.search(definition).processQuery p@{ ref ->
            //should be
            if(ref !is ParadoxScriptExpressionPsiReference) return@p true
            val refDefinition = ref.element.findParentDefinition() ?: return@p true
            val refDefinitionInfo = refDefinition.definitionInfo ?: return@p true
            when {
                refDefinitionInfo.type == "event" -> {
                    if(refDefinition.name == definitionInfo.name) {
                        //self event invocation, ignore
                        return@p true
                    }
                    val sc = ParadoxScopeHandler.getScopeContext(refDefinition) ?: return@p true
                    result.add(sc)
                }
                refDefinitionInfo.type == "on_action" -> {
                    //check on_action is valid and event type is valid
                    val onActionInfo = configGroup.onActions[refDefinitionInfo.name] ?: return@p true //missing
                    if(!definitionInfo.subtypes.contains(onActionInfo.event)) return@p true //invalid
                    val sc = onActionInfo.scopeContext ?: return@p true
                    result.add(sc)
                }
            }
            true
        }
        return result
    }
}