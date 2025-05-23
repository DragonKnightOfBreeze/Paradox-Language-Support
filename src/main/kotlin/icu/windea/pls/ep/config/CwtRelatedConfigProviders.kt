package icu.windea.pls.ep.config

import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.modifier.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.expression.complex.nodes.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Options
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.psi.*

class CwtBaseRelatedConfigProvider : CwtRelatedConfigProvider {
    override fun getRelatedConfigs(file: PsiFile, offset: Int): Collection<CwtConfig<*>> {
        //适用于脚本文件中的表达式
        //获取所有匹配的CWT规则，不存在匹配的CWT规则时，选用所有默认的CWT规则（对于propertyConfig来说是匹配key的，对于valueConfig来说是所有）
        //包括内联规则以及内联后的规则
        //包括其他一些相关的规则

        val result = mutableSetOf<CwtConfig<*>>()
        val configGroup = getConfigGroup(file.project, selectGameType(file))

        run r0@{
            val element = ParadoxPsiManager.findScriptExpression(file, offset) ?: return@r0

            val orDefault = element is ParadoxScriptPropertyKey
            val matchOptions = Options.Default or Options.AcceptDefinition
            val configs = ParadoxExpressionManager.getConfigs(element, orDefault, matchOptions)
            for (config in configs) {
                result += config
                when {
                    config is CwtPropertyConfig -> {
                        config.inlineConfig?.also { result += it }
                        config.aliasConfig?.also { result += it }
                        config.singleAliasConfig?.also { result += it }
                    }
                    config is CwtValueConfig -> {
                        config.propertyConfig?.singleAliasConfig?.also { result += it }
                    }
                }
                if (element !is ParadoxScriptStringExpressionElement) continue
                val name = element.value
                val configExpression = config.configExpression
                when {
                    configExpression.type in CwtDataTypeGroups.DynamicValue -> {
                        val type = configExpression.value
                        if (type != null) {
                            configGroup.dynamicValueTypes[type]?.valueConfigMap?.get(name)?.also { result += it }
                        }
                    }
                    configExpression.type == CwtDataTypes.EnumValue -> {
                        val enumName = configExpression.value
                        if (enumName != null) {
                            configGroup.enums[enumName]?.valueConfigMap?.get(name)?.also { result += it }
                            configGroup.complexEnums[enumName]?.also { result += it }
                        }
                    }
                    configExpression.type == CwtDataTypes.Modifier -> {
                        val modifierElement = ParadoxModifierManager.resolveModifier(name, element, configGroup)
                        modifierElement?.modifierConfig?.also { result += it }
                    }
                }
            }
        }

        return result
    }
}

class CwtInComplexExpressionRelatedConfigProvider : CwtRelatedConfigProvider {
    override fun getRelatedConfigs(file: PsiFile, offset: Int): Collection<CwtConfig<*>> {
        //适用于脚本文件与本地化文件中的复杂表达式（中的节点）

        val element = when (file) {
            is ParadoxScriptFile -> {
                file.findElementAt(offset) {
                    it.parentOfType<ParadoxScriptExpressionElement>(false)
                }?.takeIf { it.isExpression() }
            }
            is ParadoxLocalisationFile -> {
                file.findElementAt(offset) {
                    it.parentOfType<ParadoxLocalisationExpressionElement>(false)
                }?.takeIf { it.isComplexExpression() }
            }
            else -> null
        }
        if (element == null) return emptySet()

        val configGroup = getConfigGroup(file.project, selectGameType(file))
        val textRange = element.textRange
        val finalOffset = offset - textRange.startOffset
        if (finalOffset < 0) return emptySet()
        val complexExpression = ParadoxComplexExpression.resolve(element, configGroup)
        if (complexExpression == null) return emptySet()

        val result = mutableListOf<CwtConfig<*>>()
        complexExpression.accept(object : ParadoxComplexExpressionVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode, parentNode: ParadoxComplexExpressionNode?): Boolean {
                if(finalOffset in node.rangeInExpression) {
                    result.addAll(0, node.getRelatedConfigs())
                }
                return super.visit(node, parentNode)
            }
        })
        return result.toSet()
    }
}

class CwtExtendedRelatedConfigProvider : CwtRelatedConfigProvider {
    override fun getRelatedConfigs(file: PsiFile, offset: Int): Collection<CwtConfig<*>> {
        //适用于封装变量的名字与引用，定义的顶层键、名字与引用，参数用法，以及脚本文件中的表达式
        //包括其他一些相关的规则（扩展的规则 - definitions gameRules onActions parameters complexEnumValues dynamicValues）

        val result = mutableSetOf<CwtConfig<*>>()
        val configGroup = getConfigGroup(file.project, selectGameType(file))

        run r0@{
            val findOptions = ParadoxPsiManager.FindScriptedVariableOptions.run { BY_NAME or BY_REFERENCE }
            val element = ParadoxPsiManager.findScriptVariable(file, offset, findOptions) ?: return@r0
            val name = element.name
            if (name.isNullOrEmpty()) return@r0
            if (name.isParameterized()) return@r0
            val config = configGroup.extendedScriptedVariables.findFromPattern(name, element, configGroup)
            if (config != null) result += config
        }

        run r0@{
            val findOptions = ParadoxPsiManager.FindDefinitionOptions.run { BY_NAME or BY_ROOT_KEY or BY_REFERENCE }
            val element = ParadoxPsiManager.findDefinition(file, offset, findOptions) ?: return@r0
            val definition = element
            val definitionInfo = definition.definitionInfo ?: return@r0
            val definitionName = definitionInfo.name
            if (definitionName.isEmpty()) return@r0
            if (definitionName.isParameterized()) return@r0
            run r1@{
                val extendedConfigs = configGroup.extendedDefinitions.findFromPattern(definitionName, definition, configGroup).orEmpty()
                val matchedConfigs = extendedConfigs.filter { ParadoxDefinitionTypeExpression.resolve(it.type).matches(definitionInfo) }
                result += matchedConfigs
            }
            run r1@{
                if (definitionInfo.type != ParadoxDefinitionTypes.GameRule) return@r1
                val extendedConfig = configGroup.extendedGameRules.findFromPattern(definitionName, element, configGroup)
                if (extendedConfig != null) result += extendedConfig
            }
            run r1@{
                if (definitionInfo.type != ParadoxDefinitionTypes.OnAction) return@r1
                val extendedConfig = configGroup.extendedOnActions.findFromPattern(definitionName, element, configGroup)
                if (extendedConfig != null) result += extendedConfig
            }
        }

        run r0@{
            val element = file.findElementAt(offset) {
                it.parents(false).firstNotNullOfOrNull { p -> ParadoxParameterManager.getParameterElement(p) }
            } ?: return@r0
            val extendedConfigs = configGroup.extendedParameters.findFromPattern(element.name, element, configGroup).orEmpty()
                .filterTo(result) { it.contextKey.matchFromPattern(element.contextKey, element, configGroup) }
            result += extendedConfigs
        }

        run r0@{
            val element = ParadoxPsiManager.findScriptExpression(file, offset) ?: return@r0
            if (element !is ParadoxScriptStringExpressionElement) return@r0
            val name = element.name

            for (reference in element.references) {
                when {
                    ParadoxResolveConstraint.Parameter.canResolve(reference) -> {
                        val resolved = reference.resolve()?.castOrNull<ParadoxParameterElement>() ?: continue
                        val extendedConfigs = configGroup.extendedParameters.findFromPattern(name, element, configGroup).orEmpty()
                            .filterTo(result) { it.contextKey.matchFromPattern(resolved.contextKey, element, configGroup) }
                        result += extendedConfigs
                    }
                    ParadoxResolveConstraint.ComplexEnumValue.canResolve(reference) -> {
                        val resolved = reference.resolve()?.castOrNull<ParadoxComplexEnumValueElement>() ?: continue
                        val extendedConfigs = configGroup.extendedComplexEnumValues[resolved.enumName] ?: continue
                        val extendedConfig = extendedConfigs.findFromPattern(resolved.name, element, configGroup) ?: continue
                        result += extendedConfig
                    }
                    ParadoxResolveConstraint.DynamicValueStrictly.canResolve(reference) -> {
                        val resolved = reference.resolve()?.castOrNull<ParadoxDynamicValueElement>() ?: continue
                        for (type in resolved.dynamicValueTypes) {
                            val extendedConfigs = configGroup.extendedDynamicValues[type] ?: continue
                            val extendedConfig = extendedConfigs.findFromPattern(resolved.name, element, configGroup) ?: continue
                            result += extendedConfig
                        }
                    }
                }
            }

            val orDefault = element is ParadoxScriptPropertyKey
            val matchOptions = Options.Default or Options.AcceptDefinition
            val configs = ParadoxExpressionManager.getConfigs(element, orDefault, matchOptions)
            for (config in configs) {
                val configExpression = config.configExpression
                when {
                    configExpression.expressionString == ParadoxInlineScriptManager.inlineScriptPathExpressionString -> {
                        val extendedConfig = configGroup.extendedInlineScripts.findFromPattern(name, element, configGroup) ?: continue
                        result += extendedConfig
                    }
                }
            }
        }

        return result
    }
}
