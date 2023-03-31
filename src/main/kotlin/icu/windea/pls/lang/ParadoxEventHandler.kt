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
import icu.windea.pls.core.search.selectors.chained.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

object ParadoxEventHandler {
    enum class InvocationType {
        All, Immediate, After
    }
    
    val cachedEventInvocationsKey = Key.create<CachedValue<Map<String, InvocationType>>>("paradox.cached.event.invocations")
    
    @JvmStatic
    fun isValidEventNamespace(eventNamespace: String): Boolean {
        if(eventNamespace.isEmpty()) return false
        return eventNamespace.isExactIdentifier()
    }
    
    @JvmStatic
    fun isValidEventId(eventId: String): Boolean {
        if(eventId.isEmpty()) return false
        val dotIndex = eventId.indexOf('.') 
        if(dotIndex == -1) return false
        val prefix = eventId.substring(0, dotIndex)
        val no = eventId.substring(dotIndex + 1)
        return prefix.isNotEmpty() && prefix.isExactIdentifier() && no.isNotEmpty() && no.isExactIdentifier()
    }
    
    
    @JvmStatic
    fun isMatchedEventId(eventId: String, eventNamespace: String): Boolean {
        val dotIndex = eventId.indexOf('.')
        if(dotIndex == -1) return true //忽略
        val prefix = eventId.substring(0, dotIndex)
        if(prefix.isEmpty()) return true //忽略
        return prefix == eventNamespace
    }
    
    @JvmStatic
    fun getEvents(selector: ParadoxDefinitionSelector): Set<ParadoxScriptProperty> {
        val result = mutableSetOf<ParadoxScriptProperty>()
        ParadoxDefinitionSearch.search("event", selector).processQuery {
            if(it is ParadoxScriptProperty) result.add(it)
            true
        }
        return result
    }
    
    @JvmStatic
    fun getName(element: ParadoxScriptProperty): String {
        return element.definitionInfo?.name.orAnonymous()
    }
    
    @JvmStatic
    fun getNamespace(element: ParadoxScriptProperty): String {
        return getName(element).substringBefore(".") //enough
    }
    
    /**
     * 得到event的需要匹配的namespace。
     */
    @JvmStatic
    fun getMatchedNamespace(event: ParadoxScriptProperty): ParadoxScriptProperty? {
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
    
    @JvmStatic
    fun getLocalizedName(definition: ParadoxScriptProperty): ParadoxLocalisationProperty? {
        return definition.definitionInfo?.resolvePrimaryLocalisation()
    }
    
    @JvmStatic
    fun getIconFile(definition: ParadoxScriptProperty): PsiFile? {
        return definition.definitionInfo?.resolvePrimaryImage()
    }
    
    /**
     * 得到指定事件可能调用的所有事件。
     */
    @JvmStatic
    fun getInvocations(definition: ParadoxScriptProperty): Map<String, InvocationType> {
        return CachedValuesManager.getCachedValue(definition, cachedEventInvocationsKey) {
            val value = doGetInvocations(definition)
            CachedValueProvider.Result(value, definition)
        }
    }
    
    private fun doGetInvocations(definition: ParadoxScriptProperty): Map<String, InvocationType> {
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
}