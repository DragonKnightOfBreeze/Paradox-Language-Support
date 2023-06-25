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
        val config = configs.firstOrNull() as? CwtValueConfig ?: return null
        return when(config.expression.type) {
            CwtDataType.ParameterValue -> {
                //处理参数传递的情况
                //这里需要尝试避免SOE
                val passingConfig = withRecursionGuard("icu.windea.pls.lang.parameter.ParadoxParameterInferredConfigProvider.getConfig") a1@{
                    val argumentNameElement = parameterInfo.element?.parent?.castOrNull<ParadoxScriptValue>()?.propertyKey ?: return@a1 null
                    val argumentNameConfig = config.propertyConfig ?: return@a1 null
                    val passingParameterElement = ParadoxParameterSupport.resolveArgument(argumentNameElement, null, argumentNameConfig) ?: return@a1 null
                    withCheckRecursion(passingParameterElement.contextKey) a2@{
                        ParadoxParameterHandler.getInferredConfig(passingParameterElement)
                    }
                }
                passingConfig
            }
            else -> {
                config
            }
        }
    }
    
    override fun getContainingConfig(parameterInfo: ParadoxParameterInfo, parameterContextInfo: ParadoxParameterContextInfo) : List<CwtMemberConfig<*>>? {
        if(!parameterInfo.isEntireExpression) return null //要求整个作为脚本表达式
        return parameterInfo.expressionContainingConfigs
        //目前看起来不需要做额外的处理
    }
}
