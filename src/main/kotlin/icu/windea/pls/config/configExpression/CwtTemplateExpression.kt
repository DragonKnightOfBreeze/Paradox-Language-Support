package icu.windea.pls.config.configExpression

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.containsBlank
import icu.windea.pls.core.optimized
import icu.windea.pls.core.util.text.TextPattern
import icu.windea.pls.core.util.tupleOf
import icu.windea.pls.lang.isIdentifierChar

/**
 * 模板表达式。
 *
 * 用于描述脚本文件中的键或值的更复杂的取值形态，可视为多个数据表达式的组合。
 * 由数个片段拼接而成：常量字段 + 动态片段（受限支持的数据表达式）。
 *
 * 说明：
 * - 不允许包含空白字符；包含空白将直接返回空表达式。
 * - 采用“最左最早匹配”的方式，基于所有动态规则（具有前后缀的规则）扫描字符串，拆分出常量/动态片段。
 * - 仅存在一个片段（纯常量或纯动态）时视为不构成模板，返回空表达式。
 *
 * 适用对象：定义成员对应的规则的键或值。
 *
 * CWTools 兼容性：兼容，但实际的解析与处理逻辑可能不同。
 *
 * 示例：
 *
 * ```cwt
 * job_<job>_add # "job" + <job> + "_add"
 * xxx_value[anything]_xxx # "xxx_" + value[anything] + "_xxx"
 * a_enum[weight_or_base]_b # "a_" + enum[weight_or_base] + "_b"
 * value[gui_element_name]:<sprite> # value[gui_element_name] + ":" + sprite
 * value[gui_element_name]:localisation # value[gui_element_name] + ":" + localisation
 * ```
 *
 * @property snippetExpressions 解析得到的所有片段，顺序与原始字符串一致。
 * @property referenceExpressions 过滤后的引用片段（即非 [CwtDataTypes.Constant] 片段），用于后续的引用解析、导航与高亮。
 *
 * @see CwtDataExpression
 * @see icu.windea.pls.ep.config.configExpression.CwtDataExpressionResolver
 */
interface CwtTemplateExpression : CwtConfigExpression {
    val snippetExpressions: List<CwtDataExpression>
    val referenceExpressions: List<CwtDataExpression>

    interface Resolver {
        fun resolveEmpty(): CwtTemplateExpression
        fun resolve(expressionString: String): CwtTemplateExpression
    }

    companion object : Resolver by CwtTemplateExpressionResolverImpl()
}

// region Implementations

private class CwtTemplateExpressionResolverImpl : CwtTemplateExpression.Resolver {
    // 关键点：
    // - 使用基于 `expressionString` 的缓存以复用解析结果
    // - 含空白字符的输入直接视为非法模板，返回空表达式（避免对不规范规则进行模板拆分）
    // - 仅对拥有“前后缀”的动态规则进行扫描（例如 `value[` 与 `]`、`<` 与 `>`）
    // - 采用“最左最早匹配”的策略：在剩余字符串中选择最靠左的动态片段进行切分，然后继续向后扫描
    // - 当最终片段数不超过 1（纯常量或纯一个动态值）时，不视为模板，返回空表达式

    private val cache = CacheBuilder("expireAfterAccess=30m")
        .build<String, CwtTemplateExpression> { doResolve(it) }

    private val emptyExpression = CwtTemplateExpressionImpl("", emptyList())

    override fun resolveEmpty(): CwtTemplateExpression = emptyExpression

    override fun resolve(expressionString: String): CwtTemplateExpression {
        if (expressionString.isEmpty()) return emptyExpression
        if (expressionString.containsBlank()) return emptyExpression // 不允许包含空白
        return cache.get(expressionString)
    }

    private fun doResolve(expressionString: String): CwtTemplateExpression {
        // 收集所有“具有前后缀”的文本模式，用于匹配拆分
        val patterns = CwtConfigExpressionService.getAllTextPatterns()
        val snippets = mutableListOf<CwtDataExpression>()
        var startIndex = 0
        while (true) {
            // 在剩余字符串中，计算所有可拆分的文本模式的最早出现位置，选择最靠左者
            val tuple = patterns.mapNotNull f@{ pattern ->
                if (pattern !is TextPattern.Delimited) return@f null
                val (prefix, suffix) = pattern
                if (prefix.isEmpty() || suffix.isEmpty()) return@f null
                val i1 = expressionString.indexOf(prefix, startIndex).takeIf { it != -1 } ?: return@f null
                val i2 = expressionString.indexOf(suffix, i1 + prefix.length).takeIf { it != -1 } ?: return@f null
                tupleOf(prefix, suffix, i1, i2)
            }.minByOrNull { (_, _, i1, _) -> i1 }
            if (tuple != null) {
                val (prefix, suffix, i1, i2) = tuple
                // 先加入动态片段之前的常量片段
                if (i1 > 0 && i1 != startIndex) {
                    addToSnippets(expressionString.substring(startIndex, i1), snippets, true)
                }
                // 再加入当前动态片段（包含前后缀）
                val endIndex = i2 + suffix.length
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
        // 只有一个片段时（纯常量或纯动态）不视为模板
        if (snippets.size <= 1) return emptyExpression
        // 所有片段都是常量时不视为模板
        if (snippets.all { it.type == CwtDataTypes.Constant }) return emptyExpression
        return CwtTemplateExpressionImpl(expressionString, snippets)
    }

    private fun addToSnippets(expressionString: String, snippets: MutableList<CwtDataExpression>, isConstant: Boolean) {
        if (expressionString.isEmpty()) return
        if (isConstant) {
            // #129 #232 如果常量片段包含特殊符号，应将之前和之后的部分拆分出来，避免误判（之后的部分还要继续判断）
            val i = expressionString.indexOfFirst { !it.isIdentifierChar() }
            if (i != -1) {
                addToSnippets(expressionString.substring(0, i), snippets, false)
                addToSnippets(expressionString.substring(i, i + 1), snippets, false)
                addToSnippets(expressionString.substring(i + 1), snippets, true)
                return
            }
        }
        // 委托给数据表达式解析器：
        // - 若匹配到动态/常量规则则直接得到对应类型
        // - 否则降级为常量类型并记录原值
        snippets += CwtDataExpression.resolveTemplate(expressionString)
    }
}

private class CwtTemplateExpressionImpl(
    override val expressionString: String,
    snippetExpressions: List<CwtDataExpression>
) : CwtTemplateExpression {
    override val snippetExpressions: List<CwtDataExpression> = snippetExpressions.optimized()
    override val referenceExpressions: List<CwtDataExpression> = snippetExpressions.filter { it.type != CwtDataTypes.Constant }.optimized()

    override fun equals(other: Any?) = this === other || other is CwtTemplateExpression && expressionString == other.expressionString
    override fun hashCode() = expressionString.hashCode()
    override fun toString() = expressionString
}

// endregion
