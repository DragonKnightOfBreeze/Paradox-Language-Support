package icu.windea.pls.lang.scope.impl

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.scope.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.scope.*
import icu.windea.pls.script.psi.*

class ParadoxBaseDefinitionInferredScopeContextProvider: ParadoxDefinitionInferredScopeContextProvider {
    companion object {
        val cachedScopeContextInferenceInfoKey = Key.create<CachedValue<ParadoxScopeContextInferenceInfo>>("paradox.cached.scopeContextInferenceInfo")
        
        val DEFINITION_TYPES = arrayOf("scripted_trigger", "scripted_effect", "static_modifier")
    }
    
    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContextInferenceInfo? {
        if(!getSettings().inference.scopeContext) return null
        if(definitionInfo.type !in DEFINITION_TYPES) return null
        return doGetScopeContextFromCache(definition)
    }
    
    private fun doGetScopeContextFromCache(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        return CachedValuesManager.getCachedValue(definition, cachedScopeContextInferenceInfoKey) {
            ProgressManager.checkCanceled()
            val value = doGetScopeContext(definition)
            val tracker0 = ParadoxPsiModificationTracker.DefinitionScopeContextInferenceTracker
            val tracker = ParadoxPsiModificationTracker.getInstance(definition.project).ScriptFileTracker
            CachedValueProvider.Result.create(value, tracker0, tracker)
        }
    }
    
    private fun doGetScopeContext(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        val definitionInfo = definition.definitionInfo ?: return null
        //optimize search scope
        val searchScope = runReadAction { ParadoxSearchScope.fromElement(definition) }
            ?: return null
        val configGroup = definitionInfo.configGroup
        val scopeContextMap = mutableMapOf<String, String?>()
        var hasConflict = false
        val r = doProcessQuery(definitionInfo, searchScope, scopeContextMap, configGroup)
        if(!r) hasConflict = true
        val resultScopeContextMap = scopeContextMap.takeIfNotEmpty() ?: return null
        return ParadoxScopeContextInferenceInfo(resultScopeContextMap, hasConflict)
    }
    
    private fun doProcessQuery(definitionInfo: ParadoxDefinitionInfo, searchScope: GlobalSearchScope, scopeContextMap: MutableMap<String, String?>, configGroup: CwtConfigGroup): Boolean {
        val gameType = configGroup.gameType ?: return true
        val project = configGroup.project
        return ParadoxDefinitionHierarchyHandler.processInferredScopeContextAwareDefinitions(gameType, searchScope) p@{ file, infos ->
            val psiFile by lazy { file.toPsiFile(project) }
            infos.forEachFast f@{ info ->
                val n = info.expression
                if(n != definitionInfo.name) return@p true //matches definition name
                val t = info.configExpression.value?.substringBefore('.')
                if(t != definitionInfo.type) return@p true //matches definition type
                val finalPsiFile = psiFile ?: return@p true
                val e = finalPsiFile.findElementAt(info.elementOffset) ?: return@p true
                val m = e.parentOfType<ParadoxScriptMemberElement>(withSelf = false) ?: return@p true
                val scopeContext = ParadoxScopeHandler.getScopeContext(m) ?: return@p true
                val map = with(scopeContext) {
                    //TODO 这里其实可以推断更加精确的作用域上下文
                    buildMap {
                        put("this", scope.id)
                        root?.let { put("root", it.scope.id) }
                        from?.let { put("from", it.scope.id) }
                        from?.from?.let { put("fromfrom", it.scope.id) }
                        from?.from?.from?.let { put("fromfromfrom", it.scope.id) }
                        from?.from?.from?.from?.let { put("fromfromfromfrom", it.scope.id) }
                    }
                }
                if(scopeContextMap.isEmpty()) {
                    val mergedMap = ParadoxScopeHandler.mergeScopeContextMap(scopeContextMap, map)
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
            true
        }
    }
    
    override fun getMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, info: ParadoxScopeContextInferenceInfo): String {
        return PlsBundle.message("script.annotator.scopeContext.0", definitionInfo.name)
    }
    
    override fun getErrorMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, info: ParadoxScopeContextInferenceInfo): String {
        return PlsBundle.message("script.annotator.scopeContext.0.conflict", definitionInfo.name)
    }
}
