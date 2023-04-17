package icu.windea.pls.lang

import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import com.intellij.psi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.scope.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

@Suppress("UNUSED_PARAMETER")
object ParadoxLocalisationParameterHandler {
    val cachedParameterNamesKey = Key.create<CachedValue<Set<String>>>("paradox.localisation.property.cached.parameterNames")
    
    fun getParameterNames(element: ParadoxLocalisationProperty): Set<String> {
        return getParameterNamesFromCache(element)
    }
    
    private fun getParameterNamesFromCache(element: ParadoxLocalisationProperty): Set<String> {
        return CachedValuesManager.getCachedValue(element, cachedParameterNamesKey) {
            val value = doGetParameters(element)
            val tracker = ParadoxModificationTrackerProvider.getInstance().ScriptFile
            CachedValueProvider.Result.create(value, element, tracker)
        }
    }
    
    private fun doGetParameters(element: ParadoxLocalisationProperty): Set<String>? {
        val result = mutableSetOf<String>().synced()
        val searchScope = runReadAction { ParadoxSearchScope.fromElement(element) }
            ?.withFileTypes(ParadoxScriptFileType)
            ?: return null
        ProgressManager.getInstance().runProcess({
            ReferencesSearch.search(element, searchScope).processQueryAsync p@{ reference ->
                ProgressManager.checkCanceled()
                val localisationReferenceElement = reference.element
                if(localisationReferenceElement is ParadoxScriptString) {
                    val valueConfigs = ParadoxConfigHandler.getValueConfigs(localisationReferenceElement)
                    val valueConfig = valueConfigs.firstOrNull() ?: return@p true
                    val config = valueConfig.propertyConfig ?: return@p true
                    val parameterPropertyElements = findParameterPropertiesFromLocalisationProperty(localisationReferenceElement, config)
                    for(parameterPropertyElement in parameterPropertyElements) {
                        result.add(parameterPropertyElement.name)
                    }
                }
                true
            }
        }, EmptyProgressIndicator())
        return result
    }
    
    fun getLocalisationReferenceElement(element: ParadoxScriptExpressionElement, config: CwtPropertyConfig): ParadoxScriptString? {
        val localisationReferencePropertyElement = findLocalisationPropertyFromParameterProperty(element, config)
        return localisationReferencePropertyElement?.propertyValue?.castOrNull()
    }
    
    private fun findParameterPropertiesFromLocalisationProperty(element: ParadoxScriptExpressionElement, config: CwtPropertyConfig): List<ParadoxScriptProperty> {
        val configToUse = config.parent?.configs?.firstNotNullOfOrNull { c -> c.configs?.find { isParameterConfig(element, it) } }
        if(configToUse == null) return emptyList()
        val context = element.findParentProperty(fromParentBlock = true)
            ?.castOrNull<ParadoxScriptProperty>()
            ?: return emptyList()
        val result = mutableListOf<ParadoxScriptProperty>()
        context.block?.processProperty p@{ p ->
            p.block?.processProperty pp@{ pp ->
                if(isMatchedProperty(pp, configToUse)) {
                    result.add(pp)
                }
                true
            }
            true
        }
        return result
    }
    
    private fun findLocalisationPropertyFromParameterProperty(element: ParadoxScriptExpressionElement, config: CwtPropertyConfig): ParadoxScriptProperty? {
        val configToUse = config.parent?.parent?.configs?.find { isLocalisationConfig(element, it) }
        if(configToUse == null) return null
        val context = element.findParentProperty(fromParentBlock = true)
            ?.findParentProperty(fromParentBlock = true)
            ?.castOrNull<ParadoxScriptProperty>()
            ?: return null
        var result: ParadoxScriptProperty? = null
        context.block?.processProperty p@{ p ->
            if(isMatchedProperty(p, configToUse)) {
                result = p
                return@p false
            }
            true
        }
        return result
    }
    
    private fun isMatchedProperty(element: PsiElement, config: CwtDataConfig<*>): Boolean {
        if(element is ParadoxScriptProperty) {
            val configs = ParadoxConfigHandler.getConfigs(element, allowDefinition = true)
            if(configs.any { it pointerEquals config }) {
                return true
            }
        }
        return false
    }
    
    private fun isLocalisationConfig(element: PsiElement, config: CwtDataConfig<*>): Boolean {
        if(config !is CwtPropertyConfig) return false
        val dataType = config.valueExpression.type
        return dataType == CwtDataType.Localisation || (dataType == CwtDataType.InlineLocalisation && !element.text.isLeftQuoted())
    }
    
    private fun isParameterConfig(element: PsiElement, config: CwtDataConfig<*>): Boolean {
        if(config !is CwtPropertyConfig) return false
        val dataType = config.keyExpression.type
        return dataType == CwtDataType.LocalisationParameter
    }
}