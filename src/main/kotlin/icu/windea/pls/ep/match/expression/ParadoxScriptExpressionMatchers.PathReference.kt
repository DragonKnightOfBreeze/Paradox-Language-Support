package icu.windea.pls.ep.match.expression

import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.util.ParadoxMatchResultFactory

/** @see CwtDataTypeSets.PathReference */
class ParadoxPathReferenceScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun supports(dataType: CwtDataType) = dataType in CwtDataTypeSets.PathReference

    override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
        if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
        return ParadoxMatchResultFactory.forPathReference(context.element, context.project, context.expression.value, configExpression)
    }
}
