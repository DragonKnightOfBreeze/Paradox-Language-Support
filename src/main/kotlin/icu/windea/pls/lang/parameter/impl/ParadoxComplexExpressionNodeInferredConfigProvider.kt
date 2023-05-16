package icu.windea.pls.lang.parameter.impl

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.script.psi.*

class ParadoxComplexExpressionNodeInferredConfigProvider : ParadoxParameterInferredConfigProvider {
    //root.trigger:$PARAM$ -> alias_keys_field[trigger]
    //root.$PARAM$.owner -> scope_field (expression node)
    //root.value:$PARAM$|K|V| -> <script_value>
    //root.value:some_script_value|K|$PARAM$| -> from parameter K
    
    override fun getConfig(parameterInfo: ParadoxParameterInfo, parameterContextInfo: ParadoxParameterContextInfo): CwtValueConfig? {
        if(parameterInfo.isEntireExpression) return null //要求不整个作为脚本表达式
        val expressionElement = parameterInfo.expressionElement
        val expressionText = expressionElement?.text ?: return null
        if(expressionText.isLeftQuoted()) return null
        val expressionConfigs = parameterInfo.expressionConfigs
        val expressionConfig = expressionConfigs.firstOrNull() ?: return null
        val configExpression = expressionConfig.expression
        val configGroup = expressionConfig.info.configGroup
        val expression = when {
            configExpression.type.isValueSetValueType() -> {
                val textRange = TextRange.create(0, expressionText.length)
                ParadoxValueSetValueExpression.resolve(expressionText, textRange, expressionConfig, configGroup)
            }
            expressionConfig.expression.type.isScopeFieldType() -> {
                val textRange = TextRange.create(0, expressionText.length)
                ParadoxScopeFieldExpression.resolve(expressionText, textRange, configGroup)
            }
            expressionConfig.expression.type.isValueFieldType() -> {
                val textRange = TextRange.create(0, expressionText.length)
                ParadoxValueFieldExpression.resolve(expressionText, textRange, configGroup)
            }
            expressionConfig.expression.type.isVariableFieldType() -> {
                val textRange = TextRange.create(0, expressionText.length)
                ParadoxVariableFieldExpression.resolve(expressionText, textRange, configGroup)
            }
            else -> return null
        }
        if(expression == null) return null
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
    
    private fun getConfigFromNode(expressionElement: ParadoxScriptStringExpressionElement, expressionConfig: CwtDataConfig<*>, node: ParadoxExpressionNode): CwtValueConfig? {
        return when {
            node is ParadoxDataExpressionNode -> {
                node.linkConfigs.firstNotNullOfOrNull { it.expression?.let { e -> CwtValueConfig(emptyPointer(), it.info, e.expressionString) } }
            }
            node is ParadoxValueSetValueExpressionNode -> {
                node.configs.firstOrNull()?.let { it.expression?.let { e -> CwtValueConfig(emptyPointer(), it.info, e.expressionString) } }
            }
            node is ParadoxScriptValueExpressionNode -> {
                node.config.let { it.expression?.let { e -> CwtValueConfig(emptyPointer(), it.info, e.expressionString) } }
            }
            node is ParadoxScopeFieldExpressionNode -> {
                expressionConfig.let { CwtValueConfig(emptyPointer(), it.info, it.expression.expressionString) } //scope field node
            }
            node is ParadoxValueFieldExpressionNode -> {
                expressionConfig.let { CwtValueConfig(emptyPointer(), it.info, it.expression.expressionString) } //value field node 
            }
            node is ParadoxScriptValueArgumentValueExpressionNode -> {
                val argumentNode = node.argumentNode ?: return null
                val passingConfig = withRecursionGuard("icu.windea.pls.lang.ParadoxParameterHandler.inferConfig") a1@{
                    val passingParameterElement = ParadoxParameterSupport.resolveArgument(expressionElement, node.rangeInExpression, argumentNode) ?: return null
                    withCheckRecursion(passingParameterElement.contextKey) a2@{
                        ParadoxParameterHandler.inferConfig(passingParameterElement)
                    }
                }
                passingConfig
            }
            else -> null
        }
    }
}