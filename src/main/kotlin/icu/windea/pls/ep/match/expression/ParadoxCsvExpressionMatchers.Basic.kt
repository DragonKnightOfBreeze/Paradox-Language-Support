package icu.windea.pls.ep.match.expression

import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.util.ParadoxMatchResultFactory

abstract class ParadoxBasicCsvExpressionMatcher : ParadoxCsvExpressionMatcher {
    /** @see CwtDataTypes.Any */
    class ForAny : ParadoxBasicCsvExpressionMatcher() {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.Any
        }

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
            return ParadoxMatchResult.FallbackMatch
        }
    }

    /** @see CwtDataTypes.Bool */
    class ForBool : ParadoxBasicCsvExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Bool

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
            if (context.expression.type.isLenientBooleanLiteral()) {
                return ParadoxMatchResult.ExactMatch
            }
            return ParadoxMatchResult.NotMatch
        }
    }

    /** @see CwtDataTypes.Int */
    class ForInt : ParadoxBasicCsvExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Int

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
            // empty value is allowed
            if (context.expression.value.isEmpty()) return ParadoxMatchResult.ExactMatch
            // quoted number (e.g., `"1"`) -> ok according to vanilla game files
            if (context.expression.matchesInt()) {
                ParadoxMatchResultFactory.forRangedInt(context.expression, configExpression)?.let { return it }
                return ParadoxMatchResult.ExactMatch
            }
            return ParadoxMatchResult.NotMatch
        }
    }

    /** @see CwtDataTypes.Float */
    class ForFloat : ParadoxBasicCsvExpressionMatcher() {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.Float
        }

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
            // empty value is allowed
            if (context.expression.value.isEmpty()) return ParadoxMatchResult.ExactMatch
            // quoted number (e.g., `"1.0"`) -> ok according to vanilla game files
            if (context.expression.matchesFloat()) {
                ParadoxMatchResultFactory.forRangedFloat(context.expression, configExpression)?.let { return it }
                return ParadoxMatchResult.ExactMatch
            }
            return ParadoxMatchResult.NotMatch
        }
    }

    /** @see CwtDataTypes.Scalar */
    class ForScalar : ParadoxBasicCsvExpressionMatcher() {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.Scalar
        }

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
            // always match (fallback)
            return ParadoxMatchResult.FallbackMatch
        }
    }
}
