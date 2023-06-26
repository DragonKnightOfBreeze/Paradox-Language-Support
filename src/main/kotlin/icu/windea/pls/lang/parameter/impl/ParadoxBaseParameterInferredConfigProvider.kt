package icu.windea.pls.lang.parameter.impl

import icu.windea.pls.core.*
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
        val parent = parameterInfo.element?.parent
        when {
            parent is ParadoxScriptPropertyKey -> {
                //不适用于这种情况
                throw UnsupportedOperationException()
            }
            parent is ParadoxScriptString && parent.isPropertyValue() -> {
                //将rootBlock中的propertyConfigs转化为propertyValueConfigs
                return parameterInfo.expressionContextConfigs.map { c1 ->
                    when(c1) {
                        is CwtPropertyConfig -> c1.copyDelegated(null, doGetPropertyValueConfigs(c1))
                        is CwtValueConfig -> c1.copyDelegated(null, doGetPropertyValueConfigs(c1))
                    }
                }
            }
            else -> {
                return parameterInfo.expressionContextConfigs.map { c1 ->
                    when(c1) {
                        is CwtPropertyConfig -> c1.copyDelegated(null)
                        is CwtValueConfig -> c1.copyDelegated(null)
                    }
                }
            }
        }
    }
    
    private fun doGetPropertyValueConfigs(c1: CwtMemberConfig<*>): List<CwtValueConfig>? {
        return c1.configs?.mapNotNull { c2 ->
            if(c2 !is CwtPropertyConfig) return@mapNotNull null
            val vc = c2.valueConfig ?: return@mapNotNull null
            vc.copyDelegated(c1, vc.configs, null)
        }
    }
}
