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
    val cachedEventInvocationsKey = Key.create<CachedValue<Set<String>>>("paradox.cached.event.invocations")
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
        return ParadoxDefinitionHandler.getPrimaryLocalisation(definition)
    }
    
    fun getIconFile(definition: ParadoxScriptDefinitionElement): PsiFile? {
        return ParadoxDefinitionHandler.getPrimaryImage(definition)
    }
    
    /**
     * 得到指定事件可能调用的所有事件。
     *
     * TODO 兼容内联和事件继承的情况。
     */
    fun getInvocations(definition: ParadoxScriptDefinitionElement): Set<String> {
        return CachedValuesManager.getCachedValue(definition, cachedEventInvocationsKey) {
            val value = doGetInvocations(definition)
            CachedValueProvider.Result(value, definition)
        }
    }
    
    private fun doGetInvocations(definition: ParadoxScriptDefinitionElement): Set<String> {
        val result = mutableSetOf<String>()
        definition.block?.acceptChildren(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptStringExpressionElement) visitStringExpressionElement(element)
                if(element.isExpressionOrMemberContext()) super.visitElement(element)
            }
            
            private fun visitStringExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                val value = element.value
                if(result.contains(value)) return
                if(!isValidEventId(value)) return //排除非法的事件ID
                val configs = ParadoxConfigHandler.getConfigs(element)
                val isEventConfig = configs.any { isEventConfig(it) }
                if(isEventConfig) {
                    result.add(value)
                }
            }
            
            private fun isEventConfig(config: CwtDataConfig<*>): Boolean {
                return config.expression.type == CwtDataType.Definition
                    && config.expression.value?.substringBefore('.') == "event"
            }
        })
        return result
    }
    
    /**
     * 得到指定事件的所有事件类型（注有"## group = event_type"，拥有特定的作用域，一般拥有特定的type或者rootKey）。
     */
    fun getEventTypes(project: Project, gameType: ParadoxGameType): List<String> {
        val eventConfig = getCwtConfig(project).get(gameType).types["event"] ?: return emptyList()
        return eventConfig.config.getOrPutUserData(eventTypesKey) {
            eventConfig.subtypes.mapNotNull { (k, v) -> if(v.config.findOption("group")?.stringValue == "event_type") k else null }
        }
    }
    
    fun getEventType(definitionInfo: ParadoxDefinitionInfo): String? {
        return definitionInfo.subtypeConfigs.find { it.config.findOption("group")?.stringValue == "event_type" }?.name
    }
}