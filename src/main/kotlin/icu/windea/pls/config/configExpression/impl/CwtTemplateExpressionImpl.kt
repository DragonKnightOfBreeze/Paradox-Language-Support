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
    // 关键点：
    // - 使用基于 `expressionString` 的缓存以复用解析结果。
    // - 含空白字符的输入直接视为非法模板，返回空表达式（避免对不规范规则进行模板拆分）。
    // - 仅对拥有“前后缀”的动态规则进行扫描（例如 `value[` 与 `]`、`<` 与 `>`）。
    // - 采用“最左最早匹配”的策略：在剩余字符串中选择最靠左的动态片段进行切分，然后继续向后扫描。
    // - 当最终片段数不超过 1（纯常量或纯一个动态值）时，不视为模板，返回空表达式。
    // - 常量片段中的特殊拆分：若常量尾部包含非标识符字符且其后紧跟可能的“常量规则名”，尝试按 `#129` 的方式拆分，
    //   以避免把 `"<特殊符号> + 常量规则名"` 误读为一个整体常量，影响后续的规则识别与高亮。

    private val cache = CacheBuilder.newBuilder().buildCache<String, CwtTemplateExpression> { doResolve(it) }
    private val emptyExpression = CwtTemplateExpressionImpl("", emptyList())

    override fun resolveEmpty(): CwtTemplateExpression = emptyExpression

    override fun resolve(expressionString: String): CwtTemplateExpression {
        if (expressionString.isEmpty()) return emptyExpression
        if (expressionString.containsBlank()) return emptyExpression // 不允许包含空白
        return cache.get(expressionString)
    }

    private fun doResolve(expressionString: String): CwtTemplateExpression {
        // 收集所有“具有前后缀”的动态规则，用于匹配拆分
        val rules = CwtDataExpressionResolver.allRules
        val snippets = mutableListOf<CwtDataExpression>()
        var startIndex = 0
        while (true) {
            // 在剩余字符串中，计算所有动态规则的最早出现位置，选择最靠左者
            val tuple = rules.mapNotNull f@{ rule ->
                if (rule !is RuleBasedCwtDataExpressionResolver.DynamicRule) return@f null
                if (rule.prefix.isEmpty() || rule.suffix.isEmpty()) return@f null
                val i1 = expressionString.indexOf(rule.prefix, startIndex).takeIf { it != -1 } ?: return@f null
                val i2 = expressionString.indexOf(rule.suffix, i1 + rule.prefix.length).takeIf { it != -1 } ?: return@f null
                tupleOf(rule, i1, i2)
            }.minByOrNull { (_, i1, _) -> i1 }
            if (tuple != null) {
                val (rule, i1, i2) = tuple
                // 先加入动态片段之前的常量片段
                if (i1 > 0 && i1 != startIndex) {
                    addToSnippets(expressionString.substring(startIndex, i1), snippets, true)
                }
                // 再加入当前动态片段（包含前后缀）
                val endIndex = i2 + rule.suffix.length
                addToSnippets(expressionString.substring(i1, endIndex), snippets, false)
                startIndex = endIndex
            }
            if (startIndex >= expressionString.length) {
                break
            }
            if (tuple == null) {
                // 没有更多动态片段，剩余部分视为常量片段
                addToSnippets(expressionString.substring(startIndex), snippets, true)
                break
            }
        }
        // 只有一个片段（纯常量或纯动态）不视为模板
        if (snippets.size <= 1) return emptyExpression
        return CwtTemplateExpressionImpl(expressionString, snippets)
    }

    private fun addToSnippets(expressionString: String, snippets: MutableList<CwtDataExpression>, isConstant: Boolean) {
        if (expressionString.isEmpty()) return
        if (isConstant) {
            // #129 常量片段可能是 "<特殊符号> + <常量规则名>"，此时尝试拆分为两段以避免误判
            val i = expressionString.indexOfLast { !it.isIdentifierChar() }
            if (i != -1) {
                addToSnippets(expressionString.substring(0, i + 1), snippets, false)
                addToSnippets(expressionString.substring(i + 1), snippets, false)
                return
            }
        }
        // 委托给数据表达式解析器：
        // - 若匹配到动态/常量规则则直接得到对应类型；
        // - 否则降级为常量类型并记录原值。
        snippets += CwtDataExpression.resolveTemplate(expressionString)
    }
}

private class CwtTemplateExpressionImpl(
    override val expressionString: String,
    override val snippetExpressions: List<CwtDataExpression>
) : CwtTemplateExpression {
    // 过滤出“引用型”片段（非 Constant），用于后续引用解析/导航
    override val referenceExpressions = snippetExpressions.filter { it.type != CwtDataTypes.Constant }

    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtTemplateExpression && expressionString == other.expressionString
    }

    override fun hashCode(): Int = expressionString.hashCode()

    override fun toString(): String = expressionString
}
