package icu.windea.pls.ep.match.expression

import com.intellij.openapi.progress.ProgressManager
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.processCandidateConfigs
import icu.windea.pls.core.isIdentifier
import icu.windea.pls.core.runWithRecursionGuard
import icu.windea.pls.lang.match.ParadoxCsvExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxExpressionMatchService
import icu.windea.pls.lang.match.ParadoxMatchProvider
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.ParadoxMatchResultProvider
import icu.windea.pls.model.type.ParadoxExpressionType

class ParadoxCsvBasicExpressionMatcher : ParadoxCsvCompositeExpressionMatcher() {
    override fun registerMatchers() {
        register(CwtDataTypes.Any) { ParadoxMatchResult.FallbackMatch }
        register(CwtDataTypes.Bool) { matchBool(it) }
        register(CwtDataTypes.Int) { matchInt(it) }
        register(CwtDataTypes.Float) { matchFloat(it) }
        register(CwtDataTypes.Scalar) { matchScalar() }
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

class ParadoxExtraBasicCsvExpressionMatcher : ParadoxCsvCompositeExpressionMatcher() {
    override fun registerMatchers() {
        register(CwtDataTypes.PercentageField) { matchPercentageField(it) }
        register(CwtDataTypes.IntPercentageField) { matchIntPercentageField(it) }
        register(CwtDataTypes.DateField) { matchDataField(it) }
    }

    private fun matchPercentageField(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        val r = ParadoxMatchProvider.matchesFloatPercentageField(context.expression.value)
        return ParadoxMatchResult.exactOrNot(r)
    }

    private fun matchIntPercentageField(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        val r = ParadoxMatchProvider.matchesIntPercentageField(context.expression.value)
        return ParadoxMatchResult.exactOrNot(r)
    }

    private fun matchDataField(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        if (!context.expression.type.isLenientString()) return ParadoxMatchResult.NotMatch
        val datePattern = context.configExpression.metadata.value
        val r = ParadoxMatchProvider.matchesDateField(context.expression.value, datePattern)
        return ParadoxMatchResult.exactOrNot(r)
    }
}

class ParadoxCsvCoreExpressionMatcher : ParadoxCsvCompositeExpressionMatcher() {
    override fun registerMatchers() {
        register(CwtDataTypes.Definition) { matchDefinition(it) }
        register(CwtDataTypes.EnumValue) { matchEnumValue(it) }
        register(CwtDataTypes.UnionValue) { matchUnionValue(it) }
        register(CwtDataTypeSets.DynamicValue) { matchDynamicValue(it) }
    }

    private fun matchDefinition(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        val expression = context.expression.value
        // can be an int or float here (e.g., for <technology_tier>)
        if (!context.expression.type.isNumberOrLenientString()) return ParadoxMatchResult.NotMatch
        // if (!expression.isIdentifier(".-")) return ParadoxMatchResult.NotMatch // #369 can also be any string literals
        return ParadoxMatchResultProvider.forDefinition(context.element, context.project, expression, context.configExpression)
    }

    private fun matchEnumValue(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        val name = context.expression.value
        val enumName = context.configExpression.metadata.value ?: return ParadoxMatchResult.NotMatch // null -> invalid config
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

    private fun matchUnionValue(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        val unionName = context.configExpression.metadata.value ?: return ParadoxMatchResult.NotMatch // null -> invalid config
        val unionConfig = context.configGroup.unions[unionName] ?: return ParadoxMatchResult.NotMatch // null -> not match
        // NOTE 3.0.1 recursion guard is required here
        runWithRecursionGuard("csvExpression.match.union", unionName) {
            unionConfig.processCandidateConfigs { valueConfig ->
                ProgressManager.checkCanceled() // check cancellation
                val nextContext = context.copy(configExpression = valueConfig.configExpression)
                val r = ParadoxExpressionMatchService.matchCsvExpression(nextContext)
                if (r.get()) return r
                true
            }
        }
        return ParadoxMatchResult.NotMatch
    }

    private fun matchDynamicValue(context: ParadoxCsvExpressionMatchContext): ParadoxMatchResult {
        val name = context.expression.value
        if (!name.isIdentifier(".")) return ParadoxMatchResult.NotMatch
        val dynamicValueType = context.configExpression.metadata.value
        if (dynamicValueType == null) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResult.FallbackMatch
    }
}
