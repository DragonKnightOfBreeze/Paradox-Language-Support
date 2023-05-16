package icu.windea.pls.lang.parameter.impl

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.parameter.*

class ParadoxComplexExpressionNodeInferredConfigProvider : ParadoxParameterInferredConfigProvider {
    //root.trigger:$PARAM$ -> alias_keys_field[trigger]
    //root.value:$PARAM$|K|V| -> <script_value>
    //root.$PARAM$.owner -> scope_field (unsupported yet)
    
    override fun getConfig(parameterInfo: ParadoxParameterInfo, parameterContextInfo: ParadoxParameterContextInfo): CwtValueConfig? {
        if(parameterInfo.isEntireExpression) return null //要求不整个作为脚本表达式
        val expressionElement = parameterInfo.expressionElement
        val expressionText = expressionElement?.text ?: return null
        if(expressionText.isLeftQuoted()) return null
        val configs = parameterInfo.configs
        val config = configs.firstOrNull() ?: return null
        val configExpression = config.expression
        val configGroup = config.info.configGroup
        val expression = when {
            configExpression.type.isValueSetValueType() -> {
                val textRange = TextRange.create(0, expressionText.length)
                ParadoxValueSetValueExpression.resolve(expressionText, textRange, config, configGroup)
            }
            config.expression.type.isScopeFieldType() -> {
                val textRange = TextRange.create(0, expressionText.length)
                ParadoxScopeFieldExpression.resolve(expressionText, textRange, configGroup)
            }
            config.expression.type.isValueFieldType() -> {
                val textRange = TextRange.create(0, expressionText.length)
                ParadoxValueFieldExpression.resolve(expressionText, textRange, configGroup)
            }
            config.expression.type.isVariableFieldType() -> {
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
                result = getConfigFromNode(node, config)
                if(result != null) return@p false
            }
            true
        }
        return result
    }
    
    private fun getConfigFromNode(node: ParadoxExpressionNode, expressionConfig: CwtDataConfig<*>): CwtValueConfig? {
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
            node is ParadoxScopeExpressionNode -> {
                expressionConfig.let { CwtValueConfig(emptyPointer(), it.info, it.expression.expressionString) } //scope node
            }
            node is ParadoxValueFieldExpressionNode -> {
                expressionConfig.let { CwtValueConfig(emptyPointer(), it.info, it.expression.expressionString) } //scope node 
            }
            else -> null
        }
    }
}