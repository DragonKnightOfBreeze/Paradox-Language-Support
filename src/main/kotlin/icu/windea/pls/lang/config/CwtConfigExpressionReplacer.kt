package icu.windea.pls.lang.config

import com.intellij.openapi.extensions.*
import icu.windea.pls.config.config.*

/**
 * 某些情况下，需要基于上下文替换CWT规则表达式。
 */
interface CwtConfigExpressionReplacer {
    /**
     * 是否需要替换。
     */
    fun shouldReplace(configContext: CwtConfigContext): Boolean
    
    /**
     * 进行替换。注意这里需要先判断是否需要替换，然后再进行替换。如果不需要替换，则返回null。
     */
    fun doReplace(configExpression: String, configContext: CwtConfigContext): String?
    
    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName.create<CwtConfigExpressionReplacer>("icu.windea.pls.configExpressionReplacer")
        
        fun shouldReplace(configContext: CwtConfigContext): Boolean {
            return EP_NAME.extensionList.any { it.shouldReplace(configContext) }
        }
        
        fun doReplace(configExpression: String, configContext: CwtConfigContext): String? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { it.doReplace(configExpression, configContext) }
        }
    }
}
