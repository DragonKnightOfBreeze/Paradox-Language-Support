package icu.windea.pls.ep.match.expression

import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.util.ParadoxMatchResultFactory
import icu.windea.pls.model.type.ParadoxExpressionRole
import icu.windea.pls.model.type.ParadoxExpressionType

abstract class ParadoxBasicScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    /** @see CwtDataTypes.Any */
    class ForAny : ParadoxBasicScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Any

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            // low-priority fallback
            return ParadoxMatchResult.FallbackMatch
        }
    }

    /** @see CwtDataTypes.Literal */
    class ForLiteral : ParadoxBasicScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Literal

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            // low-priority fallback
            if (!context.expression.isScalar()) return ParadoxMatchResult.NotMatch
            return ParadoxMatchResult.FallbackMatch
        }
    }

    /** @see CwtDataTypes.Scalar */
    class ForScalar : ParadoxBasicScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Scalar

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            // low-priority fallback
            if (!context.expression.isScalar()) return ParadoxMatchResult.NotMatch
            return ParadoxMatchResult.FallbackMatch
        }
    }

    /** @see CwtDataTypes.Bool */
    class ForBool : ParadoxBasicScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Bool

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (context.expression.matchesBoolean()) {
                return ParadoxMatchResult.ExactMatch
            }
            if (context.expression.isFullParameterized()) return ParadoxMatchResult.ParameterizedMatch
            return ParadoxMatchResult.NotMatch
        }
    }

    /** @see CwtDataTypes.Int */
    class ForInt : ParadoxBasicScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Int

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            // quoted number (e.g., `"1"`) -> ok according to vanilla game files
            if (context.expression.matchesInt()) {
                ParadoxMatchResultFactory.forRangedInt(context.expression, configExpression)?.let { return it }
                return ParadoxMatchResult.ExactMatch
            }
            if (context.expression.isFullParameterized()) return ParadoxMatchResult.ParameterizedMatch
            return ParadoxMatchResult.NotMatch
        }
    }

    /** @see CwtDataTypes.Float */
    class ForFloat : ParadoxBasicScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Float

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            // quoted number (e.g., `"1.0"`) -> ok according to vanilla game files
            if (context.expression.matchesFloat()) {
                ParadoxMatchResultFactory.forRangedFloat(context.expression, configExpression)?.let { return it }
                return ParadoxMatchResult.ExactMatch
            }
            if (context.expression.isFullParameterized()) return ParadoxMatchResult.ParameterizedMatch
            return ParadoxMatchResult.NotMatch
        }
    }

    /** @see CwtDataTypes.ColorField */
    class ForColorField : ParadoxBasicScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.ColorField

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            val r = context.expression.type == ParadoxExpressionType.Color && configExpression.metadata.value?.let { context.expression.value.startsWith(it) } != false
            if (r) return ParadoxMatchResult.ExactMatch
            return ParadoxMatchResult.NotMatch
        }
    }

    /** @see CwtDataTypes.Block */
    class ForBlock : ParadoxBasicScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.Block

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (config !is CwtMemberConfig) return ParadoxMatchResult.NotMatch
            if (context.expression.role != ParadoxExpressionRole.Value) return ParadoxMatchResult.NotMatch
            if (context.expression.type != ParadoxExpressionType.Block) return ParadoxMatchResult.NotMatch
            if (context.expression.isFullParameterized()) return ParadoxMatchResult.ParameterizedMatch // also possible
            return ParadoxMatchResultFactory.forBlock(context.element, config)
        }
    }
}
