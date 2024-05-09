package icu.windea.pls.ep.expression

import com.intellij.openapi.extensions.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.collections.*

/**
 * 用于解析CWT规则表达式。
 */
interface CwtDataExpressionResolver {
    /**
     * 得到解析结果。
     */
    fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression?
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtDataExpressionResolver>("icu.windea.pls.dataExpressionResolver")
        
        /**
         * @see CwtDataExpressionResolver.resolve
         */
        fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
            EP_NAME.extensionList.forEachFast f@{ ep ->
                val r = ep.resolve(expressionString, isKey)
                if(r != null) return r
            }
            return null
        }
    }
}