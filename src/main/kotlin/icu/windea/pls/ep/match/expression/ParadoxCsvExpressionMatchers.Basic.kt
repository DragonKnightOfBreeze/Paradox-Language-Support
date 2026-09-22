package icu.windea.pls.ep.match.expression

import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.util.ParadoxMatchResultFactory

interface ParadoxCsvBasicExpressionMatcher : ParadoxCsvExpressionMatcher {
    class ForAny : ParadoxCsvBasicExpressionMatcher {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Any

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
            return ParadoxMatchResult.FallbackMatch
        }
    }

    class ForBool : ParadoxCsvBasicExpressionMatcher {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Bool

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
            if (context.expression.type.isLenientBooleanLiteral()) {
                return ParadoxMatchResult.ExactMatch
            }
            return ParadoxMatchResult.NotMatch
        }
    }

    class ForInt : ParadoxCsvBasicExpressionMatcher {
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

    class ForFloat : ParadoxCsvBasicExpressionMatcher {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Float

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

    class ForScalar : ParadoxCsvBasicExpressionMatcher {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Scalar

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
            // always match (fallback)
            return ParadoxMatchResult.FallbackMatch
        }
    }
}
