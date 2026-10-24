package icu.windea.pls.ep.match

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.core.match.MatchService
import icu.windea.pls.lang.match.ParadoxPatternMatchContext

class ParadoxDefaultPatternMatcher : ParadoxPatternMatcher {
    override fun matches(context: ParadoxPatternMatchContext, text: String, ignoreCase: Boolean): Boolean {
        val dataType = context.configExpression.type
        if (dataType !in CwtDataTypeSets.Pattern) return false
        val pattern = context.configExpression.metadata.value ?: return false
        val ignoreCase = ignoreCase || context.configExpression.metadata.ignoreCase
        val r = when (dataType) {
            CwtDataTypes.Glob -> MatchService.matchesPattern(text, pattern, ignoreCase = ignoreCase)
            CwtDataTypes.Ant -> MatchService.matchesAntPattern(text, pattern, ignoreCase = ignoreCase)
            CwtDataTypes.Regex -> MatchService.matchesRegex(text, pattern, ignoreCase = ignoreCase)
            else -> false
        }
        return r
    }
}

