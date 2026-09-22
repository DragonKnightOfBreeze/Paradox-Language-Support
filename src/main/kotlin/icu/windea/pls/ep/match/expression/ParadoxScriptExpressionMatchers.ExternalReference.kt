package icu.windea.pls.ep.match.expression

import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxMatchResult

abstract class ParadoxExternalReferenceScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    /** @see CwtDataTypes.ShaderEffect */
    class ForShaderEffect : ParadoxExternalReferenceScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.ShaderEffect

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
            return ParadoxMatchResult.FallbackMatch
        }
    }

    /** @see CwtDataTypes.MeshLocator */
    class ForMeshLocator : ParadoxExternalReferenceScriptExpressionMatcher() {
        override fun supports(dataType: CwtDataType) = dataType == CwtDataTypes.MeshLocator

        override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
            if (context.expression.value.isEmpty()) return ParadoxMatchResult.FallbackMatch // NOTE 2.1.9 empty string is specially allowed
            if (!context.expression.type.isLenientStringLiteral()) return ParadoxMatchResult.NotMatch
            if (context.expression.isParameterized()) return ParadoxMatchResult.ParameterizedMatch
            return ParadoxMatchResult.FallbackMatch
        }
    }
}
