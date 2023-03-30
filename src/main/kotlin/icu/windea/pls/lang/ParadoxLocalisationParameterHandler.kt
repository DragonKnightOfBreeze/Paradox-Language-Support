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
import icu.windea.pls.core.search.scopes.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

@Suppress("UNUSED_PARAMETER")
object ParadoxLocalisationParameterHandler {
    val cachedParameterNamesKey = Key.create<CachedValue<Set<String>>>("paradox.localisation.property.cached.parameterNames")
    
    @JvmStatic
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
        val result = mutableSetOf<String>()
        val searchScope = runReadAction { ParadoxGlobalSearchScope.fromElement(element) }
            ?.withFileType(ParadoxScriptFileType)
            ?: return null
        ReferencesSearch.search(element, searchScope).processQuery p@{ reference ->
            ProgressManager.checkCanceled()
            val localisationReferenceElement = reference.element
            if(localisationReferenceElement is ParadoxScriptString) {
                val valueConfigs = ParadoxConfigHandler.getValueConfigs(localisationReferenceElement)
                val valueConfig = valueConfigs.firstOrNull() ?: return@p true
                val config = valueConfig.propertyConfig ?: return@p true
                val parameterConfig = config.parent?.configs?.find { isParameterConfig(element, it) } ?: return@p true
                val parameterPropertyElements = findProperties(localisationReferenceElement, parameterConfig)
                for(parameterPropertyElement in parameterPropertyElements) {
                    result.add(parameterPropertyElement.name)
                }
            }
            true
        }
        return result
    }
    
    @JvmStatic
    fun getLocalisationReferenceElement(element: ParadoxScriptExpressionElement, config: CwtPropertyConfig): ParadoxScriptString? {
        val localisationConfig = config.parent?.configs?.find { isLocalisationConfig(element, it) }
        if(localisationConfig == null) return null
        val localisationReferencePropertyElement = findProperty(element, localisationConfig)
        return localisationReferencePropertyElement?.propertyValue?.castOrNull()
    }
    
    private fun findProperties(element: ParadoxScriptExpressionElement, config: CwtDataConfig<*>): List<ParadoxScriptProperty> {
        val result = mutableListOf<ParadoxScriptProperty>()
        element.siblings(forward = true, withSelf = false).mapNotNullTo(result) { doFindProperty(it, config) }
        element.siblings(forward = false, withSelf = false).mapNotNullTo(result) { doFindProperty(it, config) }
        return result
    }
    
    private fun findProperty(element: ParadoxScriptExpressionElement, config: CwtDataConfig<*>): ParadoxScriptProperty? {
        return (element.siblings(forward = true, withSelf = false).firstNotNullOfOrNull { doFindProperty(it, config) }
            ?: element.siblings(forward = false, withSelf = false).firstNotNullOfOrNull { doFindProperty(it, config) })
    }
    
    private fun doFindProperty(element: PsiElement, config: CwtDataConfig<*>): ParadoxScriptProperty? {
        if(element is ParadoxScriptProperty) {
            val configs = ParadoxConfigHandler.getConfigs(element, allowDefinition = true)
            if(configs.any { it pointerEquals config }) {
                return element
            }
        }
        return null
    }
    
    private fun isLocalisationConfig(element: PsiElement, config: CwtDataConfig<*>): Boolean {
        if(config !is CwtPropertyConfig) return false
        val dataType = config.valueExpression.type
        return dataType == CwtDataType.Localisation || (dataType == CwtDataType.InlineLocalisation && !element.text.isLeftQuoted())
    }
    
    private fun isParameterConfig(element: PsiElement, config: CwtDataConfig<*>): Boolean {
        if(config !is CwtPropertyConfig) return false
        val dataType = config.valueExpression.type
        return dataType == CwtDataType.LocalisationParameter
    }
}