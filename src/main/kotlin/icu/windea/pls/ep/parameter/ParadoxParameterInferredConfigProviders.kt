package icu.windea.pls.ep.parameter

import com.intellij.openapi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.expression.complex.nodes.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于基于所在位置推断参数的上下文规则。
 */
class ParadoxDefaultExpressionParameterInferredConfigProvider : ParadoxParameterInferredConfigProvider {
    override fun supports(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): Boolean {
        return true
    }

    override fun getContextConfigs(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): List<CwtMemberConfig<*>>? {
        val configGroup = getConfigGroup(parameterContextInfo.project, parameterContextInfo.gameType)
        val finalConfigs = getConfig(parameterInfo, configGroup)?.toSingletonList() ?: return null
        val contextConfig = CwtConfigManipulator.inlineWithConfigs(null, finalConfigs, configGroup)
        return listOf(contextConfig)
    }

    private fun getConfig(parameterInfo: ParadoxParameterContextInfo.Parameter, configGroup: CwtConfigGroup): CwtMemberConfig<*>? {
        val element = parameterInfo.element ?: return null
        val parentElement = parameterInfo.parentElement ?: return null
        when {
            element is ParadoxConditionParameter -> {
                return CwtValueConfig.resolve(emptyPointer(), configGroup, "scalar") //bool-like
            }
            element is ParadoxScriptParameter -> {
                if (parentElement.text.isParameterized(full = true)) return null
                return CwtValueConfig.resolve(emptyPointer(), configGroup, "scalar")
            }
            element is ParadoxScriptInlineMathParameter -> {
                if (parentElement.text.isParameterized(full = true)) return CwtValueConfig.resolve(emptyPointer(), configGroup, "float")
                return CwtValueConfig.resolve(emptyPointer(), configGroup, "scalar")
            }
            else -> return null
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
        val expressionContextConfigs = parameterInfo.expressionContextConfigs
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
            //处理参数传递的情况
            if (passingConfig !is CwtValueConfig) return null
            val argumentNameElement = parentElement?.castOrNull<ParadoxScriptValue>()?.propertyKey ?: return null
            val argumentNameConfig = passingConfig.propertyConfig ?: return null
            val passingParameterElement = ParadoxParameterSupport.resolveArgument(argumentNameElement, null, argumentNameConfig) ?: return null
            val passingContextConfigs = ParadoxParameterManager.getInferredContextConfigs(passingParameterElement)
            return passingContextConfigs
        }
        val finalConfigs = inlinedContextConfigs.map { config ->
            if (config is CwtPropertyConfig && parentElement is ParadoxScriptPropertyKey) {
                return@map CwtValueConfig.resolve(emptyPointer(), configGroup, config.key)
            }
            when (config) {
                is CwtPropertyConfig -> config.delegated(CwtConfigManipulator.deepCopyConfigs(config), config.parentConfig)
                is CwtValueConfig -> config.delegated(CwtConfigManipulator.deepCopyConfigs(config), config.parentConfig)
            }
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
    //root.trigger:$PARAM$ -> alias_keys_field[trigger]
    //root.$PARAM$.owner -> scope_field
    //root.value:$PARAM$|K|V| -> <script_value>
    //root.value:some_script_value|K|$PARAM$| -> (from parameter K)

    override fun supports(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): Boolean {
        val parentElement = parameterInfo.parentElement
        if (parentElement !is ParadoxScriptStringExpressionElement) return false
        if (parentElement.value.isParameterized(full = true)) return false
        return true
    }

    override fun getContextConfigs(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): List<CwtMemberConfig<*>>? {
        val expressionConfigs = parameterInfo.expressionConfigs
        if (expressionConfigs.isEmpty()) return null
        val parentElement = parameterInfo.parentElement
        if (parentElement !is ParadoxScriptStringExpressionElement) return null
        val contextConfigs = expressionConfigs.mapNotNull { getContextConfigFromExpressionConfig(parentElement, it, parameterInfo) }
        return contextConfigs
    }

    private fun getContextConfigFromExpressionConfig(expressionElement: ParadoxScriptStringExpressionElement, expressionConfig: CwtMemberConfig<*>, parameterInfo: ParadoxParameterContextInfo.Parameter): CwtValueConfig? {
        val configGroup = expressionConfig.configGroup
        val value = expressionElement.value
        val textRange = TextRange.create(0, value.length)
        val expression = ParadoxComplexExpression.resolveByConfig(value, textRange, configGroup, expressionConfig) ?: return null
        val rangeInExpressionElement = parameterInfo.element?.textRangeInParent
        var result: List<CwtValueConfig>? = null
        expression.accept(object: ParadoxComplexExpressionVisitor() {
            override fun visit(node: ParadoxComplexExpressionNode, parentNode: ParadoxComplexExpressionNode?): Boolean {
                if (node.rangeInExpression == rangeInExpressionElement) {
                    result = getConfigsFromNode(expressionElement, expressionConfig, node)
                    if (result.isNotNullOrEmpty()) return false
                }
                return super.visit(node, parentNode)
            }
        })
        if (result.isNullOrEmpty()) return null
        return CwtConfigManipulator.inlineWithConfigs(null, result, configGroup)
    }

    private fun getConfigsFromNode(expressionElement: ParadoxScriptStringExpressionElement, expressionConfig: CwtMemberConfig<*>, node: ParadoxComplexExpressionNode): List<CwtValueConfig> {
        val configGroup = expressionConfig.configGroup
        return when {
            node is ParadoxDataSourceNode -> {
                node.linkConfigs.mapNotNull { it.configExpression?.let { e -> CwtValueConfig.resolve(emptyPointer(), configGroup, e.expressionString) } }
            }
            node is ParadoxDynamicValueNode -> {
                node.configs.mapNotNull { it.configExpression?.let { e -> CwtValueConfig.resolve(emptyPointer(), configGroup, e.expressionString) } }
            }
            node is ParadoxScriptValueNode -> {
                node.config.toSingletonList().mapNotNull { it.configExpression?.let { e -> CwtValueConfig.resolve(emptyPointer(), configGroup, e.expressionString) } }
            }
            node is ParadoxScopeLinkNode -> {
                CwtValueConfig.resolve(emptyPointer(), configGroup, "scope_field").toSingletonList()
            }
            node is ParadoxValueFieldNode -> {
                CwtValueConfig.resolve(emptyPointer(), configGroup, "value_field").toSingletonList()
            }
            node is ParadoxScriptValueArgumentValueNode -> {
                val argumentNode = node.argumentNode ?: return emptyList()
                val passingParameterElement = ParadoxParameterSupport.resolveArgument(expressionElement, argumentNode.rangeInExpression, expressionConfig) ?: return emptyList()
                val passingContextConfigs = ParadoxParameterManager.getInferredContextConfigs(passingParameterElement)
                val passingConfigs = passingContextConfigs.singleOrNull()?.configs?.filterIsInstance<CwtValueConfig>().orEmpty()
                passingConfigs
            }
            else -> emptyList()
        }
    }
}
