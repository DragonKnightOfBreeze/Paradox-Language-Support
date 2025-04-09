package icu.windea.pls.ep.dataExpression

import icu.windea.pls.config.expression.*
import icu.windea.pls.lang.expression.*

/**
 * 用于匹配CWT数据表达式。
 */
interface CwtDataExpressionMatcher {
    fun matches(expression: CwtDataExpression, targetExpression: ParadoxDataExpression): Boolean
}
