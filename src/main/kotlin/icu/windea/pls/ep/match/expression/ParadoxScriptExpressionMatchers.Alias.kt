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

abstract class ParadoxAliasScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    /** @see CwtDataTypes.AliasName */
    class ForAliasName : ParadoxAliasScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.AliasKeysField || dataType == CwtDataTypes.AliasName

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (!context.expression.type.isLenientNumberOrStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
            val aliasName = configExpression.metadata.value ?: return ParadoxMatchResult.NotMatch
            // NOTE 3.0.1 recursion guard is required here
            val processor = ProcessorFactory.find<ParadoxMatchResult>()
            runWithRecursionGuard("scriptExpression.match.alias", aliasName) {
                ParadoxConfigExpansionService.expandAndMatchAliasKeys(context.element, context.expression, aliasName, context.configGroup, context.options) { _, r ->
                    processor.process(r)
                }
            }
            return processor.result ?: ParadoxMatchResult.NotMatch
        }
    }

    /** @see CwtDataTypes.AliasMatchLeft */
    class ForAliasMatchLeft : ParadoxAliasScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.AliasMatchLeft

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            return ParadoxMatchResult.NotMatch // 不在这里处理
        }
    }

    /** @see CwtDataTypes.SingleAliasRight */
    class ForSingleAliasRight : ParadoxAliasScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.SingleAliasRight

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            return ParadoxMatchResult.NotMatch // 不在这里处理
        }
    }
}
