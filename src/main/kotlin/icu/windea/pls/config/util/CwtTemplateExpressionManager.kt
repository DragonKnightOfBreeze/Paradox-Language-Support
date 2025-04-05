package icu.windea.pls.config.util

import com.google.common.cache.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.util.*

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

    private val regexCache = CacheBuilder.newBuilder().buildCache<CwtTemplateExpression, Regex> { doToRegex(it) }

    private fun doToRegex(templateExpression: CwtTemplateExpression): Regex {
        return buildString {
            templateExpression.snippetExpressions.forEach {
                if (it.type == CwtDataTypes.Constant) {
                    append("\\Q").append(it.expressionString).append("\\E")
                } else {
                    append("(.*?)")
                }
            }
        }.toRegex(RegexOption.IGNORE_CASE)
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
                val expression = ParadoxDataExpression.resolve(referenceName, false)
                val matched = ParadoxExpressionMatcher.matches(element, expression, snippetExpression, null, configGroup, matchOptions).get(matchOptions)
                if (!matched) return false
            }
        }
        return true
    }
}
