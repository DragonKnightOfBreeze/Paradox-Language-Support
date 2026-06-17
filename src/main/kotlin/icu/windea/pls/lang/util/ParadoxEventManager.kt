package icu.windea.pls.lang.util

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtSubtypeGroup
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.core.isExactDigit
import icu.windea.pls.core.orNull
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getOrPutUserData
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.isIdentifier
import icu.windea.pls.lang.resolve.ParadoxEventService
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.scope.ParadoxScopeConstants
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.propertyValue
import icu.windea.pls.script.psi.stringValue

@Suppress("unused")
object ParadoxEventManager {
    object Keys : KeyRegistry() {
        val cachedEventInvocations by registerKey<CachedValue<Set<String>>>(Keys)
        val eventAllTypes by registerKey<Set<String>>(Keys)
        val eventAllAttributes by registerKey<Set<String>>(Keys)
        val eventType by registerKey<String>(Keys)
        val eventAttributes by registerKey<Set<String>>(Keys)
        val eventScope by registerKey<String>(Keys)
    }

    fun isValidEventNamespace(eventNamespace: String): Boolean {
        if (eventNamespace.isEmpty()) return false
        return eventNamespace.isIdentifier()
    }

    fun isValidEventId(eventId: String): Boolean {
        if (eventId.isEmpty()) return false
        val dotIndex = eventId.indexOf('.')
        if (dotIndex == -1) return false
        val prefix = eventId.substring(0, dotIndex)
        val no = eventId.substring(dotIndex + 1)
        return prefix.isNotEmpty() && prefix.isIdentifier() && no.isNotEmpty() && no.all { it.isExactDigit() }
    }

    fun isMatchedEventId(eventId: String, eventNamespace: String): Boolean {
        val dotIndex = eventId.indexOf('.')
        if (dotIndex == -1) return true // 忽略
        val prefix = eventId.substring(0, dotIndex)
        if (prefix.isEmpty()) return true // 忽略
        return prefix == eventNamespace
    }

    fun getEvents(selector: ParadoxDefinitionSearch.Selector): List<ParadoxScriptProperty> {
        return ParadoxDefinitionSearch.searchProperty(null, ParadoxDefinitionTypes.event, selector).findAll()
    }

    fun getName(element: ParadoxDefinitionElement): String {
        return element.definitionInfo?.name.or.anonymous()
    }

    fun getNamespaceFromEventNamespaceDeclaration(element: ParadoxDefinitionElement): String? {
        if(element !is ParadoxScriptProperty) return null // TODO [inline_definition]
        return element.propertyValue<ParadoxScriptString>()?.stringValue
    }

    fun getNamespaceFromEventDeclaration(element: ParadoxDefinitionElement): String? {
        if(element !is ParadoxScriptProperty) return null // TODO [inline_definition]
        return getName(element).substringBefore('.').orNull() // enough
    }

    fun getBoundNamespaceElement(event: ParadoxDefinitionElement): ParadoxScriptProperty? {
        var current = event.prevSibling ?: return null
        while (true) {
            if (current is ParadoxScriptProperty && current.name.equals("namespace", true)) {
                if (current.propertyValue is ParadoxScriptString) {
                    return current
                } else {
                    return null // invalid
                }
            }
            current = current.prevSibling ?: return null
        }
    }

    fun getAllTypes(gameType: ParadoxGameType): Set<String> {
        val eventConfig = PlsFacade.getConfigGroup(gameType).types[ParadoxDefinitionTypes.event] ?: return emptySet()
        return eventConfig.config.getOrPutUserData(Keys.eventAllTypes) {
            eventConfig.subtypes.values.filter { it in CwtSubtypeGroup.EventType }.map { it.name }.toSet()
        }
    }

    fun getAllTypeConfigs(project: Project, gameType: ParadoxGameType): Collection<CwtSubtypeConfig> {
        val eventConfig = PlsFacade.getConfigGroup(project, gameType).types[ParadoxDefinitionTypes.event] ?: return emptySet()
        return eventConfig.subtypes.values.filter { it in CwtSubtypeGroup.EventType }
    }

    fun getAllAttributes(gameType: ParadoxGameType): Set<String> {
        val eventConfig = PlsFacade.getConfigGroup(gameType).types[ParadoxDefinitionTypes.event] ?: return emptySet()
        return eventConfig.config.getOrPutUserData(Keys.eventAllAttributes) {
            eventConfig.subtypes.values.filter { it in CwtSubtypeGroup.EventAttribute }.map { it.name }.toSet()
        }
    }

    fun getAllAttributeConfigs(project: Project, gameType: ParadoxGameType): Collection<CwtSubtypeConfig> {
        val eventConfig = PlsFacade.getConfigGroup(project, gameType).types[ParadoxDefinitionTypes.event] ?: return emptySet()
        return eventConfig.subtypes.values.filter { it in CwtSubtypeGroup.EventAttribute }
    }

    fun getType(element: ParadoxDefinitionElement): String? {
        return element.definitionInfo?.let { getType(it) }
    }

    fun getType(definitionInfo: ParadoxDefinitionInfo): String? {
        return definitionInfo.getOrPutUserData(Keys.eventType) {
            definitionInfo.subtypeConfigs.find { it in CwtSubtypeGroup.EventType }?.name
        }
    }

    fun getAttributes(element: ParadoxDefinitionElement): Set<String> {
        return element.definitionInfo?.let { getAttributes(it) }.orEmpty()
    }

    fun getAttributes(definitionInfo: ParadoxDefinitionInfo): Set<String> {
        return definitionInfo.getOrPutUserData(Keys.eventAttributes) {
            definitionInfo.subtypeConfigs.filter { it in CwtSubtypeGroup.EventAttribute }.mapTo(mutableSetOf()) { it.name }
        }
    }

    fun getScope(element: ParadoxDefinitionElement): String {
        return element.definitionInfo?.let { getScope(it) } ?: ParadoxScopeConstants.anyScope
    }

    fun getScope(definitionInfo: ParadoxDefinitionInfo): String {
        return definitionInfo.getOrPutUserData(Keys.eventScope) {
            definitionInfo.subtypeConfigs.firstNotNullOfOrNull { it.config.optionData.pushScope } ?: ParadoxScopeConstants.anyScope
        }
    }

    fun getLocalizedNameElement(definition: ParadoxDefinitionElement): ParadoxLocalisationProperty? {
        return ParadoxDefinitionManager.getPrimaryLocalisation(definition)
    }

    fun getIconFile(definition: ParadoxDefinitionElement): PsiFile? {
        return ParadoxDefinitionManager.getPrimaryImage(definition)
    }

    /**
     * 得到指定事件可能调用的所有事件的名字。
     */
    fun getInvocations(definition: ParadoxDefinitionElement): Set<String> {
        // TODO 考虑兼容需要内联和事件继承的情况
        return CachedValuesManager.getCachedValue(definition, Keys.cachedEventInvocations) {
            val value = ParadoxEventService.resolveInvocations(definition)
            CachedValueProvider.Result(value, definition)
        }
    }

    /**
     * 得到作为调用者的事件列表。
     */
    fun getInvokerEvents(definition: ParadoxDefinitionElement, selector: ParadoxDefinitionSearch.Selector): List<ParadoxScriptProperty> {
        return ParadoxEventService.resolveInvokerEvents(definition, selector)
    }

    /**
     * 得到调用的事件列表。
     */
    fun getInvokedEvents(definition: ParadoxDefinitionElement, selector: ParadoxDefinitionSearch.Selector): List<ParadoxScriptProperty> {
        return ParadoxEventService.resolveInvokedEvents(definition, selector)
    }
}
