package icu.windea.pls.ep.match.expression

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.processUnionCandidates
import icu.windea.pls.core.match.TextMatcher
import icu.windea.pls.lang.isIdentifier
import icu.windea.pls.lang.match.ParadoxCsvExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxExpressionMatchService
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.ParadoxMatchResultProvider
import icu.windea.pls.model.type.ParadoxExpressionType

class ParadoxBasicCsvExpressionMatcher : ParadoxCsvExpressionMatcher {
    override fun match(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult? {
        return when (context.dataType) {
            CwtDataTypes.Any -> ParadoxMatchResult.FallbackMatch
            CwtDataTypes.Bool -> matchBool(context)
            CwtDataTypes.Int -> matchInt(context)
            CwtDataTypes.Float -> matchFloat(context)
            CwtDataTypes.Scalar -> matchScalar()
            else -> null
        }
    }

    private fun matchBool(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        val r = context.expression.type == ParadoxExpressionType.Boolean
        return ParadoxMatchResult.exactOrNot(r)
    }

    private fun matchInt(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        // empty value is allowed
        if (context.expression.value.isEmpty()) return ParadoxMatchResult.ExactMatch
        // quoted number (e.g., "1") -> ok according to vanilla game files
        if (context.expression.matchesInt()) {
            ParadoxMatchResultProvider.forRangedInt(context.expression, context.configExpression)?.let { return it }
            return ParadoxMatchResult.ExactMatch
        }
        return ParadoxMatchResult.NotMatch
    }

    private fun matchFloat(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        // empty value is allowed
        if (context.expression.value.isEmpty()) return ParadoxMatchResult.ExactMatch
        // quoted number (e.g., "1.0") -> ok according to vanilla game files
        if (context.expression.matchesFloat()) {
            ParadoxMatchResultProvider.forRangedFloat(context.expression, context.configExpression)?.let { return it }
            return ParadoxMatchResult.ExactMatch
        }
        return ParadoxMatchResult.NotMatch
    }

    private fun matchScalar(): ParadoxMatchResult.FallbackMatch {
        // always match (fallback)
        return ParadoxMatchResult.FallbackMatch
    }
}

class ParadoxExtraBasicCsvExpressionMatcher : ParadoxCsvExpressionMatcher {
    override fun match(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult? {
        return when (context.dataType) {
            CwtDataTypes.PercentageField -> matchPercentageField(context)
            CwtDataTypes.IntPercentageField -> matchIntPercentageField(context)
            CwtDataTypes.DateField -> matchDataField(context)
            else -> null
        }
    }

    private fun matchPercentageField(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        val r = TextMatcher.matchesFloatPercentageField(context.expression.value)
        return ParadoxMatchResult.exactOrNot(r)
    }

    private fun matchIntPercentageField(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        val r = TextMatcher.matchesIntPercentageField(context.expression.value)
        return ParadoxMatchResult.exactOrNot(r)
    }

    private fun matchDataField(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        val datePattern = context.configExpression.value
        val r = TextMatcher.matchesDateField(context.expression.value, datePattern)
        return ParadoxMatchResult.exactOrNot(r)
    }
}

class ParadoxCoreCsvExpressionMatcher : ParadoxCsvExpressionMatcher {
    override fun match(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult? {
        return when (context.dataType) {
            CwtDataTypes.Definition -> matchDefinition(context)
            CwtDataTypes.EnumValue -> matchEnumValue(context)
            in CwtDataTypeSets.DynamicValue -> matchDynamicValue(context)
            CwtDataTypes.UnionValue -> matchUnion(context)
            else -> null
        }
    }

    private fun matchDefinition(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        val expression = context.expression.value
        // can be an int or float here (e.g., for <technology_tier>)
        if (!context.expression.type.isNumberOrLenientString()) return ParadoxMatchResult.NotMatch
        if (!expression.isIdentifier(".-")) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResultProvider.forDefinition(context.element, context.project, expression, context.configExpression)
    }

    private fun matchEnumValue(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        val name = context.expression.value
        val enumName = context.configExpression.value ?: return ParadoxMatchResult.NotMatch // null -> invalid config
        // match simple enums
        val enumConfig = context.configGroup.enums[enumName]
        if (enumConfig != null) {
            val r = name in enumConfig.values
            return ParadoxMatchResult.exactOrNot(r)
        }
        // match complex enums
        val complexEnumConfig = context.configGroup.complexEnums[enumName]
        if (complexEnumConfig != null) {
            return ParadoxMatchResultProvider.forComplexEnumValue(context.element, context.project, name, enumName, complexEnumConfig)
        }
        return ParadoxMatchResult.NotMatch
    }

    private fun matchDynamicValue(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        val name = context.expression.value
        if (!name.isIdentifier(".")) return ParadoxMatchResult.NotMatch
        val dynamicValueType = context.configExpression.value
        if (dynamicValueType == null) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResult.FallbackMatch
    }

    private fun matchUnion(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        val unionName = context.configExpression.value ?: return ParadoxMatchResult.NotMatch // null -> invalid config
        val unionConfig = context.configGroup.unions[unionName] ?: return ParadoxMatchResult.NotMatch // null -> not match
        unionConfig.processUnionCandidates { valueConfig ->
            val nextContext = context.copy(configExpression = valueConfig.configExpression)
            val r = ParadoxExpressionMatchService.matchCsvExpression(nextContext)
            if (r.get()) return r
            true
        }
        return ParadoxMatchResult.NotMatch
    }
}
