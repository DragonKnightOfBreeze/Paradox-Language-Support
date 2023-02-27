package icu.windea.pls.lang.provider

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.scopes.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.references.*

/**
 * 如果事件在某个`on_action`中被调用，则将其from作用域设为此`on_action`的from作用域，fromfrom作用域也这样处理，依此类推。
 */
class ParadoxEventFromOnActionInferredScopeContextProvider: ParadoxInferredScopeContextProvider {
    override val type: ParadoxInferredScopeContextProvider.Type = ParadoxInferredScopeContextProvider.Type.Definition
    
    override fun infer(contextElement: PsiElement): ParadoxScopeContextInferenceInfo? {
        ProgressManager.checkCanceled()
        if(contextElement !is ParadoxScriptProperty) return null
        val definitionInfo = contextElement.definitionInfo ?: return null
        if(definitionInfo.type != "event") return null
        return getInferredScopeContext(contextElement)
    }
    
    private fun getInferredScopeContext(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        return CachedValuesManager.getCachedValue(definition, PlsKeys.cachedScopeContextInferenceInfoKey) {
            val value = resolveInferredScopeContext(definition)
            val tracker = ParadoxModificationTrackerProvider.getInstance().ScriptFile
            CachedValueProvider.Result.create(value, tracker)
        }
    }
    
    private fun resolveInferredScopeContext(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        val definitionInfo = definition.definitionInfo ?: return null
        val configGroup = definitionInfo.configGroup
        val project = configGroup.project
        var scopeContext: ParadoxScopeContext? = null
        var hasConflict = false
        //optimize search scope
        val searchScope = GlobalSearchScope.allScope(project)
            .withFilePath("common/on_actions", "txt")
        ReferencesSearch.search(definition, searchScope).processQuery p@{ ref ->
            ProgressManager.checkCanceled()
            //should be
            if(ref !is ParadoxScriptExpressionPsiReference) return@p true
            val refDefinition = ref.element.findParentDefinition() ?: return@p true
            val refDefinitionInfo = refDefinition.definitionInfo ?: return@p true
            when {
                refDefinitionInfo.type == "on_action" -> {
                    //check on_action is valid and event type is valid
                    val onActionInfo = configGroup.onActions[refDefinitionInfo.name] ?: return@p true //missing
                    if(!definitionInfo.subtypes.contains(onActionInfo.event)) return@p true //invalid
                    val sc = onActionInfo.scopeContext ?: return@p true
                    if(scopeContext != null && sc != scopeContext) {
                        hasConflict = true
                        return@p false
                    }
                    scopeContext = sc
                }
            }
            true
        }
        return ParadoxScopeContextInferenceInfo(scopeContext ?: return null, hasConflict)
    }
}