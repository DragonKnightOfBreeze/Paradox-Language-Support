package icu.windea.pls.config.configExpression.impl

import com.google.common.cache.CacheBuilder
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.core.containsBlank
import icu.windea.pls.core.util.buildCache
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.ep.configExpression.CwtDataExpressionResolver
import icu.windea.pls.ep.configExpression.RuleBasedCwtDataExpressionResolver
import icu.windea.pls.lang.isIdentifierChar

internal class CwtTemplateExpressionResolverImpl : CwtTemplateExpression.Resolver {
    private val cache = CacheBuilder.newBuilder().buildCache<String, CwtTemplateExpression> { doResolve(it) }
    private val emptyExpression = CwtTemplateExpressionImpl("", emptyList())

    override fun resolveEmpty(): CwtTemplateExpression = emptyExpression

    override fun resolve(expressionString: String): CwtTemplateExpression {
        if (expressionString.isEmpty()) return emptyExpression
        if (expressionString.containsBlank()) return emptyExpression // 不允许包含空白
        return cache.get(expressionString)
    }

    private fun doResolve(expressionString: String): CwtTemplateExpression {
        val rules = CwtDataExpressionResolver.allRules
        val snippets = mutableListOf<CwtDataExpression>()
        var startIndex = 0
        while (true) {
            val tuple = rules.mapNotNull f@{ rule ->
                if (rule !is RuleBasedCwtDataExpressionResolver.DynamicRule) return@f null
                if (rule.prefix.isEmpty() || rule.suffix.isEmpty()) return@f null
                val i1 = expressionString.indexOf(rule.prefix, startIndex).takeIf { it != -1 } ?: return@f null
                val i2 = expressionString.indexOf(rule.suffix, i1 + rule.prefix.length).takeIf { it != -1 } ?: return@f null
                tupleOf(rule, i1, i2)
            }.minByOrNull { (_, i1, _) -> i1 }
            if (tuple != null) {
                val (rule, i1, i2) = tuple
                if (i1 > 0 && i1 != startIndex) {
                    addToSnippets(expressionString.substring(startIndex, i1), snippets, true)
                }
                val endIndex = i2 + rule.suffix.length
                addToSnippets(expressionString.substring(i1, endIndex), snippets, false)
                startIndex = endIndex
            }
            if (startIndex >= expressionString.length) {
                break
            }
            if (tuple == null) {
                addToSnippets(expressionString.substring(startIndex), snippets, true)
                break
            }
        }
        if (snippets.size <= 1) return emptyExpression
        return CwtTemplateExpressionImpl(expressionString, snippets)
    }

    private fun addToSnippets(expressionString: String, snippets: MutableList<CwtDataExpression>, isConstant: Boolean) {
        if (expressionString.isEmpty()) return
        if (isConstant) {
            // #129 expressionString can be '<special characters> + <constant rule name>'
            val i = expressionString.indexOfLast { !it.isIdentifierChar() }
            if (i != -1) {
                addToSnippets(expressionString.substring(0, i + 1), snippets, false)
                addToSnippets(expressionString.substring(i + 1), snippets, false)
                return
            }
        }
        snippets += CwtDataExpression.resolveTemplate(expressionString)
    }
}

private class CwtTemplateExpressionImpl(
    override val expressionString: String,
    override val snippetExpressions: List<CwtDataExpression>
) : CwtTemplateExpression {
    override val referenceExpressions = snippetExpressions.filter { it.type != CwtDataTypes.Constant }

    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtTemplateExpression && expressionString == other.expressionString
    }

    override fun hashCode(): Int = expressionString.hashCode()

    override fun toString(): String = expressionString
}
