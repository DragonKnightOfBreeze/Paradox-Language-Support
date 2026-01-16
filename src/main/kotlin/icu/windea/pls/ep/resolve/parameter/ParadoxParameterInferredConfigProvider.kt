package icu.windea.pls.ep.resolve.parameter

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.model.ParadoxParameterContextInfo

/**
 * 用于为脚本参数提供（基于使用）推断的规则上下文。
 *
 * 基于语言注入功能为参数值对应的脚本片段提供高级语言功能。
 *
 * @see icu.windea.pls.ep.resolve.config.CwtParameterValueConfigContextProvider
 */
@WithGameTypeEP
interface ParadoxParameterInferredConfigProvider {
    fun supports(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): Boolean

    /**
     * 返回唯一确定的规则上下文。这个规则上下文用于后续获取匹配的一组规则。
     */
    fun getContextConfigs(parameterInfo: ParadoxParameterContextInfo.Parameter, parameterContextInfo: ParadoxParameterContextInfo): List<CwtMemberConfig<*>>?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxParameterInferredConfigProvider>("icu.windea.pls.parameterInferredConfigProvider")
    }
}
