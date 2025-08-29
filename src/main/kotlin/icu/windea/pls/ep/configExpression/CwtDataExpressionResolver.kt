package icu.windea.pls.ep.configExpression

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.configExpression.CwtDataExpression

/**
 * 用于解析CWT数据表达式。
 */
interface CwtDataExpressionResolver {
    /**
     * 得到解析结果。
     */
    fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<CwtDataExpressionResolver>("icu.windea.pls.dataExpressionResolver")

        /**
         * @see CwtDataExpressionResolver.resolve
         */
        fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
            EP_NAME.extensionList.forEach f@{ ep ->
                val r = ep.resolve(expressionString, isKey)
                if (r != null) return r
            }
            return null
        }

        /**
         * @see CwtDataExpressionResolver.resolveTemplate
         */
        fun resolveTemplate(expressionString: String): CwtDataExpression? {
            EP_NAME.extensionList.forEach f@{ ep ->
                if (ep !is RuleBasedCwtDataExpressionResolver) return@f
                val r = ep.resolve(expressionString, false)
                if (r != null) return r
            }
            return null
        }

        val allRules by lazy {
            EP_NAME.extensionList.filterIsInstance<RuleBasedCwtDataExpressionResolver>().flatMap { it.rules }
        }
    }
}
