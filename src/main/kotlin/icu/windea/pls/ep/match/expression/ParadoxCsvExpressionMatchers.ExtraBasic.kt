package icu.windea.pls.ep.match.expression

import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.util.ParadoxMatchFactory

interface ParadoxExtraBasicCsvExpressionMatcher : ParadoxCsvExpressionMatcher {
    class ForPercentageField: ParadoxExtraBasicCsvExpressionMatcher {
        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            val r = ParadoxMatchFactory.matchesFloatPercentageField(context.expression.value)
            if (r) return ParadoxMatchResult.ExactMatch
            return ParadoxMatchResult.NotMatch
        }
    }

    class ForIntPercentageField : ParadoxExtraBasicCsvExpressionMatcher {
        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            val r = ParadoxMatchFactory.matchesIntPercentageField(context.expression.value)
            if (r) return ParadoxMatchResult.ExactMatch
            return ParadoxMatchResult.NotMatch
        }
    }

    class ForDateField: ParadoxExtraBasicCsvExpressionMatcher {
        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            val datePattern = configExpression.metadata.value
            val r = ParadoxMatchFactory.matchesDateField(context.expression.value, datePattern)
            if (r) return ParadoxMatchResult.ExactMatch
            return ParadoxMatchResult.NotMatch
        }
    }
}
