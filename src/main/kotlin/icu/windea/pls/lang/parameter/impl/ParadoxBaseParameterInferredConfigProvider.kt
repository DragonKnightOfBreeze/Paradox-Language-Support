package icu.windea.pls.lang.parameter.impl

import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.script.psi.*

class ParadoxBaseParameterInferredConfigProvider : ParadoxParameterInferredConfigProvider {
    override fun supports(parameterInfo: ParadoxParameterInfo, parameterContextInfo: ParadoxParameterContextInfo): Boolean {
        return parameterInfo.isEntireExpression //要求整个作为脚本表达式
    }
    
    override fun getConfig(parameterInfo: ParadoxParameterInfo, parameterContextInfo: ParadoxParameterContextInfo): CwtValueConfig? {
        val configs = parameterInfo.expressionConfigs
        val config = configs.firstOrNull() ?: return null
        return when(config.expression.type) {
            CwtDataType.ParameterValue -> {
                //处理参数传递的情况
                //这里需要尝试避免SOE
                if(config !is CwtValueConfig) return null
                val argumentNameElement = parameterInfo.element?.parent?.castOrNull<ParadoxScriptValue>()?.propertyKey ?: return null
                val argumentNameConfig = config.propertyConfig ?: return null
                val passingParameterElement = ParadoxParameterSupport.resolveArgument(argumentNameElement, null, argumentNameConfig) ?: return null
                val passingConfig = withRecursionGuard("icu.windea.pls.lang.parameter.ParadoxParameterInferredConfigProvider.getConfig") {
                    withCheckRecursion(passingParameterElement.key) {
                        ParadoxParameterHandler.getInferredConfig(passingParameterElement)
                    }
                }
                passingConfig
            }
            else -> {
                CwtValueConfig.resolve(emptyPointer(), config.info, config.expression.expressionString)
            }
        }
    }
    
    override fun getContextConfigs(parameterInfo: ParadoxParameterInfo, parameterContextInfo: ParadoxParameterContextInfo) : List<CwtMemberConfig<*>>? {
        val expressionElement = parameterInfo.element?.parent?.castOrNull<ParadoxScriptStringExpressionElement>()
        if(expressionElement == null) return emptyList()
        val contextConfigs = ParadoxConfigHandler.getConfigContext(expressionElement)?.getConfigs().orEmpty()
        if(contextConfigs.isEmpty()) return emptyList()
        val containerConfig = CwtValueConfig.resolve(
            pointer = emptyPointer(),
            info = contextConfigs.first().info,
            value = PlsConstants.blockFolder,
            valueTypeId = CwtType.Block.id,
            configs = contextConfigs.mapFast { config ->
                when(config) {
                    is CwtPropertyConfig -> config.copyDelegated(config.parent, config.deepCopyConfigs())
                    is CwtValueConfig -> config.copyDelegated(config.parent, config.deepCopyConfigs())
                }
            }
        )
        return listOf(containerConfig)
    }
}
