package icu.windea.pls.lang.util

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.PlsIcons
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.core.collections.synced
import icu.windea.pls.core.icon
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.processAsync
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.psi.select.*
import icu.windea.pls.lang.resolve.ParadoxLocalisationParameterService
import icu.windea.pls.lang.search.ParadoxLocalisationParameterSearch
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.propertyValue

@Suppress("UNUSED_PARAMETER")
object ParadoxLocalisationParameterManager {
    object Keys : KeyRegistry() {
        val cachedParameterNames by registerKey<CachedValue<Set<String>>>(Keys)
    }

    fun getParameterNames(element: ParadoxLocalisationProperty): Set<String> {
        return doGetParameterNamesFromCache(element)
    }

    private fun doGetParameterNamesFromCache(element: ParadoxLocalisationProperty): Set<String> {
        // invalidated on element modification or ScriptFileTracker
        return CachedValuesManager.getCachedValue(element, Keys.cachedParameterNames) {
            val value = doGetParameters(element)
            val trackers = with(ParadoxModificationTrackers) {
                listOf(element, ScriptFile)
            }
            value.withDependencyItems(trackers)
        }
    }

    private fun doGetParameters(element: ParadoxLocalisationProperty): Set<String> {
        val targetLocalisationName = element.name
        val result = mutableSetOf<String>().synced()
        val selector = selector(element.project, element).localisationParameter()
        ParadoxLocalisationParameterSearch.search(null, targetLocalisationName, selector).processAsync p@{ info ->
            result.add(info.name)
            true
        }
        return result
    }

    fun getLocalisationReferenceElement(element: ParadoxScriptExpressionElement, config: CwtMemberConfig<*>): ParadoxScriptString? {
        if (config !is CwtPropertyConfig || config.configExpression.type != CwtDataTypes.LocalisationParameter) return null
        val localisationReferencePropertyElement = findLocalisationPropertyFromParameterProperty(element, config)
        return localisationReferencePropertyElement?.propertyValue()
    }

    private fun findParameterPropertiesFromLocalisationProperty(element: ParadoxScriptExpressionElement, config: CwtPropertyConfig): List<ParadoxScriptProperty> {
        val configToUse = config.parentConfig?.configs?.firstNotNullOfOrNull { c -> c.configs?.find { isParameterConfig(element, it) } }
        if (configToUse == null) return emptyList()
        val context = selectScope { element.parentOfKey(fromBlock = true).asProperty() } ?: return emptyList()
        val result = context.properties().flatMap { it.properties() }.filter { isMatchedProperty(it, configToUse) }.toList()
        return result
    }

    private fun findLocalisationPropertyFromParameterProperty(element: ParadoxScriptExpressionElement, config: CwtPropertyConfig): ParadoxScriptProperty? {
        val configToUse = config.parentConfig?.parentConfig?.configs?.find { isLocalisationConfig(element, it) }
        if (configToUse == null) return null
        val context = selectScope { element.parentOfKey(fromBlock = true)?.parentOfKey(fromBlock = true).asProperty() } ?: return null
        val result = context.properties().find { isMatchedProperty(it, configToUse) }
        return result
    }

    private fun isMatchedProperty(element: PsiElement, config: CwtMemberConfig<*>): Boolean {
        if (element is ParadoxScriptProperty) {
            val configs = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(acceptDefinition = true))
            if (configs.any { it.pointer == config.pointer }) {
                return true
            }
        }
        return false
    }

    private fun isLocalisationConfig(element: PsiElement, config: CwtMemberConfig<*>): Boolean {
        if (config !is CwtPropertyConfig) return false
        val dataType = config.valueExpression.type
        return dataType == CwtDataTypes.Localisation || (dataType == CwtDataTypes.InlineLocalisation && !element.text.isLeftQuoted())
    }

    private fun isParameterConfig(element: PsiElement, config: CwtMemberConfig<*>): Boolean {
        if (config !is CwtPropertyConfig) return false
        val dataType = config.keyExpression.type
        return dataType == CwtDataTypes.LocalisationParameter
    }

    fun completeParameters(localisation: ParadoxLocalisationProperty, result: CompletionResultSet) {
        val localisationName = localisation.name
        val localisationIcon = localisation.icon
        val parameterNames = getParameterNames(localisation)
        if (parameterNames.isNotEmpty()) {
            for (parameterName in parameterNames) {
                val parameter = ParadoxLocalisationParameterService.resolveParameter(localisation, parameterName) ?: continue
                val lookupElement = LookupElementBuilder.create(parameter, parameterName)
                    .withIcon(PlsIcons.Nodes.Parameter)
                    .withTypeText(localisationName, localisationIcon, true)
                result.addElement(lookupElement)
            }
        }
    }
}
