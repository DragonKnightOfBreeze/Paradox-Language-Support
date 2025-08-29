package icu.windea.pls.ep.scope

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.ModificationTracker
import com.intellij.openapi.util.TextRange
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.parentOfType
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.replaceScopes
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.definitionScopeContextModificationTracker
import icu.windea.pls.config.configGroup.extendedOnActions
import icu.windea.pls.config.configGroup.systemScopes
import icu.windea.pls.config.findFromPattern
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.expression.complex.ParadoxScopeFieldExpression
import icu.windea.pls.lang.index.ParadoxIndexInfoType
import icu.windea.pls.lang.search.scope.ParadoxSearchScope
import icu.windea.pls.lang.search.scope.withFilePath
import icu.windea.pls.lang.util.ParadoxEventManager
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.lang.util.dataFlow.options
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxScopeContextInferenceInfo
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.script.ParadoxScriptFileType
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptMemberElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.properties

/**
 * 推断scripted_trigger、scripted_effect等的作用域上下文（仅限this和root）。
 */
class ParadoxBaseDefinitionInferredScopeContextProvider : ParadoxDefinitionInferredScopeContextProvider {
    object Constants {
        val DEFINITION_TYPES = arrayOf("scripted_trigger", "scripted_effect")
    }

    object Keys : KeyRegistry() {
        val cachedScopeContextInferenceInfo by createKey<CachedValue<ParadoxScopeContextInferenceInfo>>(Keys)
    }

    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type in Constants.DEFINITION_TYPES
    }

    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContextInferenceInfo? {
        if (!PlsFacade.getSettings().inference.scopeContext) return null
        return doGetScopeContextFromCache(definition)
    }

    private fun doGetScopeContextFromCache(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        return CachedValuesManager.getCachedValue(definition, Keys.cachedScopeContextInferenceInfo) {
            ProgressManager.checkCanceled()
            val value = doGetScopeContext(definition)
            value.withDependencyItems(
                ParadoxModificationTrackers.DefinitionScopeContextInferenceTracker,
                getTracker(definition),
            )
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
        if (!r) hasConflict = true
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
        return withRecursionGuard {
            withRecursionCheck("${definitionInfo.name}:${definitionInfo.type}") {
                val indexInfoType = ParadoxIndexInfoType.InferredScopeContextAwareDefinition
                indexInfoType.processQuery(ParadoxScriptFileType, project, gameType, searchScope) p@{ file, infos ->
                    val psiFile = file.toPsiFile(project) ?: return@p true
                    infos.forEach f@{ info ->
                        ProgressManager.checkCanceled()
                        //TODO 1.0.6+ 这里对应的引用可能属于某个复杂表达式的一部分（目前不需要考虑兼容这种情况）
                        val definitionName = info.definitionName
                        if (definitionName != definitionInfo.name) return@f //matches definition name
                        val eventType = info.typeExpression.substringBefore('.')
                        if (eventType != definitionInfo.type) return@f //matches definition type
                        val e = psiFile.findElementAt(info.elementOffset) ?: return@f
                        val m = e.parentOfType<ParadoxScriptMemberElement>(withSelf = false) ?: return@f
                        val scopeContext = ParadoxScopeManager.getSwitchedScopeContext(m) ?: return@f
                        val map = with(scopeContext) {
                            buildMap {
                                put("this", scope.id)
                                root?.let { put("root", it.scope.id) }
                            }
                        }
                        if (scopeContextMap.isNotEmpty()) {
                            val mergedMap = ParadoxScopeManager.mergeScopeContextMap(scopeContextMap, map, true)
                            if (mergedMap != null) {
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
    object Keys : KeyRegistry() {
        val cachedScopeContextInferenceInfo by createKey<CachedValue<ParadoxScopeContextInferenceInfo>>(Keys)
    }

    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == ParadoxDefinitionTypes.Event
    }

    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContextInferenceInfo? {
        if (!PlsFacade.getSettings().inference.scopeContextForEvents) return null
        return doGetScopeContextFromCache(definition)
    }

    private fun doGetScopeContextFromCache(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        return CachedValuesManager.getCachedValue(definition, Keys.cachedScopeContextInferenceInfo) {
            ProgressManager.checkCanceled()
            val value = doGetScopeContext(definition)
            value.withDependencyItems(
                ParadoxModificationTrackers.DefinitionScopeContextInferenceTracker,
                ParadoxModificationTrackers.ScriptFileTracker("common/on_actions/**/*.txt"),
            )
        }
    }

    private fun doGetScopeContext(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        ProgressManager.checkCanceled()
        val definitionInfo = definition.definitionInfo ?: return null
        val configGroup = definitionInfo.configGroup
        val thisEventName = definitionInfo.name
        val thisEventType = ParadoxEventManager.getType(definitionInfo)
        //optimize search scope
        val searchScope = runReadAction { ParadoxSearchScope.fromElement(definition) }
            ?.withFilePath("common/on_actions", "txt")
            ?: return null
        val scopeContextMap = mutableMapOf<String, String>()
        var hasConflict = false
        val r = doProcessQuery(thisEventName, thisEventType, searchScope, scopeContextMap, configGroup)
        if (!r) hasConflict = true
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
        return withRecursionGuard {
            if (depth == 1) stackTrace.addLast(thisEventName)

            val type = ParadoxIndexInfoType.EventInOnAction
            type.processQuery(ParadoxScriptFileType, project, gameType, searchScope) p@{ file, infos ->
                val psiFile = file.toPsiFile(project) ?: return@p true
                infos.forEach f@{ info ->
                    ProgressManager.checkCanceled()
                    val eventName = info.eventName
                    if (eventName != thisEventName) return@f
                    val containingOnActionName = info.containingOnActionName
                    withRecursionCheck(containingOnActionName) {
                        //这里使用psiFile作为contextElement
                        val config = configGroup.extendedOnActions.findFromPattern(containingOnActionName, psiFile, configGroup)
                        if (config == null) return@f //missing
                        if (config.eventType != thisEventType) return@f //invalid (mismatch)
                        val map = config.config.replaceScopes ?: return@f
                        if (scopeContextMap.isNotEmpty()) {
                            val mergedMap = ParadoxScopeManager.mergeScopeContextMap(scopeContextMap, map, true)
                            if (mergedMap != null) {
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
    object Keys : KeyRegistry() {
        val cachedScopeContextInferenceInfo by createKey<CachedValue<ParadoxScopeContextInferenceInfo>>(Keys)
    }

    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == ParadoxDefinitionTypes.Event
    }

    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContextInferenceInfo? {
        if (!PlsFacade.getSettings().inference.scopeContextForEvents) return null
        return doGetScopeContextFromCache(definition)
    }

    private fun doGetScopeContextFromCache(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        return CachedValuesManager.getCachedValue(definition, Keys.cachedScopeContextInferenceInfo) {
            ProgressManager.checkCanceled()
            val value = doGetScopeContext(definition)
            value.withDependencyItems(
                ParadoxModificationTrackers.DefinitionScopeContextInferenceTracker,
                ParadoxModificationTrackers.ScriptFileTracker("events/**/*.txt"),
            )
        }
    }

    private fun doGetScopeContext(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        val definitionInfo = definition.definitionInfo ?: return null
        val configGroup = definitionInfo.configGroup
        val thisEventName = definitionInfo.name
        val thisEventScope = ParadoxEventManager.getScope(definitionInfo)
        //optimize search scope
        val searchScope = runReadAction { ParadoxSearchScope.fromElement(definition) }
            ?: return null
        val scopeContextMap = mutableMapOf<String, String>()
        scopeContextMap.put("this", thisEventScope)
        scopeContextMap.put("root", thisEventScope)
        var hasConflict = false
        val r = doProcessQuery(thisEventName, searchScope, scopeContextMap, configGroup)
        if (!r) hasConflict = true
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
        return withRecursionGuard {
            if (depth == 1) stackTrace.addLast(thisEventName)

            val toRef = "from".repeat(depth)
            val indexInfoType = ParadoxIndexInfoType.EventInEvent
            indexInfoType.processQuery(ParadoxScriptFileType, project, gameType, searchScope) p@{ file, infos ->
                val psiFile = file.toPsiFile(project) ?: return@p true
                infos.forEach f@{ info ->
                    ProgressManager.checkCanceled()
                    val eventName = info.eventName
                    if (eventName != thisEventName) return@f
                    val containingEventName = info.containingEventName
                    withRecursionCheck(containingEventName) {
                        val scopesElementOffset = info.scopesElementOffset
                        if (scopesElementOffset != -1) {
                            //从scopes = { ... }中推断
                            ProgressManager.checkCanceled()
                            val scopesElement = psiFile.findElementAt(scopesElementOffset)?.parentOfType<ParadoxScriptProperty>() ?: return@p false
                            val scopesBlockElement = scopesElement.block ?: return@p false
                            val scopeContextOfScopesElement = ParadoxScopeManager.getSwitchedScopeContext(scopesElement)
                            val map = mutableMapOf<String, String>()
                            scopesBlockElement.properties().options(inline = true).forEach f@{
                                ProgressManager.checkCanceled()
                                val n = it.name.lowercase()
                                if (configGroup.systemScopes.get(n)?.baseId?.lowercase() != "from") return@f

                                if (scopeContextOfScopesElement == null) {
                                    map.put(n, ParadoxScopeManager.anyScopeId)
                                    return@f
                                }

                                val pv = it.propertyValue ?: return@f
                                val expressionString = pv.value
                                val textRange = TextRange.create(0, expressionString.length)
                                val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expressionString, textRange, configGroup) ?: return@f
                                val scopeContextOfEachScope = ParadoxScopeManager.getSwitchedScopeContext(pv, scopeFieldExpression, scopeContextOfScopesElement)
                                map.put(n, scopeContextOfEachScope.scope.id)
                            }

                            if (scopeContextMap.isNotEmpty()) {
                                val mergedMap = ParadoxScopeManager.mergeScopeContextMap(scopeContextMap, map, true)
                                if (mergedMap != null) {
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
                        if (containingEventScope != null) {
                            val newRefScope = containingEventScope
                            val oldRefScope = scopeContextMap.get(toRef)
                            if (oldRefScope == null) {
                                scopeContextMap.put(toRef, newRefScope)
                            } else {
                                val refScope = ParadoxScopeManager.mergeScopeId(oldRefScope, newRefScope)
                                if (refScope == null) {
                                    return@p false
                                }
                                scopeContextMap.put(toRef, refScope)
                            }
                            if (depth >= 4) return@p true
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
    object Keys : KeyRegistry() {
        val cachedScopeContextInferenceInfo by createKey<CachedValue<ParadoxScopeContextInferenceInfo>>(Keys)
    }

    override fun supports(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Boolean {
        return definitionInfo.type == ParadoxDefinitionTypes.OnAction
    }

    override fun getScopeContext(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo): ParadoxScopeContextInferenceInfo? {
        if (!PlsFacade.getSettings().inference.scopeContextForOnActions) return null
        return doGetScopeContextFromCache(definition)
    }

    private fun doGetScopeContextFromCache(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        return CachedValuesManager.getCachedValue(definition, Keys.cachedScopeContextInferenceInfo) {
            ProgressManager.checkCanceled()
            val value = doGetScopeContext(definition)
            value.withDependencyItems(
                ParadoxModificationTrackers.DefinitionScopeContextInferenceTracker,
                ParadoxModificationTrackers.ScriptFileTracker("events/**/*.txt"),
            )
        }
    }

    private fun doGetScopeContext(definition: ParadoxScriptDefinitionElement): ParadoxScopeContextInferenceInfo? {
        ProgressManager.checkCanceled()
        val definitionInfo = definition.definitionInfo ?: return null
        val configGroup = definitionInfo.configGroup
        //skip if on action is predefined
        val config = configGroup.extendedOnActions.findFromPattern(definitionInfo.name, definition, configGroup)
        if (config != null) return null
        val thisOnActionName = definitionInfo.name
        //optimize search scope
        val searchScope = runReadAction { ParadoxSearchScope.fromElement(definition) }
            ?: return null
        val scopeContextMap = mutableMapOf<String, String>()
        scopeContextMap.put("this", ParadoxScopeManager.anyScopeId)
        scopeContextMap.put("root", ParadoxScopeManager.anyScopeId)
        var hasConflict = false
        val r = doProcessQuery(thisOnActionName, searchScope, scopeContextMap, configGroup)
        if (!r) hasConflict = true
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
        return withRecursionGuard {
            if (depth == 1) stackTrace.addLast(thisOnActionName)

            val toRef = "from".repeat(depth)
            val indexInfoType = ParadoxIndexInfoType.OnActionInEvent
            indexInfoType.processQuery(ParadoxScriptFileType, project, gameType, searchScope) p@{ file, infos ->
                val psiFile = file.toPsiFile(project) ?: return@p true
                infos.forEach f@{ info ->
                    ProgressManager.checkCanceled()
                    val onActionName = info.onActionName
                    if (onActionName != thisOnActionName) return@f
                    val containingEventName = info.containingEventName
                    withRecursionCheck(containingEventName) {
                        val scopesElementOffset = info.scopesElementOffset
                        if (scopesElementOffset != -1) {
                            //从scopes = { ... }中推断
                            ProgressManager.checkCanceled()
                            val scopesElement = psiFile.findElementAt(scopesElementOffset)?.parentOfType<ParadoxScriptProperty>() ?: return@p false
                            val scopesBlockElement = scopesElement.block ?: return@p false
                            val scopeContextOfScopesElement = ParadoxScopeManager.getSwitchedScopeContext(scopesElement)
                            val map = mutableMapOf<String, String>()
                            scopesBlockElement.properties().options(inline = true).forEach f@{
                                ProgressManager.checkCanceled()
                                val n = it.name.lowercase()
                                if (configGroup.systemScopes.get(n)?.baseId?.lowercase() != "from") return@f

                                if (scopeContextOfScopesElement == null) {
                                    map.put(n, ParadoxScopeManager.anyScopeId)
                                    return@f
                                }

                                val pv = it.propertyValue ?: return@f
                                val expressionString = pv.value
                                val textRange = TextRange.create(0, expressionString.length)
                                val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expressionString, textRange, configGroup) ?: return@f
                                val scopeContextOfEachScope = ParadoxScopeManager.getSwitchedScopeContext(pv, scopeFieldExpression, scopeContextOfScopesElement)
                                map.put(n, scopeContextOfEachScope.scope.id)
                            }

                            if (scopeContextMap.isNotEmpty()) {
                                val mergedMap = ParadoxScopeManager.mergeScopeContextMap(scopeContextMap, map, true)
                                if (mergedMap != null) {
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
                        if (containingEventScope != null) {
                            val newRefScope = containingEventScope
                            val oldRefScope = scopeContextMap.get(toRef)
                            if (oldRefScope == null) {
                                scopeContextMap.put(toRef, newRefScope)
                            } else {
                                val refScope = ParadoxScopeManager.mergeScopeId(oldRefScope, newRefScope)
                                if (refScope == null) {
                                    return@p false
                                }
                                scopeContextMap.put(toRef, refScope)
                            }
                            if (depth >= 4) return@p true
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
