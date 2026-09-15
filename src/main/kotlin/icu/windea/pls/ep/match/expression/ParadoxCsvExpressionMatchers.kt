package icu.windea.pls.ep.match.expression

import com.intellij.openapi.progress.ProgressManager
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.expandUnionCandidates
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.isIdentifier
import icu.windea.pls.core.matchesAntPattern
import icu.windea.pls.core.matchesPattern
import icu.windea.pls.core.matchesRegex
import icu.windea.pls.core.runWithRecursionGuard
import icu.windea.pls.core.util.ProcessorScope
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxExpressionMatchService
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.util.ParadoxMatchFactory
import icu.windea.pls.lang.match.util.ParadoxMatchResultFactory

@Suppress("UNUSED_PARAMETER")
class ParadoxCsvBasicExpressionMatcher : ParadoxCsvCompositeExpressionMatcher() {
    override fun registerMatchers() {
        register(CwtDataTypes.Any, ::matchAny)
        register(CwtDataTypes.Bool, ::matchBool)
        register(CwtDataTypes.Int, ::matchInt)
        register(CwtDataTypes.Float, ::matchFloat)
        register(CwtDataTypes.Scalar, ::matchScalar)
    }

    private fun matchAny(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
        return ParadoxMatchResult.FallbackMatch
    }

    private fun matchBool(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
        if (context.expression.type.isLenientBooleanLiteral()) {
            return ParadoxMatchResult.ExactMatch
        }
        return ParadoxMatchResult.NotMatch
    }

    private fun matchInt(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
        // empty value is allowed
        if (context.expression.value.isEmpty()) return ParadoxMatchResult.ExactMatch
        // quoted number (e.g., `"1"`) -> ok according to vanilla game files
        if (context.expression.matchesInt()) {
            ParadoxMatchResultFactory.forRangedInt(context.expression, configExpression)?.let { return it }
            return ParadoxMatchResult.ExactMatch
        }
        return ParadoxMatchResult.NotMatch
    }

    private fun matchFloat(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
        // empty value is allowed
        if (context.expression.value.isEmpty()) return ParadoxMatchResult.ExactMatch
        // quoted number (e.g., `"1.0"`) -> ok according to vanilla game files
        if (context.expression.matchesFloat()) {
            ParadoxMatchResultFactory.forRangedFloat(context.expression, configExpression)?.let { return it }
            return ParadoxMatchResult.ExactMatch
        }
        return ParadoxMatchResult.NotMatch
    }

    private fun matchScalar(config: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult.FallbackMatch {
        // always match (fallback)
        return ParadoxMatchResult.FallbackMatch
    }
}

@Suppress("UNUSED_PARAMETER")
class ParadoxExtraBasicCsvExpressionMatcher : ParadoxCsvCompositeExpressionMatcher() {
    override fun registerMatchers() {
        register(CwtDataTypes.PercentageField, ::matchPercentageField)
        register(CwtDataTypes.IntPercentageField, ::matchIntPercentageField)
        register(CwtDataTypes.DateField, ::matchDataField)
    }

    private fun matchPercentageField(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        val r = ParadoxMatchFactory.matchesFloatPercentageField(context.expression.value)
        if (r) return ParadoxMatchResult.ExactMatch
        return ParadoxMatchResult.NotMatch
    }

    private fun matchIntPercentageField(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        val r = ParadoxMatchFactory.matchesIntPercentageField(context.expression.value)
        if (r) return ParadoxMatchResult.ExactMatch
        return ParadoxMatchResult.NotMatch
    }

    private fun matchDataField(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        val datePattern = configExpression.metadata.value
        val r = ParadoxMatchFactory.matchesDateField(context.expression.value, datePattern)
        if (r) return ParadoxMatchResult.ExactMatch
        return ParadoxMatchResult.NotMatch
    }
}

@Suppress("UNUSED_PARAMETER")
class ParadoxCsvCoreExpressionMatcher : ParadoxCsvCompositeExpressionMatcher() {
    override fun registerMatchers() {
        register(CwtDataTypes.Definition, ::matchDefinition)
        register(CwtDataTypes.EnumValue, ::matchEnumValue)
        register(CwtDataTypes.UnionValue, ::matchUnionValue)
        register(CwtDataTypeSets.DynamicValue, ::matchDynamicValue)
    }

    private fun matchDefinition(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
        // can be an int or float here (e.g., for <technology_tier>)
        if (!context.expression.type.isLenientNumberOrStringLiteral()) return ParadoxMatchResult.NotMatch
        // if (!context.expression.value.isParameterAwareIdentifier(".-")) return ParadoxMatchResult.NotMatch // #369 can also be any string literals
        return ParadoxMatchResultFactory.forDefinition(context.element, context.project, context.expression.value, configExpression)
    }

    private fun matchEnumValue(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
        val name = context.expression.value
        val enumName = configExpression.metadata.value ?: return ParadoxMatchResult.NotMatch // null -> invalid config
        // match simple enums
        val enumConfig = context.configGroup.enums[enumName]
        if (enumConfig != null) {
            val r = name in enumConfig.values
            return ParadoxMatchResult.exactOrNot(r)
        }
        // match complex enums
        val complexEnumConfig = context.configGroup.complexEnums[enumName]
        if (complexEnumConfig != null) {
            return ParadoxMatchResultFactory.forComplexEnumValue(context.element, context.project, name, enumName, complexEnumConfig)
        }
        return ParadoxMatchResult.NotMatch
    }

    private fun matchUnionValue(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
        val unionName = configExpression.metadata.value ?: return ParadoxMatchResult.NotMatch // null -> invalid config
        val unionConfig = context.configGroup.unions[unionName] ?: return ParadoxMatchResult.NotMatch // null -> not match
        // NOTE 3.0.1 recursion guard is required here
        return ProcessorScope.findFrom {
            runWithRecursionGuard("csvExpression.match.union", unionName) {
                unionConfig.expandUnionCandidates { valueConfig ->
                    ProgressManager.checkCanceled() // check cancellation
                    val r = ParadoxExpressionMatchService.matchCsvExpression(context, valueConfig.configExpression)
                    if (r.get()) process(r)
                    else true
                }
            }
        } ?: ParadoxMatchResult.NotMatch
    }

    private fun matchDynamicValue(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
        val name = context.expression.value
        if (!name.isIdentifier(".")) return ParadoxMatchResult.NotMatch
        val dynamicValueType = configExpression.metadata.value
        if (dynamicValueType == null) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResult.FallbackMatch
    }
}

class ParadoxCsvConstantExpressionMatcher : ParadoxCsvSimpleExpressionMatcher() {
    override val dataTypes = CwtDataTypeSets.Constant

    override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
        // 兼容空字符串，兼容带参数的情况
        val r = context.expression.matchesConstant(configExpression.expressionString)
        return ParadoxMatchResult.exactOrNot(r)
    }
}

class ParadoxCsvPatternExpressionMatcher : ParadoxCsvSimpleExpressionMatcher() {
    override val dataTypes = CwtDataTypeSets.Pattern

    override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult? {
        val pattern = configExpression.metadata.value ?: return null
        val ignoreCase = configExpression.metadata.ignoreCase
        val text = context.expression.value
        val r = when (configExpression.type) {
            CwtDataTypes.Glob -> text.matchesPattern(pattern, ignoreCase)
            CwtDataTypes.Ant -> text.matchesAntPattern(pattern, ignoreCase)
            CwtDataTypes.Regex -> text.matchesRegex(pattern, ignoreCase)
            else -> return null
        }
        return ParadoxMatchResult.exactOrNot(r)
    }
}
