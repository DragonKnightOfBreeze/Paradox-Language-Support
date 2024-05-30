package icu.windea.pls.lang.util

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

object ParadoxEventHandler {
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
        return prefix.isNotEmpty() && prefix.isExactIdentifier() && no.isNotEmpty() && no.all { it.isExactDigit() }
    }
    
    fun isMatchedEventId(eventId: String, eventNamespace: String): Boolean {
        val dotIndex = eventId.indexOf('.')
        if(dotIndex == -1) return true //忽略
        val prefix = eventId.substring(0, dotIndex)
        if(prefix.isEmpty()) return true //忽略
        return prefix == eventNamespace
    }
    
    fun getEvents(selector: ChainedParadoxSelector<ParadoxScriptDefinitionElement>): Set<ParadoxScriptDefinitionElement> {
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
    
    /**
     * 得到指定事件的所有事件类型（注有"## group = event_type"，拥有特定的作用域，一般拥有特定的type或者rootKey）。
     */
    fun getTypes(project: Project, gameType: ParadoxGameType): Set<String> {
        val eventConfig = getConfigGroup(project, gameType).types["event"] ?: return emptySet()
        return eventConfig.config.getOrPutUserData(CwtMemberConfig.Keys.eventEventTypes) {
            eventConfig.subtypes.mapNotNullTo(mutableSetOf()) { (k, v) -> if(v.config.findOption("group")?.stringValue == "event_type") k else null }
        }
    }
    
    fun getType(definitionInfo: ParadoxDefinitionInfo): String? {
        return definitionInfo.getOrPutUserData(ParadoxDefinitionInfo.Keys.eventEventType, "") {
            definitionInfo.subtypeConfigs.find { it.config.findOption("group")?.stringValue == "event_type" }?.name
        }
    }
    
    fun getScope(definitionInfo: ParadoxDefinitionInfo): String? {
        return definitionInfo.getOrPutUserData(ParadoxDefinitionInfo.Keys.eventEventScope) {
            definitionInfo.subtypeConfigs.firstNotNullOfOrNull { it.pushScope } ?: ParadoxScopeHandler.anyScopeId
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
        return CachedValuesManager.getCachedValue(definition, PlsKeys.cachedEventInvocations) {
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
                val configs = CwtConfigHandler.getConfigs(element)
                val isEventConfig = configs.any { isEventConfig(it) }
                if(isEventConfig) {
                    result.add(value)
                }
            }
            
            private fun isEventConfig(config: CwtMemberConfig<*>): Boolean {
                return config.expression.type == CwtDataTypes.Definition
                    && config.expression.value?.substringBefore('.') == "event"
            }
        })
        return result
    }
}

private val PlsKeys.cachedEventInvocations by createKey<CachedValue<Set<String>>>("paradox.cached.event.invocations")
private val CwtMemberConfig.Keys.eventEventTypes by createKey<Set<String>>("paradox.event.types")
private val ParadoxDefinitionInfo.Keys.eventEventType by createKey<String>("paradox.event.type")
private val ParadoxDefinitionInfo.Keys.eventEventScope by createKey<String>("paradox.event.scope") 