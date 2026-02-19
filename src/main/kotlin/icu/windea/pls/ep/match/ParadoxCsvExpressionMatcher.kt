package icu.windea.pls.ep.match

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.match.ParadoxCsvExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.ParadoxMatchService

/**
 * 用于匹配 CSV 表达式与规则表达式。
 *
 * @see ParadoxMatchService
 * @see ParadoxCsvExpressionElement
 * @see CwtDataExpression
 */
interface ParadoxCsvExpressionMatcher {
    /**
     * 匹配 CSV 表达式和规则表达式。
     */
    fun match(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxCsvExpressionMatcher>("icu.windea.pls.csvExpressionMatcher")
    }
}
