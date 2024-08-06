package icu.windea.pls.ep.scope

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.index.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.lang.search.scope.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.script.psi.*

/**
 * 推断scripted_trigger、scripted_effect等的作用域上下文（仅限this和root）。
 */
class ParadoxBaseDefinitionInferredScopeContextProvider : ParadoxDefinitionInferredScopeContextProvider {
    object Data {
        val cachedScopeContextInferenceInfoKey = createKey<CachedValue<ParadoxScopeContextInferenceInfo>>("paradox.cached.scopeContextInferenceInfo")
        
        val DEFINITION_TYPES = arrayOf("scripted_trigger", "scripted_effect")
    }
    
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type in Data.DEFINITION_TYPES
    }
    
    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContextInferenceInfo? {
        if(!getSettings().inference.scopeContext) return null
        return doGetScopeContextFromCache(definition)
    }
    
    private fun doGetScopeContextFromCache(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        return CachedValuesManager.getCachedValue(definition, Data.cachedScopeContextInferenceInfoKey) {
            ProgressManager.checkCanceled()
            val value = doGetScopeContext(definition)
            val tracker0 = ParadoxModificationTrackers.DefinitionScopeContextInferenceTracker
            val tracker = getTracker(definition)
            CachedValueProvider.Result.create(value, tracker0, tracker)
        }
    }
    
    private fun getTracker(definition: ParadoxScriptDefinitionElement): ModificationTracker {
        val configGroup = definition.definitionInfo?.configGroup
            ?: return ParadoxModificationTrackers.ScriptFileTracker
        return configGroup.definitionScopeContextModificationTracker
    }
    
    private fun doGetScopeContext(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        val definitionInfo = definition.definitionInfo ?: return null
        
        //optimize search scope
        val searchScope = runReadAction { ParadoxSearchScope.fromElement(definition) }
            ?: return null
        val configGroup = definitionInfo.configGroup
        val scopeContextMap = mutableMapOf<String, String>()
        var hasConflict = false
        val r = doProcessQuery(definitionInfo, searchScope, scopeContextMap, configGroup)
        if(!r) hasConflict = true
        val resultScopeContextMap = scopeContextMap.orNull() ?: return null
        return ParadoxScopeContextInferenceInfo(resultScopeContextMap, hasConflict)
    }
    
    private fun doProcessQuery(
        definitionInfo: ParadoxDefinitionInfo,
        searchScope: GlobalSearchScope,
        scopeContextMap: MutableMap<String, String>,
        configGroup: CwtConfigGroup
    ): Boolean {
        ProgressManager.checkCanceled()
        val project = configGroup.project
        val gameType = configGroup.gameType ?: return true
        return withRecursionGuard("icu.windea.pls.lang.scope.ParadoxBaseDefinitionInferredScopeContextProvider.doProcessQuery") {
            withCheckRecursion(definitionInfo.name + "@" + definitionInfo.type) {
                val indexId = ParadoxExpressionIndexId.InferredScopeContextAwareDefinition
                ParadoxExpressionIndex.processQuery(indexId, project, gameType, searchScope) p@{ file, infos ->
                    val psiFile = file.toPsiFile(project) ?: return@p true
                    infos.forEach f@{ info ->
                        //TODO 1.0.6+ 这里对应的引用可能属于某个复杂表达式的一部分（目前不需要考虑兼容这种情况）
                        val definitionName = info.definitionName
                        if(definitionName != definitionInfo.name) return@f //matches definition name
                        val eventType = info.typeExpression.substringBefore('.')
                        if(eventType != definitionInfo.type) return@f //matches definition type
                        val e = psiFile.findElementAt(info.elementOffset) ?: return@f
                        val m = e.parentOfType<ParadoxScriptMemberElement>(withSelf = false) ?: return@f
                        val scopeContext = ParadoxScopeHandler.getSwitchedScopeContext(m) ?: return@f
                        val map = with(scopeContext) {
                            buildMap {
                                put("this", scope.id)
                                root?.let { put("root", it.scope.id) }
                            }
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
                    }
                    true
                }
            }
        } ?: false
    }
    
    override fun getMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, info: ParadoxScopeContextInferenceInfo): String {
        return PlsBundle.message("script.annotator.scopeContext.0", definitionInfo.name)
    }
    
    override fun getErrorMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, info: ParadoxScopeContextInferenceInfo): String {
        return PlsBundle.message("script.annotator.scopeContext.0.conflict", definitionInfo.name)
    }
}

/**
 * 如果某个event在某个on_action中被调用，
 * 则将此on_action的from, fromfrom...作用域推断为此event的from, fromfrom...作用域。
 */
class ParadoxEventInOnActionInferredScopeContextProvider : ParadoxDefinitionInferredScopeContextProvider {
    object Data {
        val cachedScopeContextInferenceInfoKey = createKey<CachedValue<ParadoxScopeContextInferenceInfo>>("paradox.cached.scopeContextInferenceInfo.event.in.onAction")
    }
    
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == "event"
    }
    
    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContextInferenceInfo? {
        if(!getSettings().inference.scopeContextForEvents) return null
        return doGetScopeContextFromCache(definition)
    }
    
    private fun doGetScopeContextFromCache(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        return CachedValuesManager.getCachedValue(definition, Data.cachedScopeContextInferenceInfoKey) {
            ProgressManager.checkCanceled()
            val value = doGetScopeContext(definition)
            val tracker0 = ParadoxModificationTrackers.DefinitionScopeContextInferenceTracker
            val tracker = ParadoxModificationTrackers.ScriptFileTracker("common/on_actions/**/*.txt")
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
        val scopeContextMap = mutableMapOf<String, String>()
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
        scopeContextMap: MutableMap<String, String>,
        configGroup: CwtConfigGroup,
        depth: Int = 1
    ): Boolean {
        ProgressManager.checkCanceled()
        val project = configGroup.project
        val gameType = configGroup.gameType ?: return true
        return withRecursionGuard("icu.windea.pls.lang.scope.ParadoxEventInEventInferredScopeContextProvider.doProcessQuery") {
            if(depth == 1) stackTrace.addLast(thisEventName)
            
            val indexId = ParadoxExpressionIndexId.EventInOnAction
            ParadoxExpressionIndex.processQuery(indexId, project, gameType, searchScope) p@{ file, infos ->
                val psiFile = file.toPsiFile(project) ?: return@p true
                infos.forEach f@{ info ->
                    val eventName = info.eventName
                    if(eventName != thisEventName) return@f
                    val containingOnActionName = info.containingOnActionName
                    withCheckRecursion(containingOnActionName) {
                        //这里使用psiFile作为contextElement
                        val config = configGroup.extendedOnActions.findFromPattern(containingOnActionName, psiFile, configGroup)
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

/**
 * 如果某个event在一个event声明中被调用，
 * 则将此另一个event的root作用域推断为此event的from作用域，
 * 将调用此另一个event的event的root作用域推断为此event的fromfrom作用域，
 * 依此类推直到fromfromfromfrom作用域。
 * 如果有声明scopes = { from = ... }，则将此event的from作用域推断为这个声明中from对应的作用域，
 * 依此类推直到fromfromfromfrom作用域。
 */
class ParadoxEventInEventInferredScopeContextProvider : ParadoxDefinitionInferredScopeContextProvider {
    object Data {
        val cachedScopeContextInferenceInfoKey = createKey<CachedValue<ParadoxScopeContextInferenceInfo>>("paradox.cached.scopeContextInferenceInfo.event.in.event")
    }
    
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == "event"
    }
    
    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContextInferenceInfo? {
        if(!getSettings().inference.scopeContextForEvents) return null
        return doGetScopeContextFromCache(definition)
    }
    
    private fun doGetScopeContextFromCache(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        return CachedValuesManager.getCachedValue(definition, Data.cachedScopeContextInferenceInfoKey) {
            ProgressManager.checkCanceled()
            val value = doGetScopeContext(definition)
            val tracker0 = ParadoxModificationTrackers.DefinitionScopeContextInferenceTracker
            val tracker = ParadoxModificationTrackers.ScriptFileTracker("events/**/*.txt")
            CachedValueProvider.Result.create(value, tracker0, tracker)
        }
    }
    
    private fun doGetScopeContext(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        val definitionInfo = definition.definitionInfo ?: return null
        val configGroup = definitionInfo.configGroup
        val thisEventName = definitionInfo.name
        val thisEventScope = ParadoxEventHandler.getScope(definitionInfo) ?: return null
        //optimize search scope
        val searchScope = runReadAction { ParadoxSearchScope.fromElement(definition) }
            ?: return null
        val scopeContextMap = mutableMapOf<String, String>()
        scopeContextMap.put("this", thisEventScope)
        scopeContextMap.put("root", thisEventScope)
        var hasConflict = false
        val r = doProcessQuery(thisEventName, searchScope, scopeContextMap, configGroup)
        if(!r) hasConflict = true
        val resultScopeContextMap = scopeContextMap.takeIf { it.size > 2 } ?: return null
        return ParadoxScopeContextInferenceInfo(resultScopeContextMap, hasConflict)
    }
    
    private fun doProcessQuery(
        thisEventName: String,
        searchScope: GlobalSearchScope,
        scopeContextMap: MutableMap<String, String>,
        configGroup: CwtConfigGroup,
        depth: Int = 1
    ): Boolean {
        ProgressManager.checkCanceled()
        val project = configGroup.project
        val gameType = configGroup.gameType ?: return true
        return withRecursionGuard("icu.windea.pls.lang.scope.ParadoxEventInEventInferredScopeContextProvider.doProcessQuery") {
            if(depth == 1) stackTrace.addLast(thisEventName)
            
            val toRef = "from".repeat(depth)
            val indexId = ParadoxExpressionIndexId.EventInEvent
            ParadoxExpressionIndex.processQuery(indexId, project, gameType, searchScope) p@{ file, infos ->
                val psiFile = file.toPsiFile(project) ?: return@p true
                infos.forEach f@{ info ->
                    val eventName = info.eventName
                    if(eventName != thisEventName) return@f
                    val containingEventName = info.containingEventName
                    withCheckRecursion(containingEventName) {
                        val scopesElementOffset = info.scopesElementOffset
                        if(scopesElementOffset != -1) {
                            //从scopes = { ... }中推断
                            ProgressManager.checkCanceled()
                            val scopesElement = psiFile.findElementAt(scopesElementOffset)?.parentOfType<ParadoxScriptProperty>() ?: return@p false
                            val scopesBlockElement = scopesElement.block ?: return@p false
                            val scopeContextOfScopesElement = ParadoxScopeHandler.getSwitchedScopeContext(scopesElement)
                            val map = mutableMapOf<String, String>()
                            scopesBlockElement.processProperty(inline = true) pp@{
                                ProgressManager.checkCanceled()
                                val n = it.name.lowercase()
                                if(configGroup.systemScopes.get(n)?.baseId?.lowercase() != "from") return@pp true
                                
                                if(scopeContextOfScopesElement == null) {
                                    map.put(n, ParadoxScopeHandler.anyScopeId)
                                    return@pp true
                                }
                                
                                val pv = it.propertyValue ?: return@pp true
                                val expressionString = pv.value
                                val textRange = TextRange.create(0, expressionString.length)
                                val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expressionString, textRange, configGroup) ?: return@pp true
                                val scopeContextOfEachScope = ParadoxScopeHandler.getSwitchedScopeContext(pv, scopeFieldExpression, scopeContextOfScopesElement)
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
        return PlsBundle.message("script.annotator.scopeContext.2", definitionInfo.name)
    }
    
    override fun getErrorMessage(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, info: ParadoxScopeContextInferenceInfo): String {
        return PlsBundle.message("script.annotator.scopeContext.2.conflict", definitionInfo.name)
    }
}

/**
 * 如果某个on_action在一个event声明中被调用，
 * 则将此另一个event的root作用域推断为此event的from作用域，
 * 将调用此另一个event的event的root作用域推断为此event的fromfrom作用域，
 * 依此类推直到fromfromfromfrom作用域。
 * 如果有声明scopes = { from = ... }，则将此on_action的from作用域推断为这个声明中from对应的作用域，
 * 依此类推直到fromfromfromfrom作用域。
 */
class ParadoxOnActionInEventInferredScopeContextProvider : ParadoxDefinitionInferredScopeContextProvider {
    object Data {
        val cachedScopeContextInferenceInfoKey = createKey<CachedValue<ParadoxScopeContextInferenceInfo>>("paradox.cached.scopeContextInferenceInfo.onAction.in.event")
    }
    
    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == "on_action"
    }
    
    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContextInferenceInfo? {
        if(!getSettings().inference.scopeContextForOnActions) return null
        return doGetScopeContextFromCache(definition)
    }
    
    private fun doGetScopeContextFromCache(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        return CachedValuesManager.getCachedValue(definition, Data.cachedScopeContextInferenceInfoKey) {
            ProgressManager.checkCanceled()
            val value = doGetScopeContext(definition)
            val tracker1 = ParadoxModificationTrackers.DefinitionScopeContextInferenceTracker
            val tracker2 = ParadoxModificationTrackers.ScriptFileTracker
            CachedValueProvider.Result.create(value, tracker1, tracker2)
        }
    }
    
    private fun doGetScopeContext(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        ProgressManager.checkCanceled()
        val definitionInfo = definition.definitionInfo ?: return null
        val configGroup = definitionInfo.configGroup
        //skip if on action is predefined
        val config = configGroup.extendedOnActions.findFromPattern(definitionInfo.name, definition, configGroup)
        if(config != null) return null
        val thisOnActionName = definitionInfo.name
        //optimize search scope
        val searchScope = runReadAction { ParadoxSearchScope.fromElement(definition) }
            ?: return null
        val scopeContextMap = mutableMapOf<String, String>()
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
        scopeContextMap: MutableMap<String, String>,
        configGroup: CwtConfigGroup,
        depth: Int = 1
    ): Boolean {
        ProgressManager.checkCanceled()
        val project = configGroup.project
        val gameType = configGroup.gameType ?: return true
        return withRecursionGuard("icu.windea.pls.lang.scope.ParadoxOnActionInEventInferredScopeContextProvider.doProcessQuery") {
            if(depth == 1) stackTrace.addLast(thisOnActionName)
            
            val toRef = "from".repeat(depth)
            val indexId = ParadoxExpressionIndexId.OnActionInEvent
            ParadoxExpressionIndex.processQuery(indexId, project, gameType, searchScope) p@{ file, infos ->
                val psiFile = file.toPsiFile(project) ?: return@p true
                infos.forEach f@{ info ->
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
                            val scopeContextOfScopesElement = ParadoxScopeHandler.getSwitchedScopeContext(scopesElement)
                            val map = mutableMapOf<String, String>()
                            scopesBlockElement.processProperty(inline = true) pp@{
                                ProgressManager.checkCanceled()
                                val n = it.name.lowercase()
                                if(configGroup.systemScopes.get(n)?.baseId?.lowercase() != "from") return@pp true
                                
                                if(scopeContextOfScopesElement == null) {
                                    map.put(n, ParadoxScopeHandler.anyScopeId)
                                    return@pp true
                                }
                                
                                val pv = it.propertyValue ?: return@pp true
                                val expressionString = pv.value
                                val textRange = TextRange.create(0, expressionString.length)
                                val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expressionString, textRange, configGroup) ?: return@pp true
                                val scopeContextOfEachScope = ParadoxScopeHandler.getSwitchedScopeContext(pv, scopeFieldExpression, scopeContextOfScopesElement)
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
