package icu.windea.pls.ep.resolve.parameter

import icu.windea.pls.PlsFacade
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.util.values.singletonList
import icu.windea.pls.core.util.values.to
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.resolve.ParadoxParameterService
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDynamicValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScopeLinkNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueArgumentValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxScriptValueNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxValueFieldNode
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionRecursiveVisitor
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.model.ParadoxParameterContextInfo
import icu.windea.pls.script.psi.ParadoxConditionParameter
import icu.windea.pls.script.psi.ParadoxScriptInlineMathParameter
import icu.windea.pls.script.psi.ParadoxScriptParameter
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.propertyKey

/**
 * 用于基于所在位置推断参数的上下文规则。
 */
class ParadoxDefaultExpressionParameterInferredConfigProvider : ParadoxParameterInferredConfigProvider {
    override fun supports(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): Boolean {
        return true
    }

    override fun getContextConfigs(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): List<CwtMemberConfig<*>>? {
        val configGroup = PlsFacade.getConfigGroup(parameterContextInfo.project, parameterContextInfo.gameType)
        val finalConfigs = getConfig(parameterInfo, configGroup)?.to?.singletonList() ?: return null
        val contextConfig = CwtConfigManipulator.inlineWithConfigs(null, finalConfigs, configGroup)
        return listOf(contextConfig)
    }

    private fun getConfig(parameterInfo: ParadoxParameterContextInfo.Parameter, configGroup: CwtConfigGroup): CwtMemberConfig<*>? {
        val element = parameterInfo.element ?: return null
        val parentElement = parameterInfo.parentElement ?: return null
        return when (element) {
            is ParadoxConditionParameter -> {
                CwtValueConfig.createMock(configGroup, "wildcard_scalar")
            }
            is ParadoxScriptParameter -> {
                if (parentElement.text.isParameterized(full = true)) return null
                CwtValueConfig.createMock(configGroup, "wildcard_scalar")
            }
            is ParadoxScriptInlineMathParameter -> {
                if (parentElement.text.isParameterized(full = true)) return CwtValueConfig.createMock(configGroup, "float")
                CwtValueConfig.createMock(configGroup, "wildcard_scalar")
            }
            else -> null
        }
    }
}

/**
 * 用于推断在脚本表达式中使用的参数的上下文规则，适用于部分简单的场合。
 */
class ParadoxBaseParameterInferredConfigProvider : ParadoxParameterInferredConfigProvider {
    override fun supports(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): Boolean {
        val parentElement = parameterInfo.parentElement
        if (parentElement !is ParadoxScriptStringExpressionElement) return false
        if (!parentElement.value.isParameterized(full = true)) return false
        return true
    }

    override fun getContextConfigs(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): List<CwtMemberConfig<*>>? {
        val expressionContextConfigs = ParadoxParameterManager.getExpressionContextConfigs(parameterInfo)
        if (expressionContextConfigs.isEmpty()) return null
        val contextConfigs = getContextConfigsFromExpressionContextConfigs(expressionContextConfigs, parameterInfo)
        if (contextConfigs.isNullOrEmpty()) return null
        return contextConfigs
    }

    private fun getContextConfigsFromExpressionContextConfigs(expressionContextConfigs: List<CwtMemberConfig<*>>, parameterInfo: ParadoxParameterContextInfo.Parameter): List<CwtMemberConfig<*>>? {
        val inlinedContextConfigs = expressionContextConfigs.map { config ->
            if (config is CwtPropertyConfig) {
                return@map CwtConfigManipulator.inlineSingleAlias(config) ?: config
            }
            config
        }
        val parentElement = parameterInfo.parentElement
        val configGroup = expressionContextConfigs.first().configGroup
        val passingConfig = inlinedContextConfigs.find { it.configExpression.type == CwtDataTypes.ParameterValue }
        if (passingConfig != null) {
            // 处理参数传递的情况
            if (passingConfig !is CwtValueConfig) return null
            val argumentNameElement = parentElement?.castOrNull<ParadoxScriptValue>()?.propertyKey ?: return null
            val argumentNameConfig = passingConfig.propertyConfig ?: return null
            val passingParameterElement = ParadoxParameterService.resolveArgument(argumentNameElement, null, argumentNameConfig) ?: return null
            val passingContextConfigs = ParadoxParameterManager.getInferredContextConfigs(passingParameterElement)
            return passingContextConfigs
        }
        val finalConfigs = inlinedContextConfigs.map { config ->
            if (config is CwtPropertyConfig && parentElement is ParadoxScriptPropertyKey) {
                return@map CwtValueConfig.createMock(configGroup, config.key)
            }
            val delegatedConfig = config.delegated(CwtConfigManipulator.deepCopyConfigs(config)).also { it.parentConfig = config.parentConfig }
            delegatedConfig.postOptimize() // 进行后续优化
            delegatedConfig
        }
        if (finalConfigs.isEmpty()) return emptyList()
        val contextConfig = CwtConfigManipulator.inlineWithConfigs(null, finalConfigs, configGroup)
        return listOf(contextConfig)
    }
}

/**
 * 用于推断在脚本表达式中使用的参数的上下文规则，适用于参数作为复杂表达式节点的场合。
 */
class ParadoxComplexExpressionNodeParameterInferredConfigProvider : ParadoxParameterInferredConfigProvider {
    // root.trigger:$PARAM$ -> alias_keys_field[trigger]
    // root.$PARAM$.owner -> scope_field
    // root.value:$PARAM$|K|V| -> <script_value>
    // root.value:some_script_value|K|$PARAM$| -> (from parameter K)

    override fun supports(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): Boolean {
        val parentElement = parameterInfo.parentElement
        if (parentElement !is ParadoxScriptStringExpressionElement) return false
        if (parentElement.value.isParameterized(full = true)) return false
        return true
    }

    override fun getContextConfigs(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): List<CwtMemberConfig<*>>? {
        val expressionConfigs = ParadoxParameterManager.getExpressionConfigs(parameterInfo)
        if (expressionConfigs.isEmpty()) return null
        val parentElement = parameterInfo.parentElement
        if (parentElement !is ParadoxScriptStringExpressionElement) return null
        val contextConfigs = expressionConfigs.mapNotNull { getContextConfigFromExpressionConfig(parentElement, it, parameterInfo) }
        return contextConfigs
    }

    private fun getContextConfigFromExpressionConfig(expressionElement: ParadoxScriptStringExpressionElement, expressionConfig: CwtMemberConfig<*>, parameterInfo: ParadoxParameterContextInfo.Parameter): CwtValueConfig? {
        val configGroup = expressionConfig.configGroup
        val value = expressionElement.value
        val expression = ParadoxComplexExpression.resolveByConfig(value, null, configGroup, expressionConfig) ?: return null
        val rangeInExpressionElement = parameterInfo.element?.textRangeInParent
        var result: List<CwtValueConfig>? = null
        expression.accept(object : ParadoxComplexExpressionRecursiveVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode): Boolean {
                if (node.rangeInExpression == rangeInExpressionElement) {
                    result = getConfigsFromNode(expressionElement, expressionConfig, node)
                    if (result.isNotNullOrEmpty()) return false
                }
                return super.visit(node)
            }
        })
        if (result.isNullOrEmpty()) return null
        return CwtConfigManipulator.inlineWithConfigs(null, result, configGroup)
    }

    private fun getConfigsFromNode(expressionElement: ParadoxScriptStringExpressionElement, expressionConfig: CwtMemberConfig<*>, node: ParadoxComplexExpressionNode): List<CwtValueConfig> {
        val configGroup = expressionConfig.configGroup
        return when {
            node is ParadoxDataSourceNode -> {
                node.linkConfigs.mapNotNull { it.configExpression?.let { e -> CwtValueConfig.createMock(configGroup, e.expressionString) } }
            }
            node is ParadoxDynamicValueNode -> {
                node.configs.mapNotNull { it.configExpression?.let { e -> CwtValueConfig.createMock(configGroup, e.expressionString) } }
            }
            node is ParadoxScriptValueNode -> {
                node.config.to.singletonList().mapNotNull { it.configExpression?.let { e -> CwtValueConfig.createMock(configGroup, e.expressionString) } }
            }
            node is ParadoxScopeLinkNode -> {
                CwtValueConfig.createMock(configGroup, "scope_field").to.singletonList()
            }
            node is ParadoxValueFieldNode -> {
                CwtValueConfig.createMock(configGroup, "value_field").to.singletonList()
            }
            node is ParadoxScriptValueArgumentValueNode -> {
                val argumentNode = node.argumentNode ?: return emptyList()
                val passingParameterElement = ParadoxParameterService.resolveArgument(expressionElement, argumentNode.rangeInExpression, expressionConfig) ?: return emptyList()
                val passingContextConfigs = ParadoxParameterManager.getInferredContextConfigs(passingParameterElement)
                val passingConfigs = passingContextConfigs.singleOrNull()?.configs?.filterIsInstance<CwtValueConfig>().orEmpty()
                passingConfigs
            }
            else -> emptyList()
        }
    }
}
