package icu.windea.pls.lang.parameter

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.model.*

/**
 * 用于为脚本参数提供推断的CWT规则上下文。
 * 
 * 基于语言注入功能为参数值对应的脚本片段提供高级语言功能。
 *
 * @see icu.windea.pls.lang.config.impl.CwtParameterValueConfigContextProvider
 */
@WithGameTypeEP
interface ParadoxParameterInferredConfigProvider {
    fun supports(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): Boolean
    
    /**
     * 返回唯一确定的规则。这个规则仅用作显示（快速文档、参数提示等）。
     */
    fun getConfig(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): CwtValueConfig?
    
    /**
     * 返回唯一确定的规则上下文。这个规则上下文用于后续获取匹配的一组规则。
     */
    fun getContextConfigs(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo) : List<CwtMemberConfig<*>>?
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxParameterInferredConfigProvider>("icu.windea.pls.parameterInferredConfigProvider")
        
        fun getConfig(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): CwtValueConfig? {
            val gameType = parameterContextInfo.gameType
            return withRecursionGuard("icu.windea.pls.lang.parameter.ParadoxParameterInferredConfigProvider.getConfig") {
                EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                    if(!gameType.supportsByAnnotation(ep)) return@f null
                    if(!ep.supports(parameterInfo, parameterContextInfo)) return@f null
                    ep.getConfig(parameterInfo, parameterContextInfo)
                        ?.takeUnless { ParadoxParameterHandler.isIgnoredInferredConfig(it) }
                }
            }
        }
        
        fun getContextConfigs(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): List<CwtMemberConfig<*>>? {
            val gameType = parameterContextInfo.gameType
            return withRecursionGuard("icu.windea.pls.lang.parameter.ParadoxParameterInferredConfigProvider.INSTANCE.getContextConfigs") {
                EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                    if(!gameType.supportsByAnnotation(ep)) return@f null
                    if(!ep.supports(parameterInfo, parameterContextInfo)) return@f null
                    ep.getContextConfigs(parameterInfo, parameterContextInfo).orNull()
                }
            }
        }
    }
}