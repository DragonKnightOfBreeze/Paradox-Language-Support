package icu.windea.pls.ep.match

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.core.matchesAntPattern
import icu.windea.pls.core.matchesPattern
import icu.windea.pls.core.matchesRegex
import icu.windea.pls.lang.match.ParadoxPatternMatchContext
import icu.windea.pls.lang.match.util.ParadoxMatchResultFactory

class ParadoxDefaultPatternMatcher : ParadoxPatternMatcher {
    override fun matches(text: String, ignoreCase: Boolean, context: ParadoxPatternMatchContext): Boolean {
        if (context.dataType !in CwtDataTypeSets.Pattern) return false
        val pattern = context.configExpression.metadata.value ?: return false
        val ignoreCase = ignoreCase || context.configExpression.metadata.ignoreCase
        val r = when (context.dataType) {
            CwtDataTypes.Glob -> text.matchesPattern(pattern, ignoreCase)
            CwtDataTypes.Ant -> text.matchesAntPattern(pattern, ignoreCase)
            CwtDataTypes.Regex -> text.matchesRegex(pattern, ignoreCase)
            else -> false
        }
        return r
    }
}

class ParadoxTemplatePatternMatcher : ParadoxPatternMatcher {
    override fun matches(text: String, ignoreCase: Boolean, context: ParadoxPatternMatchContext): Boolean {
        if (context.dataType != CwtDataTypes.Template) return false
        val r = ParadoxMatchResultFactory.forTemplate(context.element, context.configGroup, text, context.configExpression, context.options)
        return r.get(context.options)
    }
}
