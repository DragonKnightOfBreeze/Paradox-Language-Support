package icu.windea.pls.lang.util

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icons.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.parameter.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.CwtConfigMatcher.Options
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

@Suppress("UNUSED_PARAMETER")
object ParadoxLocalisationParameterHandler {
    val cachedParameterNamesKey = createKey<CachedValue<Set<String>>>("paradox.localisation.property.cached.parameterNames")
    
    fun getParameterNames(element: ParadoxLocalisationProperty): Set<String> {
        return doGetParameterNamesFromCache(element)
    }
    
    private fun doGetParameterNamesFromCache(element: ParadoxLocalisationProperty): Set<String> {
        return CachedValuesManager.getCachedValue(element, cachedParameterNamesKey) {
            val value = doGetParameters(element)
            //invalidated on element modification or ScriptFileTracker
            val tracker = ParadoxModificationTrackers.ScriptFileTracker
            CachedValueProvider.Result.create(value, element, tracker)
        }
    }
    
    private fun doGetParameters(element: ParadoxLocalisationProperty): Set<String> {
        return doGetParametersFromDefinitionHierarchyIndex(element)
    }
    
    private fun doGetParametersFromDefinitionHierarchyIndex(element: ParadoxLocalisationProperty): Set<String> {
        val targetLocalisationName = element.name
        val result = mutableSetOf<String>().synced()
        val selector = localisationParameterSelector(element.project, element)
        ParadoxLocalisationParameterSearch.search(targetLocalisationName, selector).processQueryAsync p@{ info ->
            result.add(info.name)
            true
        }
        return result
    }
    
    fun getLocalisationReferenceElement(element: ParadoxScriptExpressionElement, config: CwtMemberConfig<*>): ParadoxScriptString? {
        if(config !is CwtPropertyConfig || config.expression.type != CwtDataTypes.LocalisationParameter) return null
        val localisationReferencePropertyElement = findLocalisationPropertyFromParameterProperty(element, config)
        return localisationReferencePropertyElement?.propertyValue?.castOrNull()
    }
    
    private fun findParameterPropertiesFromLocalisationProperty(element: ParadoxScriptExpressionElement, config: CwtPropertyConfig): List<ParadoxScriptProperty> {
        val configToUse = config.parentConfig?.configs?.firstNotNullOfOrNull { c -> c.configs?.find { isParameterConfig(element, it) } }
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
        val configToUse = config.parentConfig?.parentConfig?.configs?.find { isLocalisationConfig(element, it) }
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
            val configs = ParadoxExpressionHandler.getConfigs(element, matchOptions = Options.Default or Options.AcceptDefinition)
            if(configs.any { it.pointer == config.pointer }) {
                return true
            }
        }
        return false
    }
    
    private fun isLocalisationConfig(element: PsiElement, config: CwtMemberConfig<*>): Boolean {
        if(config !is CwtPropertyConfig) return false
        val dataType = config.valueExpression.type
        return dataType == CwtDataTypes.Localisation || (dataType == CwtDataTypes.InlineLocalisation && !element.text.isLeftQuoted())
    }
    
    private fun isParameterConfig(element: PsiElement, config: CwtMemberConfig<*>): Boolean {
        if(config !is CwtPropertyConfig) return false
        val dataType = config.keyExpression.type
        return dataType == CwtDataTypes.LocalisationParameter
    }
    
    fun completeParameters(localisation: ParadoxLocalisationProperty, result: CompletionResultSet) {
        val localisationName = localisation.name
        val localisationIcon = localisation.icon
        val parameterNames = getParameterNames(localisation)
        if(parameterNames.isNotEmpty()) {
            for(parameterName in parameterNames) {
                val parameter = ParadoxLocalisationParameterSupport.resolveParameter(localisation, parameterName) ?: continue
                val lookupElement = LookupElementBuilder.create(parameter, parameterName)
                    .withIcon(PlsIcons.Nodes.Parameter)
                    .withTypeText(localisationName, localisationIcon, true)
                result.addElement(lookupElement)
            }
        }
    }
}
