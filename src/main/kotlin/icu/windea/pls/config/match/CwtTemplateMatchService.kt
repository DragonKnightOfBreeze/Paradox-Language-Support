package icu.windea.pls.config.match

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.collections.anyFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.forEachIndexedFast

@Optimized
object CwtTemplateMatchService {
    /**
     * 尝试将输入文本与模板表达式进行匹配，并得到匹配结果（[CwtTemplateMatchResult]）。
     *
     * 说明：
     * - 完整模式（[incomplete] 为 `false`）：要求模板表达式的所有片段都参与匹配。
     *   常量片段需要完整匹配（忽略大小写），动态片段接受任意文本（包括空字符串），
     *   并且输入文本必须被完整消费。此时匹配结果的分组数量恒等于片段数量。
     * - 不完整模式（[incomplete] 为 `true`）：用于代码补全等场景。采用前缀匹配策略：
     *   首先尝试完整匹配；若无法匹配且当前需要匹配的片段是常量片段（且其之前不存在待定动态片段），
     *   则允许该常量片段仅匹配输入文本的前缀；若仍然无法匹配，则忽略模板末尾尚未匹配的片段，
     *   使用剩余片段继续尝试匹配。
     *
     * @see CwtTemplateExpression
     * @see CwtTemplateMatchResult
     * @see CwtTemplateMatchGroup
     */
    fun match(input: String, templateExpression: CwtTemplateExpression, incomplete: Boolean): CwtTemplateMatchResult? {
        if (templateExpression.expressionString.isEmpty()) return null // invalid template expression -> fast return

        val groups = doMatch(input, templateExpression.snippetExpressions, incomplete) ?: return null
        if (groups.isEmpty()) return null // not matched
        val incompleteMarker = groups.anyFast { it.incomplete }
        return CwtTemplateMatchResult(templateExpression, input, groups, incomplete = incompleteMarker)
    }

    /**
     * 顺序匹配输入文本与所有片段，匹配成功时返回所有片段对应的分组。
     *
     * 实现要点：
     * - 动态片段先暂存（可能连续出现），直到遇到常量片段时一起结算：
     *   第一个动态片段消费常量片段之前跳过的文本，其余动态片段消费空字符串。
     * - 输入文本被完整消费时才算匹配成功（不完整模式下的前缀匹配除外）。
     */
    private fun doMatch(input: String, snippetExpressions: List<CwtDataExpression>, incomplete: Boolean): List<CwtTemplateMatchGroup>? {
        val length = input.length
        var current = 0
        val groups = mutableListOf<CwtTemplateMatchGroup>()
        val wildcardExpressions = mutableListOf<CwtDataExpression>()

        var snippetExpressionIndex = 0
        while (snippetExpressionIndex < snippetExpressions.size) {
            val snippetExpression = snippetExpressions[snippetExpressionIndex]
            if (snippetExpression.type != CwtDataTypes.Constant) {
                wildcardExpressions += snippetExpression
                snippetExpressionIndex++
                continue
            }

            val snippet = snippetExpression.expressionString
            if (wildcardExpressions.isEmpty()) {
                // 常量片段需要从当前位置起完整匹配
                if (current + snippet.length <= length && input.regionMatches(current, snippet, 0, snippet.length, ignoreCase = true)) {
                    groups += CwtTemplateMatchGroup(snippetExpression, input.substring(current, current + snippet.length), current)
                    current += snippet.length
                    snippetExpressionIndex++
                    continue
                }
                // 不完整模式下允许常量片段仅匹配输入文本的前缀
                if (incomplete && isPrefixOf(snippet, input, current)) {
                    groups += CwtTemplateMatchGroup(snippetExpression, input.substring(current), current, incomplete = true)
                    return groups
                }
                return null
            } else {
                // 动态片段之后的常量片段允许出现在其后的任意位置
                val i = input.indexOf(snippet, current, ignoreCase = true)
                if (i != -1) {
                    flushWildcardExpressions(groups, wildcardExpressions, input, current, i)
                    wildcardExpressions.clear()
                    groups += CwtTemplateMatchGroup(snippetExpression, input.substring(i, i + snippet.length), i)
                    current = i + snippet.length
                    snippetExpressionIndex++
                    continue
                }
                // 不完整模式下找不到常量片段时，由待定动态片段消费剩余文本，并忽略后续片段
                if (incomplete) {
                    flushWildcardExpressions(groups, wildcardExpressions, input, current, length)
                    wildcardExpressions.clear()
                    return groups
                }
                return null
            }
        }

        // 结算模板末尾剩余的动态片段
        if (wildcardExpressions.isNotEmpty()) {
            flushWildcardExpressions(groups, wildcardExpressions, input, current, length)
            wildcardExpressions.clear()
            current = length
        }
        if (current != length) return null // not fully consumed
        return groups
    }

    private fun isPrefixOf(snippet: String, input: String, offset: Int): Boolean {
        val remainLength = input.length - offset
        if (remainLength > snippet.length) return false
        if (remainLength == 0) return true
        return snippet.regionMatches(0, input, offset, remainLength, ignoreCase = true)
    }

    private fun flushWildcardExpressions(
        groups: MutableList<CwtTemplateMatchGroup>,
        wildcardExpressions: List<CwtDataExpression>,
        input: String,
        start: Int,
        end: Int,
    ) {
        wildcardExpressions.forEachIndexedFast { index, wildcardExpression ->
            if (index == 0) {
                groups += CwtTemplateMatchGroup(wildcardExpression, input.substring(start, end), start)
            } else {
                groups += CwtTemplateMatchGroup(wildcardExpression, "", end)
            }
        }
    }

    fun toRegex(templateExpression: CwtTemplateExpression): Regex {
        return regexCache.get(templateExpression)
    }

    private val regexCache = CacheBuilder("expireAfterAccess=30m").build<CwtTemplateExpression, Regex> { doToRegex(it) }

    private fun doToRegex(templateExpression: CwtTemplateExpression): Regex {
        return buildString { templateExpression.snippetExpressions.forEachFast { appendRegexSnippet(it) } }.toRegex(RegexOption.IGNORE_CASE)
    }

    private fun StringBuilder.appendRegexSnippet(snippetExpression: CwtDataExpression) {
        when (snippetExpression.type) {
            CwtDataTypes.Constant -> append("\\Q").append(snippetExpression.expressionString).append("\\E")
            else -> append("(.*?)")
        }
    }
}
