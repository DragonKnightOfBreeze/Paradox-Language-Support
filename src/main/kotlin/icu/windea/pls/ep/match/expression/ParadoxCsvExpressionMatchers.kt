package icu.windea.pls.ep.match.expression

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxMatchResult

class ParadoxCsvConstantExpressionMatcher : ParadoxCsvSimpleExpressionMatcher() {
    override val dataTypes = CwtDataTypeSets.Constant

    override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
        // 兼容空字符串
        val r = context.expression.matchesConstant(configExpression.expressionString)
        return ParadoxMatchResult.exactOrNot(r)
    }
}

