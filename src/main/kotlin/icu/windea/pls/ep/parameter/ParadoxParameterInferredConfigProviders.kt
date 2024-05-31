package icu.windea.pls.ep.parameter

import com.intellij.openapi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.model.expression.complex.nodes.*
import icu.windea.pls.script.psi.*

class ParadoxBaseParameterInferredConfigProvider : ParadoxParameterInferredConfigProvider {
    override fun supports(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): Boolean {
        //要求整个作为脚本表达式
        return parameterInfo.isEntireExpression
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
        if(expressionContextConfigs.isEmpty()) return emptyList()
        val containerConfig = CwtValueConfig.resolve(
            pointer = emptyPointer(),
            configGroup = expressionContextConfigs.first().configGroup,
            value = PlsConstants.Folders.block,
            valueTypeId = CwtType.Block.id,
            configs = expressionContextConfigs.map { config ->
                when(config) {
                    is CwtPropertyConfig -> config.delegated(CwtConfigManipulator.deepCopyConfigs(config), config.parentConfig)
                    is CwtValueConfig -> config.delegated(CwtConfigManipulator.deepCopyConfigs(config), config.parentConfig)
                }
            }
        )
        return listOf(containerConfig)
    }
}

class ParadoxComplexExpressionNodeInferredConfigProvider : ParadoxParameterInferredConfigProvider {
    //root.trigger:$PARAM$ -> alias_keys_field[trigger]
    //root.$PARAM$.owner -> scope_field (expression node)
    //root.value:$PARAM$|K|V| -> <script_value>
    //root.value:some_script_value|K|$PARAM$| -> from parameter K
    
    override fun supports(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): Boolean {
        //要求不整个作为脚本表达式
        return !parameterInfo.isEntireExpression
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
        return when {
            node is ParadoxDataSourceNode -> {
                node.linkConfigs.firstNotNullOfOrNull { it.expression?.let { e -> CwtValueConfig.resolve(emptyPointer(), it.configGroup, e.expressionString) } }
            }
            node is ParadoxDynamicValueNode -> {
                node.configs.firstOrNull()?.let { it.expression?.let { e -> CwtValueConfig.resolve(emptyPointer(), it.configGroup, e.expressionString) } }
            }
            node is ParadoxScriptValueNode -> {
                node.config.let { it.expression?.let { e -> CwtValueConfig.resolve(emptyPointer(), it.configGroup, e.expressionString) } }
            }
            node is ParadoxScopeFieldNode -> {
                expressionConfig.let { CwtValueConfig.resolve(emptyPointer(), it.configGroup, "scope_field") } //scope field node
            }
            node is ParadoxValueFieldNode -> {
                expressionConfig.let { CwtValueConfig.resolve(emptyPointer(), it.configGroup, "value_field") } //value field node
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