package icu.windea.pls.ep.match.expression

import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.runWithRecursionGuard
import icu.windea.pls.core.util.ProcessorFactory
import icu.windea.pls.lang.manipulation.ParadoxConfigExpansionService
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxMatchResult

abstract class ParadoxExpandableCsvExpressionMatcher : ParadoxCsvExpressionMatcher {
    /** @see icu.windea.pls.config.CwtDataTypes.UnionValue */
    class ForUnionValue : ParadoxCoreCsvExpressionMatcher() {
        override fun supports(dataType: CwtDataType): Boolean {
            return dataType == CwtDataTypes.UnionValue
        }

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression): ParadoxMatchResult {
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
    }
}
