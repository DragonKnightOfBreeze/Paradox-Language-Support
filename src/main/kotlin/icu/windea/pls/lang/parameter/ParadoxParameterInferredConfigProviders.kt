package icu.windea.pls.lang.parameter

import com.intellij.openapi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

class ParadoxBaseParameterInferredConfigProvider : ParadoxParameterInferredConfigProvider {
    override fun supports(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): Boolean {
        //要求整个作为脚本表达式
        return parameterInfo.isEntireExpression
    }
    
    override fun getConfig(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): CwtValueConfig? {
        val expressionConfigs = parameterInfo.expressionConfigs
        val config = expressionConfigs.firstNotNullOfOrNull { doGetConfigFromExpressionConfig(it, parameterInfo) }
        return config
    }
    
    override fun getContextConfigs(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): List<CwtMemberConfig<*>>? {
        val expressionElement = parameterInfo.expressionElement ?: return null
        val expressionContextConfigs = CwtConfigHandler.getConfigContext(expressionElement)?.getConfigs().orEmpty()
        val contextConfigs = doGetContextConfigsFromExpressionContextConfigs(expressionContextConfigs, parameterInfo)
        return contextConfigs
    }
    
    private fun doGetConfigFromExpressionConfig(expressionConfig: CwtMemberConfig<*>, parameterInfo: ParadoxParameterContextInfo.Parameter): CwtValueConfig? {
        if(expressionConfig.expression.type == CwtDataType.ParameterValue) {
            //处理参数传递的情况
            //这里需要尝试避免SOE
            if(expressionConfig !is CwtValueConfig) return null
            val argumentNameElement = parameterInfo.element?.parent?.castOrNull<ParadoxScriptValue>()?.propertyKey ?: return null
            val argumentNameConfig = expressionConfig.propertyConfig ?: return null
            val passingParameterElement = ParadoxParameterSupport.resolveArgument(argumentNameElement, null, argumentNameConfig) ?: return null
            val passingConfig = withRecursionGuard("icu.windea.pls.lang.parameter.impl.ParadoxBaseParameterInferredConfigProvider.doGetConfigFromExpressionConfig") {
                withCheckRecursion(passingParameterElement.contextKey) {
                    ParadoxParameterHandler.getInferredConfig(passingParameterElement)
                }
            }
            return passingConfig
        }
        return CwtValueConfig.resolve(emptyPointer(), expressionConfig.info, expressionConfig.expression.expressionString)
    }
    
    private fun doGetContextConfigsFromExpressionContextConfigs(expressionContextConfigs: List<CwtMemberConfig<*>>, parameterInfo: ParadoxParameterContextInfo.Parameter): List<CwtMemberConfig<*>>{
        if(expressionContextConfigs.isEmpty()) return emptyList()
        val expressionContextConfig = expressionContextConfigs.find { it.expression.type == CwtDataType.ParameterValue }
        if(expressionContextConfig != null) {
            //处理参数传递的情况
            //这里需要尝试避免SOE
            if(expressionContextConfig !is CwtValueConfig) return emptyList()
            val argumentNameElement = parameterInfo.element?.parent?.castOrNull<ParadoxScriptValue>()?.propertyKey ?: return emptyList()
            val argumentNameConfig = expressionContextConfig.propertyConfig ?: return emptyList()
            val passingParameterElement = ParadoxParameterSupport.resolveArgument(argumentNameElement, null, argumentNameConfig) ?: return emptyList()
            val passingContextConfigs = withRecursionGuard("icu.windea.pls.lang.parameter.impl.ParadoxBaseParameterInferredConfigProvider.doGetContextConfigsFromExpressionContextConfigs") {
                withCheckRecursion(passingParameterElement.contextKey) {
                    ParadoxParameterHandler.getInferredContextConfigs(passingParameterElement)
                }
            }
            return passingContextConfigs.orEmpty()
        }
        if(expressionContextConfigs.isEmpty()) return emptyList()
        val containerConfig = CwtValueConfig.resolve(
            pointer = emptyPointer(),
            info = expressionContextConfigs.first().info,
            value = PlsConstants.blockFolder,
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