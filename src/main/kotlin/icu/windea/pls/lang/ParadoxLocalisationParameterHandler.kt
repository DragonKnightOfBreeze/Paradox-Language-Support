package icu.windea.pls.lang

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.application.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.scope.*
import icu.windea.pls.lang.hierarchy.impl.*
import icu.windea.pls.lang.parameter.*
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
    
    private fun doGetParametersFromDefinitionHierarchyIndex(element: ParadoxLocalisationProperty): Set<String> {
        val project = element.project
        val gameType = selectGameType(element) ?: return emptySet()
        val searchScope = runReadAction { ParadoxSearchScope.fromElement(element) }
            ?.withFileTypes(ParadoxScriptFileType)
            ?: return emptySet()
        val targetLocalisationName = element.name
        val result = mutableSetOf<String>().synced()
        ParadoxDefinitionHierarchyHandler.processLocalisationParameters(gameType, project, searchScope) p@{ _, infos ->
            infos.forEachFast { info ->
                val localisationName = info.getUserData(ParadoxLocalisationParameterDefinitionHierarchySupport.localisationNameKey)
                if(localisationName == targetLocalisationName) result.add(info.expression)
            }
            true
        }
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
    
    fun completeParameters(localisation: ParadoxLocalisationProperty, result: CompletionResultSet) {
        val localisationName = localisation.name
        val localisationIcon = localisation.icon
        val parameterNames = getParameterNames(localisation)
        if(parameterNames.isNotEmpty()) {
            for(parameterName in parameterNames) {
                val parameter = ParadoxLocalisationParameterSupport.resolveParameter(localisation, parameterName) ?: continue
                val lookupElement = LookupElementBuilder.create(parameter, parameterName)
                    .withIcon(PlsIcons.Parameter)
                    .withTypeText(localisationName, localisationIcon, true)
                result.addElement(lookupElement)
            }
        }
    }
}