package icu.windea.pls.ep.dataExpression

import com.intellij.openapi.extensions.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.lang.expression.*

/**
 * 用于匹配CWT数据表达式。
 */
interface CwtDataExpressionMatcher {
    /**
     * 指定目标数据表达式，得到初步的匹配结果。
     */
    fun matches(expression: CwtDataExpression, targetExpression: ParadoxDataExpression): Boolean

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<CwtDataExpressionMatcher>("icu.windea.pls.dataExpressionMatcher")

        /**
         * @see CwtDataExpressionResolver.resolve
         */
        fun matches(expression: CwtDataExpression, targetExpression: ParadoxDataExpression): Boolean {
            return EP_NAME.extensionList.any f@{ ep ->
                ep.matches(expression, targetExpression)
            }
        }
    }
}
