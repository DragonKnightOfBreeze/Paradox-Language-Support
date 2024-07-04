package icu.windea.pls.ep.parameter

import com.intellij.openapi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.model.expression.complex.nodes.*
import icu.windea.pls.script.psi.*

/**
 * 用于推断在脚本表达式中使用的参数的上下文规则，适用于部分简单的场合。
 */
class ParadoxBaseParameterInferredConfigProvider : ParadoxParameterInferredConfigProvider {
    override fun supports(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): Boolean {
        return parameterInfo.expressionElement != null && parameterInfo.isEntireExpression
    }
    
    override fun getContextConfigs(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): List<CwtMemberConfig<*>>? {
        val expressionElement = parameterInfo.expressionElement ?: return null
        val expressionContextConfigs = CwtConfigHandler.getConfigContext(expressionElement)?.getConfigs().orEmpty()
        val contextConfigs = doGetContextConfigsFromExpressionContextConfigs(expressionContextConfigs, parameterInfo)
        return contextConfigs
    }
    
    private fun doGetContextConfigsFromExpressionContextConfigs(expressionContextConfigs: List<CwtMemberConfig<*>>, parameterInfo: ParadoxParameterContextInfo.Parameter): List<CwtMemberConfig<*>>{
        if(expressionContextConfigs.isEmpty()) return emptyList()
        val expressionContextConfig = expressionContextConfigs.find { it.expression.type == CwtDataTypes.ParameterValue }
        if(expressionContextConfig != null) {
            //处理参数传递的情况
            if(expressionContextConfig !is CwtValueConfig) return emptyList()
            val argumentNameElement = parameterInfo.element?.parent?.castOrNull<ParadoxScriptValue>()?.propertyKey ?: return emptyList()
            val argumentNameConfig = expressionContextConfig.propertyConfig ?: return emptyList()
            val passingParameterElement = ParadoxParameterSupport.resolveArgument(argumentNameElement, null, argumentNameConfig) ?: return emptyList()
            val passingContextConfigs = ParadoxParameterHandler.getInferredContextConfigs(passingParameterElement)
            return passingContextConfigs
        }
        val finalConfigs = expressionContextConfigs.map { config ->
            when(config) {
                is CwtPropertyConfig -> config.delegated(CwtConfigManipulator.deepCopyConfigs(config), config.parentConfig)
                is CwtValueConfig -> config.delegated(CwtConfigManipulator.deepCopyConfigs(config), config.parentConfig)
            }
        }
        val configGroup = expressionContextConfigs.first().configGroup
        val contextConfig = CwtConfigManipulator.inlineAsValueConfig(null, finalConfigs, configGroup)
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
        return parameterInfo.expressionElement != null &&!parameterInfo.isEntireExpression
    }
    
    override fun getContextConfigs(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): List<CwtMemberConfig<*>>? {
        val expressionElement = parameterInfo.expressionElement ?: return null
        if(expressionElement.text.isLeftQuoted()) return null
        val expressionConfigs = parameterInfo.expressionConfigs
        val configs = expressionConfigs.mapNotNull { getConfigFromExpressionConfig(expressionElement, it, parameterInfo) }
        return configs
    }
    
    private fun getConfigFromExpressionConfig(expressionElement: ParadoxScriptStringExpressionElement, expressionConfig: CwtMemberConfig<*>, parameterInfo: ParadoxParameterContextInfo.Parameter): CwtValueConfig? {
        val configGroup = expressionConfig.configGroup
        val textRange = TextRange.create(0, expressionElement.text.length)
        val expression = ParadoxComplexExpression.resolve(expressionElement.text, textRange, configGroup, expressionConfig) ?: return null
        val rangeInExpressionElement = parameterInfo.rangeInExpressionElement
        var result: CwtValueConfig? = null
        expression.processAllNodes p@{ node ->
            if(node.rangeInExpression == rangeInExpressionElement) {
                result = getConfigFromNode(expressionElement, expressionConfig, node)
                if(result != null) return@p false
            }
            true
        }
        return result
    }
    
    private fun getConfigFromNode(expressionElement: ParadoxScriptStringExpressionElement, expressionConfig: CwtMemberConfig<*>, node: ParadoxComplexExpressionNode): CwtValueConfig? {
        val configGroup = expressionConfig.configGroup
        return when {
            node is ParadoxDataSourceNode -> {
                node.linkConfigs.firstNotNullOfOrNull { it.expression?.let { e -> CwtValueConfig.resolve(emptyPointer(), configGroup, e.expressionString) } }
            }
            node is ParadoxDynamicValueNode -> {
                node.configs.firstOrNull()?.let { it.expression?.let { e -> CwtValueConfig.resolve(emptyPointer(), configGroup, e.expressionString) } }
            }
            node is ParadoxScriptValueNode -> {
                node.config.let { it.expression?.let { e -> CwtValueConfig.resolve(emptyPointer(), configGroup, e.expressionString) } }
            }
            node is ParadoxScopeFieldNode -> {
                CwtValueConfig.resolve(emptyPointer(), configGroup, "scope_field")
            }
            node is ParadoxValueFieldNode -> {
                CwtValueConfig.resolve(emptyPointer(), configGroup, "value_field")
            }
            node is ParadoxScriptValueArgumentValueNode -> {
                val argumentNode = node.argumentNode ?: return null
                val passingParameterElement = ParadoxParameterSupport.resolveArgument(expressionElement, argumentNode.rangeInExpression, expressionConfig) ?: return null
                val passingContextConfigs = ParadoxParameterHandler.getInferredContextConfigs(passingParameterElement)
                val passingConfig = passingContextConfigs.singleOrNull()?.castOrNull<CwtValueConfig>()
                passingConfig
            }
            else -> null
        }
    }
}

/**
 * 用于推断不在脚本表达式中使用的参数的上下文规则。适用于参数在条件表达式以及内联数学块中使用的场合。
 */
class ParadoxNotInExpressionParameterInferredConfigProvider: ParadoxParameterInferredConfigProvider {
    override fun supports(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): Boolean {
        return parameterInfo.expressionElement == null
    }
    
    override fun getContextConfigs(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): List<CwtMemberConfig<*>>? {
        val configGroup = getConfigGroup(parameterContextInfo.project, parameterContextInfo.gameType)
        val element = parameterInfo.element
        val config = when {
            element is ParadoxConditionParameter -> CwtValueConfig.resolve(emptyPointer(), configGroup, "bool")
            element is ParadoxScriptInlineMathParameter -> CwtValueConfig.resolve(emptyPointer(), configGroup, "float")
            else -> null
        }
        return config?.toSingletonList()
    }
}