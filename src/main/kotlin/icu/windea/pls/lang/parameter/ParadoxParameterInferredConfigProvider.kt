package icu.windea.pls.lang.parameter

import com.intellij.openapi.extensions.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.model.*

/**
 * 用于为脚本参数提供推断的CWT规则。
 * 
 * 如果推断结果有多个且互不兼容，认为推断结果存在冲突，最终使用的CWT规则会是null。
 */
@WithGameTypeEP
interface ParadoxParameterInferredConfigProvider {
    fun getConfig(parameterInfo: ParadoxParameterInfo, parameterContextInfo: ParadoxParameterContextInfo) : CwtValueConfig?
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<ParadoxParameterInferredConfigProvider>("icu.windea.pls.parameterInferredConfigProvider")
        
        fun getConfig(name: String, contextInfo: ParadoxParameterContextInfo) : CwtValueConfig? {
            val gameType = contextInfo.gameType
            return EP_NAME.extensions.firstNotNullOfOrNull f@{ ep ->
                if(!gameType.supportsByAnnotation(ep)) return@f null
                ep.getConfig(name, contextInfo)
            }
        }
    }
}