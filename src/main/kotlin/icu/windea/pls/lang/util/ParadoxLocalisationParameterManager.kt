package icu.windea.pls.lang.util

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.isSamePointer
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
import icu.windea.pls.lang.resolve.ParadoxLocalisationParameterService
import icu.windea.pls.lang.search.ParadoxLocalisationParameterSearch
import icu.windea.pls.lang.select.selectScope
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
        return getParameterNamesFromCache(element)
    }

    private fun getParameterNamesFromCache(element: ParadoxLocalisationProperty): Set<String> {
        return CachedValuesManager.getCachedValue(element, Keys.cachedParameterNames) {
            ProgressManager.checkCanceled()
            val value = resolveParameters(element)
            val dependencies = with(ParadoxModificationTrackers) { listOf(element, ScriptFile) }
            value.withDependencyItems(dependencies)
        }
    }

    private fun resolveParameters(element: ParadoxLocalisationProperty): Set<String> {
        val targetLocalisationName = element.name
        val result = mutableSetOf<String>().synced()
        val selector = ParadoxLocalisationParameterSearch.selector(element.project, element)
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
        // `__` - caret position
        // `<container> = { description = __<loc> description_parameters = { <param> = <value> } }`
        // -> `<container> = { description = <loc> __description_parameters = { <param> = <value> } }`

        val configToUse = config.parentConfig?.configs?.firstNotNullOfOrNull { c -> c.configs?.find { isParameterConfig(element, it) } } ?: return emptyList()
        val containerElement = selectScope { element.queryParentBy("*/*").asProperty() } ?: return emptyList()
        return selectScope { containerElement.queryBy("*/*").asProperty().filter { isMatchedProperty(it, configToUse) }.all() }
    }

    private fun findLocalisationPropertyFromParameterProperty(element: ParadoxScriptExpressionElement, config: CwtPropertyConfig): ParadoxScriptProperty? {
        // `__` - caret position
        // `<container> = { description = <loc> description_parameters = { __<param> = <value> } }`
        // -> `<container> = { description = <loc> __description_parameters = { <param> = <value> } }`

        val configToUse = config.parentConfig?.parentConfig?.configs?.find { isLocalisationConfig(element, it) } ?: return null
        val containerElement = selectScope { element.queryParentBy("*/*/*").asProperty() } ?: return null
        return selectScope { containerElement.queryBy("*").asProperty().filter { isMatchedProperty(it, configToUse) }.one() }
    }

    private fun isMatchedProperty(element: ParadoxScriptProperty, config: CwtMemberConfig<*>): Boolean {
        val configs = ParadoxConfigManager.getConfigs(element, ParadoxMatchOptions(forDeclarationRoot = true))
        return configs.any { it isSamePointer config }
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
                    .withIcon(ChronicleIcons.Nodes.Parameter)
                    .withTypeText(localisationName, localisationIcon, true)
                result.addElement(lookupElement)
            }
        }
    }
}
