package icu.windea.pls.ep.match.expression

import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.util.ParadoxMatchFactory

// ExtraBasic

interface ParadoxExtraBasicScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    class ForPercentageField : ParadoxExtraBasicScriptExpressionMatcher {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.PercentageField

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isFullParameterized()) return ParadoxMatchResult.ParameterizedMatch
            val r = ParadoxMatchFactory.matchesFloatPercentageField(context.expression.value)
            if (r) return ParadoxMatchResult.ExactMatch
            if (context.expression.isFullParameterized()) return ParadoxMatchResult.ParameterizedMatch
            return ParadoxMatchResult.NotMatch
        }
    }

    class ForIntPercentageField : ParadoxExtraBasicScriptExpressionMatcher {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.IntPercentageField

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isFullParameterized()) return ParadoxMatchResult.ParameterizedMatch
            val r = ParadoxMatchFactory.matchesIntPercentageField(context.expression.value)
            if (r) return ParadoxMatchResult.ExactMatch
            if (context.expression.isFullParameterized()) return ParadoxMatchResult.ParameterizedMatch
            return ParadoxMatchResult.NotMatch
        }
    }

    class ForDateField : ParadoxExtraBasicScriptExpressionMatcher {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.DateField

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isFullParameterized()) return ParadoxMatchResult.ParameterizedMatch
            val datePattern = configExpression.metadata.value
            val r = ParadoxMatchFactory.matchesDateField(context.expression.value, datePattern)
            if (r) return ParadoxMatchResult.ExactMatch
            if (context.expression.isFullParameterized()) return ParadoxMatchResult.ParameterizedMatch
            return ParadoxMatchResult.NotMatch
        }
    }
}
