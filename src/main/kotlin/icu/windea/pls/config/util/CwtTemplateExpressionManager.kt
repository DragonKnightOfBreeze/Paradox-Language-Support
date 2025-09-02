package icu.windea.pls.config.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.unquote
import icu.windea.pls.core.util.CacheBuilder
import icu.windea.pls.core.util.Tuple2
import icu.windea.pls.core.util.cancelable
import icu.windea.pls.ep.expression.ParadoxScriptExpressionMatcher
import icu.windea.pls.lang.expression.ParadoxScriptExpression
import icu.windea.pls.lang.util.ParadoxExpressionMatcher

object CwtTemplateExpressionManager {
    fun extract(templateExpression: CwtTemplateExpression, referenceName: String): String {
        if (templateExpression.referenceExpressions.size != 1) throw IllegalStateException()
        return buildString {
            for (snippetExpression in templateExpression.snippetExpressions) {
                when (snippetExpression.type) {
                    CwtDataTypes.Constant -> append(snippetExpression.expressionString)
                    else -> append(referenceName)
                }
            }
        }
    }

    fun extract(templateExpression: CwtTemplateExpression, referenceNames: Map<CwtDataExpression, String>): String {
        if (templateExpression.referenceExpressions.size != referenceNames.size) throw IllegalStateException()
        return buildString {
            for (snippetExpression in templateExpression.snippetExpressions) {
                when (snippetExpression.type) {
                    CwtDataTypes.Constant -> append(snippetExpression.expressionString)
                    else -> append(referenceNames.getValue(snippetExpression))
                }
            }
        }
    }

    fun toRegex(templateExpression: CwtTemplateExpression): Regex {
        return regexCache.get(templateExpression)
    }

    private val regexCache = CacheBuilder().build<CwtTemplateExpression, Regex> { doToRegex(it) }.cancelable()

    private fun doToRegex(templateExpression: CwtTemplateExpression): Regex {
        return buildString { templateExpression.snippetExpressions.forEach { appendRegexSnippet(it) } }.toRegex(RegexOption.IGNORE_CASE)
    }

    fun toMatchedRegex(templateExpression: CwtTemplateExpression, text: String, incomplete: Boolean = false): Tuple2<Regex, MatchResult>? {
        val regex = toRegex(templateExpression)
        val matchResult = regex.matchEntire(text)
        if (matchResult != null) return regex to matchResult
        if (incomplete) {
            var truncated = templateExpression.snippetExpressions.size - 1
            while (truncated > 0) {
                val regex1 = buildString { templateExpression.snippetExpressions.take(truncated).forEach { appendRegexSnippet(it) } }.toRegex(RegexOption.IGNORE_CASE)
                val matchResult1 = regex1.matchEntire(text)
                if (matchResult1 != null) return regex to matchResult1
                truncated--
            }
        }
        return null
    }

    private fun StringBuilder.appendRegexSnippet(expression: CwtDataExpression) {
        if (expression.type == CwtDataTypes.Constant) {
            append("\\Q").append(expression.expressionString).append("\\E")
        } else {
            append("(.*?)")
        }
    }

    fun matches(
        element: PsiElement,
        text: String,
        templateExpression: CwtTemplateExpression,
        configGroup: CwtConfigGroup,
        matchOptions: Int = ParadoxExpressionMatcher.Options.Default
    ): Boolean {
        val snippetExpressions = templateExpression.snippetExpressions
        if (snippetExpressions.isEmpty()) return false
        val expressionString = text.unquote()
        val regex = toRegex(templateExpression)
        val matchResult = regex.matchEntire(expressionString) ?: return false
        if (templateExpression.referenceExpressions.size != matchResult.groups.size - 1) return false
        var i = 1
        for (snippetExpression in snippetExpressions) {
            ProgressManager.checkCanceled()
            if (snippetExpression.type != CwtDataTypes.Constant) {
                val matchGroup = matchResult.groups.get(i++) ?: return false
                val referenceName = matchGroup.value
                val expression = ParadoxScriptExpression.resolve(referenceName, false)
                val matched = ParadoxScriptExpressionMatcher.matches(element, expression, snippetExpression, null, configGroup, matchOptions).get(matchOptions)
                if (!matched) return false
            }
        }
        return true
    }
}
