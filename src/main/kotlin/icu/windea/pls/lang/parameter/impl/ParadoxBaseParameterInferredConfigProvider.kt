package icu.windea.pls.lang.parameter.impl

import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.script.psi.*

class ParadoxBaseParameterInferredConfigProvider : ParadoxParameterInferredConfigProvider {
    override fun getConfig(parameterInfo: ParadoxParameterInfo, parameterContextInfo: ParadoxParameterContextInfo): CwtValueConfig? {
        if(!parameterInfo.isEntireExpression) return null //要求整个作为脚本表达式
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
                    withCheckRecursion(passingParameterElement.contextKey) {
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
        if(!parameterInfo.isEntireExpression) return null //要求整个作为脚本表达式
        val parent = parameterInfo.element?.parent
        when {
            parent is ParadoxScriptPropertyKey -> {
                //不适用于这种情况，特殊处理返回的数据
                return listOf(CwtValueConfig.EmptyConfig)
            }
            else -> {
                return parameterInfo.expressionContextConfigs
            }
        }
    }
}
