package icu.windea.pls.ep.match.expression

import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.matchesAntPattern
import icu.windea.pls.core.matchesPattern
import icu.windea.pls.core.matchesRegex
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxMatchResult

/** @see CwtDataTypeSets.Constant */
class ParadoxConstantCsvExpressionMatcher : ParadoxCsvExpressionMatcher {
    override fun supports(dataType: CwtDataType) = dataType in CwtDataTypeSets.Constant

    override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
        // 兼容空字符串
        val r = context.expression.matchesConstant(configExpression.expressionString)
        return ParadoxMatchResult.exactOrNot(r)
    }
}

/** @see CwtDataTypeSets.Pattern */
class ParadoxPatternCsvExpressionMatcher : ParadoxCsvExpressionMatcher {
    override fun supports(dataType: CwtDataType) = dataType in CwtDataTypeSets.Pattern

    override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult? {
        val pattern = configExpression.metadata.value ?: return null
        val ignoreCase = configExpression.metadata.ignoreCase
        val text = context.expression.value
        val r = when (configExpression.type) {
            CwtDataTypes.Glob -> text.matchesPattern(pattern, ignoreCase)
            CwtDataTypes.Ant -> text.matchesAntPattern(pattern, ignoreCase)
            CwtDataTypes.Regex -> text.matchesRegex(pattern, ignoreCase)
            else -> return null
        }
        return ParadoxMatchResult.exactOrNot(r)
    }
}
