package icu.windea.pls.ep.expression

import com.intellij.openapi.extensions.*
import icu.windea.pls.config.*
import icu.windea.pls.core.collections.*

/**
 * 用于解析CWT规则表达式。
 */
interface CwtDataExpressionResolver {
    /**
     * 得到解析结果。
     */
    fun resolve(expressionString: String): Result?
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtDataExpressionResolver>("icu.windea.pls.dataExpressionResolver")
        
        /**
         * @see CwtDataExpressionResolver.resolve
         */
        fun resolve(expressionString: String): Result? {
            EP_NAME.extensionList.forEachFast f@{ ep ->
                val r = ep.resolve(expressionString)
                if(r != null) return r
            }
            return null
        }
    }
    
    data class Result(
        val expressionString: String,
        val type: CwtDataType,
        val value: String? = null,
        val extraValue: Any? = null
    )
}