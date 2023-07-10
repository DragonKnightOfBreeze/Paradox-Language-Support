package icu.windea.pls.lang.parameter

import com.intellij.openapi.extensions.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.impl.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.injection.*
import icu.windea.pls.lang.model.*

/**
 * 用于为脚本参数提供推断的CWT规则。
 *
 * 如果推断结果有多个且互不兼容，认为推断结果存在冲突，最终使用的CWT规则会是null。
 * 
 * 如过参数值是用引号括起的，可能需要通过预言注入推断CWT规则文件的上下文。
 *
 * @see ParadoxScriptInjector
 * @see ParadoxParameterValueConfigContextProvider
 */
@WithGameTypeEP
interface ParadoxParameterInferredConfigProvider {
    fun supports(parameterInfo: ParadoxParameterInfo, parameterContextInfo: ParadoxParameterContextInfo): Boolean
    
    fun getConfig(parameterInfo: ParadoxParameterInfo, parameterContextInfo: ParadoxParameterContextInfo): CwtValueConfig?
    
    /**
     * @throws UnsupportedOperationException 此方法不适用。
     */
    fun getContextConfigs(parameterInfo: ParadoxParameterInfo, parameterContextInfo: ParadoxParameterContextInfo) : List<CwtMemberConfig<*>>?
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxParameterInferredConfigProvider>("icu.windea.pls.parameterInferredConfigProvider")
        
        fun getConfig(parameterInfo: ParadoxParameterInfo, parameterContextInfo: ParadoxParameterContextInfo): CwtValueConfig? {
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
        
        fun getContextConfigs(parameterInfo: ParadoxParameterInfo, parameterContextInfo: ParadoxParameterContextInfo): List<CwtMemberConfig<*>>? {
            val gameType = parameterContextInfo.gameType
            return withRecursionGuard("icu.windea.pls.lang.parameter.ParadoxParameterInferredConfigProvider.INSTANCE.getContextConfigs") {
                EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                    if(!gameType.supportsByAnnotation(ep)) return@f null
                    if(!ep.supports(parameterInfo, parameterContextInfo)) return@f null
                    ep.getContextConfigs(parameterInfo, parameterContextInfo).takeIfNotEmpty()
                }
            }
        }
    }
}