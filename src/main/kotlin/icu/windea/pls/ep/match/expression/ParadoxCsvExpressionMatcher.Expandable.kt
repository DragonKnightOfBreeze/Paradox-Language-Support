package icu.windea.pls.ep.match.expression

import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.lang.manipulation.ParadoxConfigExpansionService
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxMatchResult

abstract class ParadoxExpandableCsvExpressionMatcher : ParadoxCsvExpressionMatcher {
    /** @see CwtDataTypes.UnionValue */
    class ForUnionValue : ParadoxCoreCsvExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.UnionValue

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
            val unionName = configExpression.metadata.value ?: return ParadoxMatchResult.NotMatch // null -> invalid config
            // NOTE 3.0.1 recursion guard is required here
            // NOTE 3.0.3 use first actually matched match result atm, event if there are multiple match results before processing
            val guardName = "csvExpression.match.union"
            val expanded = ParadoxConfigExpansionService.expandAndMatchUnion(context.element, context.expression, unionName, context.configGroup, context.options, guardName = guardName)
            val result = expanded.firstNotNullOrOrNull { _, mr -> if (mr.get(context.options)) mr else null }
            return result ?: ParadoxMatchResult.NotMatch
        }
    }
}
