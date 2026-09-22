package icu.windea.pls.ep.match.expression

import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.matchesAntPattern
import icu.windea.pls.core.matchesPattern
import icu.windea.pls.core.matchesRegex
import icu.windea.pls.lang.match.ParadoxExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxMatchResult
import icu.windea.pls.lang.match.util.ParadoxMatchFactory

class ParadoxConstantScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType in CwtDataTypeSets.Constant
    }

    override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult {
        // 兼容空字符串，兼容带参数的情况
        if (context.expression.isFullParameterized()) {
            return ParadoxMatchResult.ParameterizedMatch
        }
        if (context.expression.isParameterized()) {
            if (context.expression.matchesRegex(configExpression.expressionString)) return ParadoxMatchResult.ParameterizedMatch
            return ParadoxMatchResult.NotMatch
        }
        val r = context.expression.matchesConstant(configExpression.expressionString)
        return ParadoxMatchResult.exactOrNot(r)
    }
}

class ParadoxPatternScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun supports(dataType: CwtDataType) = dataType in CwtDataTypeSets.Pattern

    override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult? {
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

// NOTE 3.0.1 目前从未被实际使用
class ParadoxPredicateBasedScriptExpressionMatcher : ParadoxScriptExpressionMatcher {
    override fun supports(dataType: CwtDataType): Boolean {
        return true
    }

    override fun match(context: ParadoxExpressionMatchContext, configExpression: CwtDataExpression, config: CwtConfig<*>?): ParadoxMatchResult? {
        // 3.0.1 optimize: use attribute to apply fast return
        if (!context.usePredicateBasedMatch) return null

        // 如果附有 `## predicate = {...}` 选项，则根据上下文进行匹配
        if (config !is CwtMemberConfig<*>) return null
        if (!ParadoxMatchFactory.matchesByPredicate(context.element, config)) return ParadoxMatchResult.NotMatch
        return null
    }
}
