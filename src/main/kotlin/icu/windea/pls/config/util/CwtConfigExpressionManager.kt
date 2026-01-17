package icu.windea.pls.config.util

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtLocalisationLocationExpression
import icu.windea.pls.config.configExpression.CwtLocationExpression
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.cache.cancelable
import icu.windea.pls.core.util.Tuple2

object CwtConfigExpressionManager {
    fun getPriority(configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Double {
        val dataType = configExpression.type
        return dataType.priority ?: dataType.priorityProvider?.invoke(configExpression, configGroup) ?: 0.0
    }

    fun resolvePlaceholder(locationExpression: CwtLocationExpression, name: String): String? {
        if (!locationExpression.isPlaceholder) return null
        val r = buildString { for (c in locationExpression.location) if (c == '$') append(name) else append(c) }
        return when {
            locationExpression is CwtLocalisationLocationExpression && locationExpression.forceUpperCase -> r.uppercase()
            else -> r
        }
    }

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
}
