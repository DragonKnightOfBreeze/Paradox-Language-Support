package icu.windea.pls.lang.util

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.references.*

object ParadoxEventManager {
    object Keys : KeyRegistry() {
        val cachedEventInvocations by createKey<CachedValue<Set<String>>>(this)
        val eventAllTypes by createKey<Set<String>>(this)
        val eventAllAttributes by createKey<Set<String>>(this)
        val eventType by createKey<String>(this)
        val eventAttributes by createKey<Set<String>>(this)
        val eventScope by createKey<String>(this)
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
        if (dotIndex == -1) return true //忽略
        val prefix = eventId.substring(0, dotIndex)
        if (prefix.isEmpty()) return true //忽略
        return prefix == eventNamespace
    }

    fun getEvents(selector: ChainedParadoxSelector<ParadoxScriptDefinitionElement>): Set<ParadoxScriptDefinitionElement> {
        return ParadoxDefinitionSearch.search(ParadoxDefinitionTypes.Event, selector).findAll()
    }

    fun getName(element: ParadoxScriptDefinitionElement): String {
        return element.definitionInfo?.name.orAnonymous()
    }

    fun getNamespace(element: ParadoxScriptDefinitionElement): String {
        return getName(element).substringBefore(".") //enough
    }

    /**
     * 得到event的需要匹配的namespace。
     */
    fun getMatchedNamespace(event: ParadoxScriptDefinitionElement): ParadoxScriptProperty? {
        var current = event.prevSibling ?: return null
        while (true) {
            if (current is ParadoxScriptProperty && current.name.equals("namespace", true)) {
                if (current.propertyValue is ParadoxScriptString) {
                    return current
                } else {
                    return null //invalid
                }
            }
            current = current.prevSibling ?: return null
        }
    }

    fun getAllTypes(gameType: ParadoxGameType): Set<String> {
        val eventConfig = PlsFacade.getConfigGroup(gameType).types[ParadoxDefinitionTypes.Event] ?: return emptySet()
        return eventConfig.config.getOrPutUserData(Keys.eventAllTypes) {
            eventConfig.subtypes.values.filter { it.inGroup("event_type") }.map { it.name }.toSet()
        }
    }

    fun getAllTypeConfigs(project: Project, gameType: ParadoxGameType): Collection<CwtSubtypeConfig> {
        val eventConfig = PlsFacade.getConfigGroup(project, gameType).types[ParadoxDefinitionTypes.Event] ?: return emptySet()
        return eventConfig.subtypes.values.filter { it.inGroup("event_type") }
    }

    fun getAllAttributes(gameType: ParadoxGameType): Set<String> {
        val eventConfig = PlsFacade.getConfigGroup(gameType).types[ParadoxDefinitionTypes.Event] ?: return emptySet()
        return eventConfig.config.getOrPutUserData(Keys.eventAllAttributes) {
            eventConfig.subtypes.values.filter { it.inGroup("event_attribute") }.map { it.name }.toSet()
        }
    }

    fun getAllAttributeConfigs(project: Project, gameType: ParadoxGameType): Collection<CwtSubtypeConfig> {
        val eventConfig = PlsFacade.getConfigGroup(project, gameType).types[ParadoxDefinitionTypes.Event] ?: return emptySet()
        return eventConfig.subtypes.values.filter { it.inGroup("event_attribute") }
    }

    fun getType(element: ParadoxScriptDefinitionElement): String? {
        return element.definitionInfo?.let { getType(it) }
    }

    fun getType(definitionInfo: ParadoxDefinitionInfo): String? {
        return definitionInfo.getOrPutUserData(Keys.eventType, "") {
            definitionInfo.subtypeConfigs.find { it.inGroup("event_type") }?.name
        }
    }

    fun getAttributes(element: ParadoxScriptDefinitionElement): Set<String> {
        return element.definitionInfo?.let { getAttributes(it) }.orEmpty()
    }

    fun getAttributes(definitionInfo: ParadoxDefinitionInfo): Set<String> {
        return definitionInfo.getOrPutUserData(Keys.eventAttributes) {
            definitionInfo.subtypeConfigs.filter { it.inGroup("event_attribute") }.mapTo(mutableSetOf()) { it.name }
        }
    }

    fun getScope(element: ParadoxScriptDefinitionElement): String {
        return element.definitionInfo?.let { getScope(it) } ?: ParadoxScopeManager.anyScopeId
    }

    fun getScope(definitionInfo: ParadoxDefinitionInfo): String {
        return definitionInfo.getOrPutUserData(Keys.eventScope) {
            definitionInfo.subtypeConfigs.firstNotNullOfOrNull { it.config.pushScope } ?: ParadoxScopeManager.anyScopeId
        }
    }

    fun getLocalizedName(definition: ParadoxScriptDefinitionElement): ParadoxLocalisationProperty? {
        return ParadoxDefinitionManager.getPrimaryLocalisation(definition)
    }

    fun getIconFile(definition: ParadoxScriptDefinitionElement): PsiFile? {
        return ParadoxDefinitionManager.getPrimaryImage(definition)
    }

    /**
     * 得到指定事件可能调用的所有事件的名字。
     *
     * TODO 考虑兼容需要内联和事件继承的情况。
     */
    fun getInvocations(definition: ParadoxScriptDefinitionElement): Set<String> {
        return CachedValuesManager.getCachedValue(definition, Keys.cachedEventInvocations) {
            val value = doGetInvocations(definition)
            CachedValueProvider.Result(value, definition)
        }
    }

    private fun doGetInvocations(definition: ParadoxScriptDefinitionElement): Set<String> {
        val result = mutableSetOf<String>()
        definition.block?.acceptChildren(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptStringExpressionElement) visitStringExpressionElement(element)
                if (!ParadoxPsiManager.inMemberContext(element)) return //optimize
                super.visitElement(element)
            }

            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                val value = element.value
                if (result.contains(value)) return
                if (!isValidEventId(value)) return //排除非法的事件ID
                val configs = ParadoxExpressionManager.getConfigs(element)
                val isEventConfig = configs.any { isEventConfig(it) }
                if (isEventConfig) {
                    result.add(value)
                }
            }

            private fun isEventConfig(config: CwtMemberConfig<*>): Boolean {
                return config.configExpression.type == CwtDataTypes.Definition
                    && config.configExpression.value?.substringBefore('.') == ParadoxDefinitionTypes.Event
            }
        })
        return result
    }

    /**
     * 得到作为调用者的事件列表。
     */
    fun getInvokerEvents(definition: ParadoxScriptDefinitionElement, selector: ChainedParadoxSelector<ParadoxScriptDefinitionElement>): List<ParadoxScriptDefinitionElement> {
        //NOTE 1. 目前不兼容封装变量引用 2. 这里需要从所有同名定义查找使用
        //NOTE 为了优化性能，这里可能需要新增并应用索引

        val name = definition.definitionInfo?.name
        if (name.isNullOrEmpty()) return emptyList()
        selector.withGameType(ParadoxGameType.Stellaris)
        return buildList b@{
            ParadoxDefinitionSearch.search(name, ParadoxDefinitionTypes.Event, selector).processQuery p0@{ definition0 ->
                ProgressManager.checkCanceled()
                ReferencesSearch.search(definition0, selector.scope).processQuery p@{ ref ->
                    if (ref !is ParadoxScriptExpressionPsiReference) return@p true
                    ProgressManager.checkCanceled()
                    val refElement = ref.element.castOrNull<ParadoxScriptString>() ?: return@p true
                    val rDefinition = refElement.findParentDefinition() ?: return@p true
                    val rDefinitionInfo = rDefinition.definitionInfo ?: return@p true
                    if (rDefinitionInfo.name.isEmpty()) return@p true
                    if (rDefinitionInfo.type != ParadoxDefinitionTypes.Event) return@p true
                    this += rDefinition
                    true
                }
                true
            }
        }.distinct()
    }

    /**
     * 得到调用的事件列表。
     */
    fun getInvokedEvents(definition: ParadoxScriptDefinitionElement, selector: ChainedParadoxSelector<ParadoxScriptDefinitionElement>): List<ParadoxScriptDefinitionElement> {
        //NOTE 1. 目前不兼容封装变量引用
        //NOTE 为了优化性能，这里可能需要新增并应用索引

        val name = definition.definitionInfo?.name
        if (name.isNullOrEmpty()) return emptyList()
        val invocations = getInvocations(definition)
        if (invocations.isEmpty()) return emptyList()
        selector.withGameType(ParadoxGameType.Stellaris)
        return buildList b@{
            ParadoxDefinitionSearch.search(ParadoxDefinitionTypes.Event, selector).processQuery p@{ rDefinition ->
                ProgressManager.checkCanceled()
                val rDefinitionInfo = rDefinition.definitionInfo ?: return@p true
                if (rDefinitionInfo.name.isEmpty()) return@p true
                if (rDefinitionInfo.name !in invocations) return@p true
                this += rDefinition
                true
            }
        }.distinct()
    }
}
