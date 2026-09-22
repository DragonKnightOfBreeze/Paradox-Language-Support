package icu.windea.pls.ep.match.expression

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.isIdentifier
import icu.windea.pls.core.runWithRecursionGuard
import icu.windea.pls.core.util.ProcessorFactory
import icu.windea.pls.lang.manipulation.ParadoxConfigExpansionService
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.util.ParadoxMatchResultFactory

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
        // NOTE 3.0.1 recursion guard is required here
        val processor = ProcessorFactory.find<ParadoxMatchResult>()
        runWithRecursionGuard("csvExpression.match.union", unionName) {
            ParadoxConfigExpansionService.expandAndMatchUnion(context.element, context.expression, unionName, context.configGroup, context.options) { _, matchResult ->
                processor.process(matchResult)
            }
        }
        return processor.result ?: ParadoxMatchResult.NotMatch
    }

    private fun matchDynamicValue(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
        val name = context.expression.value
        if (!name.isIdentifier(".")) return ParadoxMatchResult.NotMatch
        val dynamicValueType = configExpression.metadata.value
        if (dynamicValueType == null) return ParadoxMatchResult.NotMatch
        return ParadoxMatchResult.FallbackMatch
    }
}
