package icu.windea.pls.lang.expression

import com.intellij.openapi.extensions.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.collections.*

interface CwtDataExpressionResolver {
    fun resolve(expressionString: String): Result?
    
    data class Result(
        val expressionString: String,
        val type: CwtDataType,
        val value: String? = null,
        val extraValue: Any? = null
    )
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtDataExpressionResolver>("icu.windea.pls.dataExpressionResolver")
        
        fun resolve(expressionString: String): Result? {
            EP_NAME.extensionList.forEachFast f@{ ep ->
                val r = ep.resolve(expressionString)
                if(r != null) return r
            }
            return null
        }
    }
}