package icu.windea.pls.lang.scope.impl

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.index.hierarchy.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.scope.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.scope.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

private val cachedScopeContextInferenceInfoKey = Key.create<CachedValue<ParadoxScopeContextInferenceInfo>>("paradox.cached.scopeContextInferenceInfo.onAction.in.event")

/**
 * 如果某个on_action在一个event声明中被调用，
 * 则将此另一个event的root作用域推断为此event的from作用域，
 * 将调用此另一个event的event的root作用域推断为此event的fromfrom作用域，
 * 依此类推直到fromfromfromfrom作用域。
 * 如果有声明scopes = { from = ... }，则将此on_action的from作用域推断为这个声明中from对应的作用域，
 * 依此类推直到fromfromfromfrom作用域。
 */
class ParadoxOnActionInEventInferredScopeContextProvider : ParadoxDefinitionInferredScopeContextProvider {
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == "on_action"
    }
    
    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContextInferenceInfo? {
        if(!getSettings().inference.onActionScopeContext) return null
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
        ProgressManager.checkCanceled()
        val definitionInfo = definition.definitionInfo ?: return null
        val configGroup = definitionInfo.configGroup
        //skip if on action is predefined
        val config = configGroup.onActions.getByTemplate(definitionInfo.name, definition, configGroup)
        if(config != null) return null
        val thisOnActionName = definitionInfo.name
        //optimize search scope
        val searchScope = runReadAction { ParadoxSearchScope.fromElement(definition) }
            ?: return null
        val scopeContextMap = mutableMapOf<String, String?>()
        scopeContextMap.put("this", ParadoxScopeHandler.anyScopeId)
        scopeContextMap.put("root", ParadoxScopeHandler.anyScopeId)
        var hasConflict = false
        val r = doProcessQuery(thisOnActionName, searchScope, scopeContextMap, configGroup)
        if(!r) hasConflict = true
        val resultScopeContextMap = scopeContextMap.takeIf { it.size > 2 } ?: return null
        return ParadoxScopeContextInferenceInfo(resultScopeContextMap, hasConflict)
    }
    
    private fun doProcessQuery(
        thisOnActionName: String,
        searchScope: GlobalSearchScope,
        scopeContextMap: MutableMap<String, String?>,
        configGroup: CwtConfigGroup,
        depth: Int = 1
    ): Boolean {
        ProgressManager.checkCanceled()
        val project = configGroup.project
        val gameType = configGroup.gameType ?: return true
        return withRecursionGuard("icu.windea.pls.lang.scope.impl.ParadoxOnActionInEventInferredScopeContextProvider.doProcessQuery") {
            if(depth == 1) stackTrace.addLast(thisOnActionName)
            
            val toRef = "from".repeat(depth)
            val index = findIndex<ParadoxOnActionInEventDefinitionHierarchyIndex>()
            ParadoxDefinitionHierarchyHandler.processQuery(index, project, gameType, searchScope) p@{ file, fileData ->
                val infos = fileData.values.firstOrNull() ?: return@p true
                val psiFile = file.toPsiFile(project) ?: return@p true
                infos.forEachFast f@{ info ->
                    val onActionName = info.onActionName
                    if(onActionName != thisOnActionName) return@f
                    val containingEventName = info.containingEventName
                    withCheckRecursion(containingEventName) {
                        val scopesElementOffset = info.scopesElementOffset
                        if(scopesElementOffset != -1) {
                            //从scopes = { ... }中推断
                            ProgressManager.checkCanceled()
                            val scopesElement = psiFile.findElementAt(scopesElementOffset)?.parentOfType<ParadoxScriptProperty>() ?: return@p false
                            val scopesBlockElement = scopesElement.block ?: return@p false
                            val scopeContextOfScopesElement = ParadoxScopeHandler.getScopeContext(scopesElement)
                            val map = mutableMapOf<String, String?>()
                            scopesBlockElement.processProperty(inline = true) pp@{
                                ProgressManager.checkCanceled()
                                val n = it.name.lowercase()
                                if(configGroup.systemLinks.get(n)?.baseId?.lowercase() != "from") return@pp true
                                
                                if(scopeContextOfScopesElement == null) {
                                    map.put(n, ParadoxScopeHandler.anyScopeId)
                                    return@pp true
                                }
                                
                                val pv = it.propertyValue ?: return@pp true
                                val scopeField = pv.text
                                if(scopeField.isLeftQuoted()) return@pp true
                                val textRange = TextRange.create(0, scopeField.length)
                                val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(scopeField, textRange, configGroup) ?: return@pp true
                                val scopeContextOfEachScope = ParadoxScopeHandler.getScopeContext(scopeFieldExpression, scopeContextOfScopesElement)
                                map.put(n, scopeContextOfEachScope.scope.id)
                                
                                true
                            }
                            
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
                            
                            return@f
                        }
                        
                        val containingEventScope = info.containingEventScope
                        if(containingEventScope != null) {
                            val newRefScope = containingEventScope
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
                            if(depth >= 4) return@p true
                            doProcessQuery(containingEventName, searchScope, scopeContextMap, configGroup, depth + 1)
                        }
                    }
                }
                true
            }
        } ?: false
    }
    
    override fun getMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, info: ParadoxScopeContextInferenceInfo): String {
        return PlsBundle.message("script.annotator.scopeContext.3", definitionInfo.name)
    }
    
    override fun getErrorMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, info: ParadoxScopeContextInferenceInfo): String {
        return PlsBundle.message("script.annotator.scopeContext.3.conflict", definitionInfo.name)
    }
}