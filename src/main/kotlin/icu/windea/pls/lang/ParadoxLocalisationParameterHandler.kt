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
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.scope.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.hierarchy.impl.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

@Suppress("UNUSED_PARAMETER")
object ParadoxLocalisationParameterHandler {
    val cachedParameterNamesKey = Key.create<CachedValue<Set<String>>>("paradox.localisation.property.cached.parameterNames")
    
    fun getParameterNames(element: ParadoxLocalisationProperty): Set<String> {
        return doGetParameterNamesFromCache(element)
    }
    
    private fun doGetParameterNamesFromCache(element: ParadoxLocalisationProperty): Set<String> {
        return CachedValuesManager.getCachedValue(element, cachedParameterNamesKey) {
            val value = doGetParameters(element)
            //invalidated on element modification or ScriptFileTracker
            val tracker = ParadoxPsiModificationTracker.getInstance(element.project).ScriptFileTracker
            CachedValueProvider.Result.create(value, element, tracker)
        }
    }
    
    private fun doGetParameters(element: ParadoxLocalisationProperty): Set<String> {
        return doGetParametersFromDefinitionHierarchyIndex(element)
    }
    
    private fun doGetParametersFromDefinitionHierarchyIndex(element: ParadoxLocalisationProperty): MutableSet<String> {
        val result = mutableSetOf<String>().synced()
        val targetLocalisationName = element.name
        val selector = definitionHierarchySelector(element.project, element)
        ParadoxDefinitionHierarchyHandler.processQuery(selector) p@{ file, fileData ->
            val infos = fileData.get(ParadoxLocalisationParameterDefinitionHierarchySupport.ID) ?: return@p true
            infos.forEachFast { info ->
                val localisationName = info.getUserData(ParadoxLocalisationParameterDefinitionHierarchySupport.localisationKey)
                if(localisationName == targetLocalisationName) result.add(info.expression)
            }
            true
        }
        return result
    }
    
    @Deprecated("Use doGetParametersFromDefinitionHierarchyIndex()")
    private fun doGetParametersFromReferenceSearch(element: ParadoxLocalisationProperty): Set<String> {
        val result = mutableSetOf<String>().synced()
        val searchScope = runReadAction { ParadoxSearchScope.fromElement(element) }
            ?.withFileTypes(ParadoxScriptFileType)
            ?: return emptySet()
        ProgressManager.checkCanceled()
        ProgressManager.getInstance().runProcess({
            ReferencesSearch.search(element, searchScope).processQueryAsync p@{ reference ->
                ProgressManager.checkCanceled()
                val localisationReferenceElement = reference.element
                if(localisationReferenceElement is ParadoxScriptString) {
                    val valueConfigs = ParadoxConfigResolver.getValueConfigs(localisationReferenceElement, true, true, ParadoxConfigMatcher.Options.Default)
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
    
    fun getLocalisationReferenceElement(element: ParadoxScriptExpressionElement, config: CwtMemberConfig<*>): ParadoxScriptString? {
        if(config !is CwtPropertyConfig || config.expression.type != CwtDataType.LocalisationParameter) return null
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
    
    private fun isMatchedProperty(element: PsiElement, config: CwtMemberConfig<*>): Boolean {
        if(element is ParadoxScriptProperty) {
            val configs = ParadoxConfigResolver.getConfigs(element, allowDefinition = true, matchOptions = ParadoxConfigMatcher.Options.Default)
            if(configs.any { it pointerEquals config }) {
                return true
            }
        }
        return false
    }
    
    private fun isLocalisationConfig(element: PsiElement, config: CwtMemberConfig<*>): Boolean {
        if(config !is CwtPropertyConfig) return false
        val dataType = config.valueExpression.type
        return dataType == CwtDataType.Localisation || (dataType == CwtDataType.InlineLocalisation && !element.text.isLeftQuoted())
    }
    
    private fun isParameterConfig(element: PsiElement, config: CwtMemberConfig<*>): Boolean {
        if(config !is CwtPropertyConfig) return false
        val dataType = config.keyExpression.type
        return dataType == CwtDataType.LocalisationParameter
    }
}