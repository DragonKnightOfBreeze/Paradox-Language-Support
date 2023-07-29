package icu.windea.pls.lang.parameter.impl

import com.intellij.openapi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.script.psi.*

class ParadoxComplexExpressionNodeInferredConfigProvider : ParadoxParameterInferredConfigProvider {
    //root.trigger:$PARAM$ -> alias_keys_field[trigger]
    //root.$PARAM$.owner -> scope_field (expression node)
    //root.value:$PARAM$|K|V| -> <script_value>
    //root.value:some_script_value|K|$PARAM$| -> from parameter K
    
    override fun supports(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): Boolean {
        //要求不整个作为脚本表达式
        return !parameterInfo.isEntireExpression
    }
    
    override fun getConfig(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): CwtValueConfig? {
        val expressionElement = parameterInfo.expressionElement ?: return null
        if(expressionElement.text.isLeftQuoted()) return null
        val expressionConfigs = parameterInfo.expressionConfigs
        val config = expressionConfigs.firstNotNullOfOrNull { getConfigFromExpressionConfig(expressionElement, it, parameterInfo) }
        return config
    }
    
    override fun getContextConfigs(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): List<CwtMemberConfig<*>>? {
        val expressionElement = parameterInfo.expressionElement ?: return null
        if(expressionElement.text.isLeftQuoted()) return null
        val expressionConfigs = parameterInfo.expressionConfigs
        val configs = expressionConfigs.mapNotNull { getConfigFromExpressionConfig(expressionElement, it, parameterInfo) }
        if(configs.isEmpty()) return null
        val containerConfig = CwtValueConfig.resolve(
            pointer = emptyPointer(),
            info = configs.first().info,
            value = PlsConstants.blockFolder,
            valueTypeId = CwtType.Block.id,
            configs = configs
        )
        return listOf(containerConfig)
    }
    
    private fun getConfigFromExpressionConfig(expressionElement: ParadoxScriptStringExpressionElement, expressionConfig: CwtMemberConfig<*>, parameterInfo: ParadoxParameterContextInfo.Parameter): CwtValueConfig? {
        val configGroup = expressionConfig.info.configGroup
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
    
    private fun getConfigFromNode(expressionElement: ParadoxScriptStringExpressionElement, expressionConfig: CwtMemberConfig<*>, node: ParadoxExpressionNode): CwtValueConfig? {
        return when {
            node is ParadoxDataExpressionNode -> {
                node.linkConfigs.firstNotNullOfOrNull { it.expression?.let { e -> CwtValueConfig.resolve(emptyPointer(), it.info, e.expressionString) } }
            }
            node is ParadoxValueSetValueExpressionNode -> {
                node.configs.firstOrNull()?.let { it.expression?.let { e -> CwtValueConfig.resolve(emptyPointer(), it.info, e.expressionString) } }
            }
            node is ParadoxScriptValueExpressionNode -> {
                node.config.let { it.expression?.let { e -> CwtValueConfig.resolve(emptyPointer(), it.info, e.expressionString) } }
            }
            node is ParadoxScopeFieldExpressionNode -> {
                expressionConfig.let { CwtValueConfig.resolve(emptyPointer(), it.info, "scope_field") } //scope field node
            }
            node is ParadoxValueFieldExpressionNode -> {
                expressionConfig.let { CwtValueConfig.resolve(emptyPointer(), it.info, "value_field") } //value field node 
            }
            node is ParadoxScriptValueArgumentValueExpressionNode -> {
                val argumentNode = node.argumentNode ?: return null
                val passingConfig = withRecursionGuard("icu.windea.pls.lang.parameter.ParadoxParameterInferredConfigProvider.getConfigFromNode") a1@{
                    val passingParameterElement = ParadoxParameterSupport.resolveArgument(expressionElement, argumentNode.rangeInExpression, expressionConfig) ?: return null
                    withCheckRecursion(passingParameterElement.contextKey) a2@{
                        ParadoxParameterHandler.getInferredConfig(passingParameterElement)
                    }
                }
                passingConfig
            }
            else -> null
        }
    }
}