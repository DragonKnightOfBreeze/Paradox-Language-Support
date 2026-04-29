package icu.windea.pls.ep.match

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.resolve.ParadoxTypeService
import icu.windea.pls.lang.isIdentifier
import icu.windea.pls.lang.match.ParadoxCsvExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.ParadoxMatchResultProvider
import icu.windea.pls.model.ParadoxType

class ParadoxBaseCsvExpressionMatcher : ParadoxCsvExpressionMatcher {
    override fun match(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult? {
        return when (context.dataType) {
            CwtDataTypes.Bool -> matchBool(context)
            CwtDataTypes.Int -> matchInt(context)
            CwtDataTypes.Float -> matchFloat(context)
            CwtDataTypes.Scalar -> matchScalar()
            else -> null
        }
    }

    private fun matchBool(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        val value = context.expressionText
        val r = ParadoxTypeService.isBoolean(value)
        return ParadoxMatchResult.exactOrNot(r)
    }

    private fun matchInt(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        val value = context.expressionText
        val r = value.isEmpty() || ParadoxTypeService.isInt(value) // empty value is allowed
        if (!r) return ParadoxMatchResult.NotMatch
        ParadoxMatchResultProvider.forRangedInt(value, context.configExpression)?.let { return it }
        return ParadoxMatchResult.ExactMatch
    }

    private fun matchFloat(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        val value = context.expressionText
        val r = value.isEmpty() || ParadoxTypeService.isFloat(value) // empty value is allowed
        if (!r) return ParadoxMatchResult.NotMatch
        ParadoxMatchResultProvider.forRangedFloat(value, context.configExpression)?.let { return it }
        return ParadoxMatchResult.ExactMatch
    }

    private fun matchScalar(): ParadoxMatchResult.FallbackMatch {
        return ParadoxMatchResult.FallbackMatch // always match (fallback)
    }
}

class ParadoxCoreCsvExpressionMatcher : ParadoxCsvExpressionMatcher {
    override fun match(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult? {
        return when (context.dataType) {
            CwtDataTypes.Definition -> matchDefinition(context)
            CwtDataTypes.EnumValue -> matchEnumValue(context)
            else -> null
        }
    }

    private fun matchDefinition(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        // can be an int or float here (e.g., for <technology_tier>)
        val value = context.expressionText.unquote()
        val valueType = ParadoxTypeService.resolve(value)
        if (valueType != ParadoxType.String && valueType != ParadoxType.Int && valueType != ParadoxType.Float) return ParadoxMatchResult.NotMatch
        if (!value.isIdentifier(".-")) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResultProvider.forDefinition(context.element, context.project, value, context.configExpression)
    }

    private fun matchEnumValue(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        val value = context.expressionText.unquote()
        val enumName = context.configExpression.value ?: return ParadoxMatchResult.NotMatch // null -> invalid config
        run {
            // match simple enums
            val enumConfig = context.configGroup.enums[enumName] ?: return@run
            val r = value in enumConfig.values
            return ParadoxMatchResult.exactOrNot(r)
        }
        run {
            // match complex enums
            val complexEnumConfig = context.configGroup.complexEnums[enumName] ?: return@run
            return ParadoxMatchResultProvider.forComplexEnumValue(context.element, context.project, value, enumName, complexEnumConfig)
        }
        return ParadoxMatchResult.NotMatch
    }
}
