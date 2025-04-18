package icu.windea.pls.ep.dataExpression

import icu.windea.pls.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.expression.*

abstract class PatternAwareCwtDataExpressionMatcher : CwtDataExpressionMatcher

class TemplateExpressionCwtDataExpressionMatcher : PatternAwareCwtDataExpressionMatcher() {
    override fun matches(expression: CwtDataExpression, targetExpression: ParadoxDataExpression): Boolean {
        TODO("Not yet implemented")
    }
}

class AntExpressionCwtDataExpressionMatcher : PatternAwareCwtDataExpressionMatcher() {
    override fun matches(expression: CwtDataExpression, targetExpression: ParadoxDataExpression): Boolean {
        if (expression.type != CwtDataTypes.AntExpression) return false
        val pattern = expression.value ?: return false
        val ignoreCase = expression.ignoreCase ?: false
        val r = targetExpression.value.matchesAntPattern(pattern, ignoreCase)
        return r
    }
}

class RegexCwtDataExpressionMatcher : PatternAwareCwtDataExpressionMatcher() {
    override fun matches(expression: CwtDataExpression, targetExpression: ParadoxDataExpression): Boolean {
        if (expression.type != CwtDataTypes.Regex) return false
        val pattern = expression.value ?: return false
        val ignoreCase = expression.ignoreCase ?: false
        val r = targetExpression.value.matchesRegex(pattern, ignoreCase)
        return r
    }
}

class ConstantCwtDataExpressionMatcher : CwtDataExpressionMatcher {
    override fun matches(expression: CwtDataExpression, targetExpression: ParadoxDataExpression): Boolean {
        if (expression.type != CwtDataTypes.Constant) return false
        TODO("Not yet implemented")
    }
}
