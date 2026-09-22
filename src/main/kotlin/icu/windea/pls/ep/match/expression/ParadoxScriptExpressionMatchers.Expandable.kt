package icu.windea.pls.ep.match.expression

import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.runWithRecursionGuard
import icu.windea.pls.core.util.ProcessorFactory
import icu.windea.pls.lang.manipulation.ParadoxConfigExpansionService
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxMatchResult

abstract class ParadoxExpandableScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    /** @see CwtDataTypes.UnionValue */
    class ForUnionValue : ParadoxCoreScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.UnionValue

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch // 3.0.2 fast return
            val unionName = configExpression.metadata.value ?: return ParadoxMatchResult.NotMatch // null -> unexpected
            // NOTE 3.0.3 recursion guard is required here
            // NOTE 3.0.3 use first actually matched match result atm, event if there are multiple match results before processing
            val processor = ProcessorFactory.find<ParadoxMatchResult>()
            runWithRecursionGuard("scriptExpression.match.union", unionName) {
                ParadoxConfigExpansionService.expandAndMatchUnion(context.element, context.expression, unionName, context.configGroup, context.options) { _, matchResult ->
                    if (matchResult.get(context.options)) processor.process(matchResult) else true
                }
            }
            return processor.result ?: ParadoxMatchResult.NotMatch
        }
    }

    /** @see CwtDataTypes.AliasName */
    class ForAliasName : ParadoxExpandableScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.AliasKeysField || dataType == CwtDataTypes.AliasName

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (!context.expression.type.isLenientNumberOrStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch // 3.0.2 fast return
            val aliasName = configExpression.metadata.value ?: return ParadoxMatchResult.NotMatch // null -> unexpected
            // NOTE 3.0.3 recursion guard is required here
            // NOTE 3.0.3 use first actually matched match result atm, event if there are multiple match results before processing
            val processor = ProcessorFactory.find<ParadoxMatchResult>()
            runWithRecursionGuard("scriptExpression.match.alias", aliasName) {
                ParadoxConfigExpansionService.expandAndMatchAliasKeys(context.element, context.expression, aliasName, context.configGroup, context.options) { _, matchResult ->
                    if (matchResult.get(context.options)) processor.process(matchResult) else true
                }
            }
            return processor.result ?: ParadoxMatchResult.NotMatch
        }
    }

    /** @see CwtDataTypes.AliasMatchLeft */
    class ForAliasMatchLeft : ParadoxExpandableScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.AliasMatchLeft

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            return ParadoxMatchResult.NotMatch // 不在这里处理
        }
    }

    /** @see CwtDataTypes.SingleAliasRight */
    class ForSingleAliasRight : ParadoxExpandableScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.SingleAliasRight

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            return ParadoxMatchResult.NotMatch // 不在这里处理
        }
    }
}
