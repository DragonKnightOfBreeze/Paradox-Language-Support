package icu.windea.pls.lang.util

import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.isStatic
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.resolveElementWithConfig
import icu.windea.pls.core.ReadWriteAccess
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.light.ParadoxComplexEnumValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxMeshLocatorLightElement
import icu.windea.pls.lang.psi.light.ParadoxShaderEffectLightElement
import icu.windea.pls.lang.search.ParadoxComplexEnumValueSearch
import icu.windea.pls.lang.search.util.selector
import icu.windea.pls.lang.search.util.withSearchScopeType
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

object ParadoxResolutionManager {
    fun resolveEnumValue(element: ParadoxExpressionElement, expression: String, config: CwtConfig<*>): PsiElement? {
        resolveStaticEnumValue(expression, config)?.let { return it }
        resolveComplexEnumValue(element, expression, config)?.let { return it }
        return null
    }

    fun resolveStaticEnumValue(expression: String, config: CwtConfig<*>): PsiElement? {
        val dataExpression = config.configExpression ?: return null
        if (dataExpression.type != CwtDataTypes.EnumValue) return null
        val name = expression
        val enumName = dataExpression.value ?: return null
        val configGroup = config.configGroup
        val enumConfig = configGroup.enums[enumName] ?: return null
        val enumValueConfig = enumConfig.valueConfigMap.get(name) ?: return null
        val resolved = enumValueConfig.resolveElementWithConfig() ?: return null
        return resolved
    }

    fun resolveComplexEnumValue(element: ParadoxExpressionElement, expression: String, config: CwtConfig<*>): PsiElement? {
        val dataExpression = config.configExpression ?: return null
        if (dataExpression.type != CwtDataTypes.EnumValue) return null
        val name = expression
        val enumName = dataExpression.value ?: return null
        val configGroup = config.configGroup
        val complexEnumConfig = configGroup.complexEnums[enumName] ?: return null
        val project = configGroup.project
        val searchScopeType = complexEnumConfig.searchScopeType
        val selector = selector(project, element).complexEnumValue().withSearchScopeType(searchScopeType)
        val info = ParadoxComplexEnumValueSearch.search(name, enumName, selector).findFirst() ?: return null
        val readWriteAccess = ReadWriteAccess.Read // usage
        return ParadoxComplexEnumValueLightElement(element, info.name, info.enumName, readWriteAccess, info.gameType, project)
    }

    fun resolveDynamicValue(element: ParadoxExpressionElement, expression: String, config: CwtConfig<*>): PsiElement? {
        val dataExpression = config.configExpression ?: return null
        if (dataExpression.type !in CwtDataTypeSets.DynamicValue) return null
        val name = expression
        val configGroup = config.configGroup
        return ParadoxDynamicValueManager.resolveDynamicValue(element, name, dataExpression, configGroup)
    }

    fun resolveModifier(element: ParadoxExpressionElement, name: String, configGroup: CwtConfigGroup): PsiElement? {
        if (element !is ParadoxScriptStringExpressionElement) return null // NOTE 1.4.0 - unnecessary to support yet
        return ParadoxModifierManager.resolveModifier(name, element, configGroup)
    }

    fun resolveShaderEffect(element: ParadoxExpressionElement, expression: String, configGroup: CwtConfigGroup): PsiElement {
        val name = expression
        return ParadoxShaderEffectLightElement(element, name, configGroup.gameType, configGroup.project)
    }

    fun resolveMeshLocator(element: ParadoxExpressionElement, expression: String, configGroup: CwtConfigGroup): PsiElement {
        val name = expression
        return ParadoxMeshLocatorLightElement(element, name, configGroup.gameType, configGroup.project)
    }

    @Suppress("unused")
    fun resolveSystemScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val systemScopeConfig = configGroup.systemScopes[name] ?: return null
        val resolved = systemScopeConfig.resolveElementWithConfig() ?: return null
        return resolved
    }

    @Suppress("unused")
    fun resolveScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val linkConfig = configGroup.links[name]?.takeIf { it.type.forScope() && it.isStatic } ?: return null
        val resolved = linkConfig.resolveElementWithConfig() ?: return null
        return resolved
    }

    @Suppress("unused")
    fun resolveValueField(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val linkConfig = configGroup.links[name]?.takeIf { it.type.forValue() && it.isStatic } ?: return null
        val resolved = linkConfig.resolveElementWithConfig() ?: return null
        return resolved
    }

    @Suppress("unused")
    fun resolvePredefinedLocalisationScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val linkConfig = configGroup.localisationLinks[name] ?: return null
        val resolved = linkConfig.resolveElementWithConfig() ?: return null
        return resolved
    }

    @Suppress("unused")
    fun resolvePredefinedLocalisationCommand(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val commandConfig = configGroup.localisationCommands[name] ?: return null
        val resolved = commandConfig.resolveElementWithConfig() ?: return null
        return resolved
    }
}
