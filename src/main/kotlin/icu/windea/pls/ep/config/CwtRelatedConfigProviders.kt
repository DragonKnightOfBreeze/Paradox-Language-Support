package icu.windea.pls.ep.config

import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parents
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.inlineConfig
import icu.windea.pls.config.config.singleAliasConfig
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.complexEnums
import icu.windea.pls.config.configGroup.dynamicValueTypes
import icu.windea.pls.config.configGroup.enums
import icu.windea.pls.config.configGroup.extendedComplexEnumValues
import icu.windea.pls.config.configGroup.extendedDefinitions
import icu.windea.pls.config.configGroup.extendedDynamicValues
import icu.windea.pls.config.configGroup.extendedGameRules
import icu.windea.pls.config.configGroup.extendedInlineScripts
import icu.windea.pls.config.configGroup.extendedOnActions
import icu.windea.pls.config.configGroup.extendedParameters
import icu.windea.pls.config.configGroup.extendedScriptedVariables
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.findElementAt
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.isHeaderColumn
import icu.windea.pls.ep.resolve.modifier.modifierConfig
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.lang.match.matchesByPattern
import icu.windea.pls.lang.psi.ParadoxPsiFinder
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionVisitor
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxComplexEnumValueManager
import icu.windea.pls.lang.util.ParadoxCsvManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.lang.util.ParadoxModifierManager
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.isComplexExpression
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isExpression

class CwtBaseRelatedConfigProvider : CwtRelatedConfigProvider {
    override fun getRelatedConfigs(file: PsiFile, offset: Int): Collection<CwtConfig<*>> {
        // 适用于脚本文件中的表达式
        // 获取所有匹配的规则，不存在匹配的规则时，选用所有默认的规则（对于 propertyConfig 来说是匹配 key 的，对于 valueConfig 来说是所有）
        // 包括内联规则以及内联后的规则
        // 包括其他一些相关的规则

        val element = ParadoxPsiFinder.findScriptExpression(file, offset) ?: return emptySet()
        val configGroup = PlsFacade.getConfigGroup(file.project, selectGameType(file))

        val result = mutableSetOf<CwtConfig<*>>()

        // 尝试解析为复杂枚举值声明
        run r@{
            if (element !is ParadoxScriptStringExpressionElement) return@r
            val complexEnumValueInfo = ParadoxComplexEnumValueManager.getInfo(element) ?: return@r
            val complexEnumConfig = configGroup.complexEnums[complexEnumValueInfo.enumName] ?: return@r
            result += complexEnumConfig
        }

        // 基于所有匹配的规则
        run r@{
            val orDefault = element is ParadoxScriptPropertyKey
            val matchOptions = ParadoxMatchOptions.Default or ParadoxMatchOptions.AcceptDefinition
            val configs = ParadoxExpressionManager.getConfigs(element, orDefault, matchOptions)
            if (configs.isEmpty()) return@r
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
        // 适用于脚本文件与本地化文件中的复杂表达式（中的节点）

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

        val configGroup = PlsFacade.getConfigGroup(file.project, selectGameType(file))
        val textRange = element.textRange
        val finalOffset = offset - textRange.startOffset
        if (finalOffset < 0) return emptySet()
        val complexExpression = ParadoxComplexExpression.resolve(element, configGroup)
        if (complexExpression == null) return emptySet()

        val result = mutableListOf<CwtConfig<*>>()
        complexExpression.accept(object : ParadoxComplexExpressionVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                if (finalOffset in node.rangeInExpression) {
                    result.addAll(0, node.getRelatedConfigs())
                }
                return super.visit(node)
            }
        })
        return result.toSet()
    }
}

class CwtExtendedRelatedConfigProvider : CwtRelatedConfigProvider {
    override fun getRelatedConfigs(file: PsiFile, offset: Int): Collection<CwtConfig<*>> {
        // 适用于封装变量的名字与引用，定义的类型键、名字与引用，参数用法，以及脚本文件中的表达式
        // 包括其他一些相关的规则（扩展的规则 - definitions gameRules onActions parameters complexEnumValues dynamicValues）

        val result = mutableSetOf<CwtConfig<*>>()
        val configGroup = PlsFacade.getConfigGroup(file.project, selectGameType(file))

        run r0@{
            val element = ParadoxPsiFinder.findScriptedVariable(file, offset) { BY_NAME or BY_REFERENCE } ?: return@r0
            val name = element.name
            if (name.isNullOrEmpty()) return@r0
            if (name.isParameterized()) return@r0
            val config = configGroup.extendedScriptedVariables.findByPattern(name, element, configGroup)
            if (config != null) result += config
        }

        run r0@{
            val element = ParadoxPsiFinder.findDefinition(file, offset) { BY_NAME or BY_TYPE_KEY or BY_REFERENCE } ?: return@r0
            val definition = element
            val definitionInfo = definition.definitionInfo ?: return@r0
            val definitionName = definitionInfo.name
            if (definitionName.isEmpty()) return@r0
            if (definitionName.isParameterized()) return@r0
            run r1@{
                val extendedConfigs = configGroup.extendedDefinitions.findByPattern(definitionName, definition, configGroup).orEmpty()
                val matchedConfigs = extendedConfigs.filter { ParadoxDefinitionTypeExpression.resolve(it.type).matches(definitionInfo) }
                result += matchedConfigs
            }
            run r1@{
                if (definitionInfo.type != ParadoxDefinitionTypes.GameRule) return@r1
                val extendedConfig = configGroup.extendedGameRules.findByPattern(definitionName, element, configGroup)
                if (extendedConfig != null) result += extendedConfig
            }
            run r1@{
                if (definitionInfo.type != ParadoxDefinitionTypes.OnAction) return@r1
                val extendedConfig = configGroup.extendedOnActions.findByPattern(definitionName, element, configGroup)
                if (extendedConfig != null) result += extendedConfig
            }
        }

        run r0@{
            val element = file.findElementAt(offset) {
                it.parents(false).firstNotNullOfOrNull { p -> ParadoxParameterManager.getParameterElement(p) }
            } ?: return@r0
            val extendedConfigs = configGroup.extendedParameters.findByPattern(element.name, element, configGroup).orEmpty()
                .filterTo(result) { it.contextKey.matchesByPattern(element.contextKey, element, configGroup) }
            result += extendedConfigs
        }

        run r0@{
            val element = ParadoxPsiFinder.findScriptExpression(file, offset) ?: return@r0
            if (element !is ParadoxScriptStringExpressionElement) return@r0
            val name = element.name

            for (reference in element.references) {
                when {
                    ParadoxResolveConstraint.Parameter.canResolve(reference) -> {
                        val resolved = reference.resolve()?.castOrNull<ParadoxParameterElement>() ?: continue
                        val extendedConfigs = configGroup.extendedParameters.findByPattern(name, element, configGroup).orEmpty()
                            .filterTo(result) { it.contextKey.matchesByPattern(resolved.contextKey, element, configGroup) }
                        result += extendedConfigs
                    }
                    ParadoxResolveConstraint.ComplexEnumValue.canResolve(reference) -> {
                        val resolved = reference.resolve()?.castOrNull<ParadoxComplexEnumValueElement>() ?: continue
                        val extendedConfigs = configGroup.extendedComplexEnumValues[resolved.enumName] ?: continue
                        val extendedConfig = extendedConfigs.findByPattern(resolved.name, element, configGroup) ?: continue
                        result += extendedConfig
                    }
                    ParadoxResolveConstraint.DynamicValueStrictly.canResolve(reference) -> {
                        val resolved = reference.resolve()?.castOrNull<ParadoxDynamicValueElement>() ?: continue
                        for (type in resolved.dynamicValueTypes) {
                            val extendedConfigs = configGroup.extendedDynamicValues[type] ?: continue
                            val extendedConfig = extendedConfigs.findByPattern(resolved.name, element, configGroup) ?: continue
                            result += extendedConfig
                        }
                    }
                }
            }

            val orDefault = element is ParadoxScriptPropertyKey
            val matchOptions = ParadoxMatchOptions.Default or ParadoxMatchOptions.AcceptDefinition
            val configs = ParadoxExpressionManager.getConfigs(element, orDefault, matchOptions)
            for (config in configs) {
                val configExpression = config.configExpression
                if (configExpression == ParadoxInlineScriptManager.inlineScriptPathExpression) {
                    val extendedConfig = configGroup.extendedInlineScripts.findByPattern(name, element, configGroup) ?: continue
                    result += extendedConfig
                }
            }
        }

        return result
    }
}

class CwtColumnRelatedConfigProvider : CwtRelatedConfigProvider {
    override fun getRelatedConfigs(file: PsiFile, offset: Int): Collection<CwtConfig<*>> {
        // 适用于 CSV 文件中的某一列对应的表达式

        val element = ParadoxPsiFinder.findCsvExpression(file, offset) ?: return emptySet()
        if (element !is ParadoxCsvColumn) return emptySet()

        val columnConfig = ParadoxCsvManager.getColumnConfig(element) ?: return emptySet()
        val result = mutableSetOf<CwtConfig<*>>()
        if (element.isHeaderColumn()) {
            result += columnConfig
        } else {
            val config = columnConfig.valueConfig
            if (config != null) result += config
        }
        return result
    }
}
