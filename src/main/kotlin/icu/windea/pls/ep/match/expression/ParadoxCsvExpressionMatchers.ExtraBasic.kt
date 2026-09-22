package icu.windea.pls.ep.match.expression

import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.util.ParadoxMatchFactory

abstract class ParadoxExtraBasicCsvExpressionMatcher : ParadoxCsvExpressionMatcher {
    /** @see CwtDataTypes.PercentageField */
    class ForPercentageField : ParadoxExtraBasicCsvExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.PercentageField

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            val r = ParadoxMatchFactory.matchesFloatPercentageField(context.expression.value)
            if (r) return ParadoxMatchResult.ExactMatch
            return ParadoxMatchResult.NotMatch
        }
    }

    /** @see CwtDataTypes.IntPercentageField */
    class ForIntPercentageField : ParadoxExtraBasicCsvExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.IntPercentageField

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            val r = ParadoxMatchFactory.matchesIntPercentageField(context.expression.value)
            if (r) return ParadoxMatchResult.ExactMatch
            return ParadoxMatchResult.NotMatch
        }
    }

    /** @see CwtDataTypes.DateField */
    class ForDateField : ParadoxExtraBasicCsvExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.DateField

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            val datePattern = configExpression.metadata.value
            val r = ParadoxMatchFactory.matchesDateField(context.expression.value, datePattern)
            if (r) return ParadoxMatchResult.ExactMatch
            return ParadoxMatchResult.NotMatch
        }
    }
}
