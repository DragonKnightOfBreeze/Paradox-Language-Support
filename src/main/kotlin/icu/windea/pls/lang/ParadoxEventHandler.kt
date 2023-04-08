package icu.windea.pls.lang

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

object ParadoxEventHandler {
    enum class InvocationType { All, Immediate, After }
    
    val cachedEventInvocationsKey = Key.create<CachedValue<Map<String, InvocationType>>>("paradox.cached.event.invocations")
    val eventTypesKey = Key.create<List<String>>("paradox.event.types")
    
    fun isValidEventNamespace(eventNamespace: String): Boolean {
        if(eventNamespace.isEmpty()) return false
        return eventNamespace.isExactIdentifier()
    }
    
    fun isValidEventId(eventId: String): Boolean {
        if(eventId.isEmpty()) return false
        val dotIndex = eventId.indexOf('.')
        if(dotIndex == -1) return false
        val prefix = eventId.substring(0, dotIndex)
        val no = eventId.substring(dotIndex + 1)
        return prefix.isNotEmpty() && prefix.isExactIdentifier() && no.isNotEmpty() && no.isExactIdentifier()
    }
    
    fun isMatchedEventId(eventId: String, eventNamespace: String): Boolean {
        val dotIndex = eventId.indexOf('.')
        if(dotIndex == -1) return true //忽略
        val prefix = eventId.substring(0, dotIndex)
        if(prefix.isEmpty()) return true //忽略
        return prefix == eventNamespace
    }
    
    fun getEvents(selector: ParadoxDefinitionSelector): Set<ParadoxScriptDefinitionElement> {
        return ParadoxDefinitionSearch.search("events", selector).findAll()
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
        while(true) {
            if(current is ParadoxScriptProperty && current.name.equals("namespace", true)) {
                if(current.propertyValue is ParadoxScriptString) {
                    return current
                } else {
                    return null //invalid
                }
            }
            current = current.prevSibling ?: return null
        }
    }
    
    fun getLocalizedName(definition: ParadoxScriptDefinitionElement): ParadoxLocalisationProperty? {
        return definition.definitionInfo?.resolvePrimaryLocalisation()
    }
    
    fun getIconFile(definition: ParadoxScriptDefinitionElement): PsiFile? {
        return definition.definitionInfo?.resolvePrimaryImage()
    }
    
    /**
     * 得到指定事件可能调用的所有事件。
     */
    fun getInvocations(definition: ParadoxScriptDefinitionElement): Map<String, InvocationType> {
        return CachedValuesManager.getCachedValue(definition, cachedEventInvocationsKey) {
            val value = doGetInvocations(definition)
            CachedValueProvider.Result(value, definition)
        }
    }
    
    private fun doGetInvocations(definition: ParadoxScriptDefinitionElement): Map<String, InvocationType> {
        val result = mutableMapOf<String, InvocationType>()
        definition.block?.processProperty p@{ prop ->
            ProgressManager.checkCanceled()
            val propName = prop.name.lowercase()
            val invocationType = when(propName) {
                "immediate" -> InvocationType.Immediate
                "after" -> InvocationType.After
                else -> return@p true
            }
            prop.block?.acceptChildren(object : PsiRecursiveElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    if(element is ParadoxScriptStringExpressionElement) visitStringExpressionElement(element)
                    if(element.isExpressionOrMemberContext()) super.visitElement(element)
                }
                
                private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                    ProgressManager.checkCanceled()
                    val value = element.value
                    if(value.count { it == '.' } != 1) return //事件ID应当包含一个点号，这里用来提高性能
                    val configs = ParadoxConfigHandler.getConfigs(element)
                    val isEventConfig = configs.any { isEventConfig(it) }
                    if(isEventConfig) {
                        result.compute(value) { _, t ->
                            when {
                                t == null || t == invocationType -> invocationType
                                else -> InvocationType.All
                            }
                        }
                    }
                }
                
                private fun isEventConfig(config: CwtDataConfig<*>): Boolean {
                    return config.expression.type == CwtDataType.Definition
                        && config.expression.value?.substringBefore('.') == "event"
                }
            })
            true
        }
        return result
    }
    
    fun getEventTypes(project: Project, gameType: ParadoxGameType): List<String> {
        val eventConfig = getCwtConfig(project).getValue(gameType).types["event"] ?: return emptyList()
        return eventConfig.config.getOrPutUserData(eventTypesKey) {
            //subtypes, ends with "_event"
            eventConfig.subtypes.keys.filter { it.endsWith("_event") }
        }
    }
}