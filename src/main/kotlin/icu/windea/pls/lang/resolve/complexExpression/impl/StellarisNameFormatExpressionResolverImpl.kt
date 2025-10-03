package icu.windea.pls.lang.resolve.complexExpression.impl

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionBase
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.StellarisNameFormatExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxDataSourceNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorTokenNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxTemplateSnippetConstantNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxTemplateSnippetNode
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.PlsCoreManager

/**
 * 解析器：Stellaris 命名格式表达式。
 */
internal class StellarisNameFormatExpressionResolverImpl : StellarisNameFormatExpression.Resolver {
    override fun resolve(
        text: String,
        range: TextRange,
        configGroup: CwtConfigGroup,
        config: CwtConfig<*>,
    ): StellarisNameFormatExpression? {
        val configExpression = config.configExpression ?: return null
        if (configExpression.type != CwtDataTypes.StellarisNameFormat) return null

        val incomplete = PlsCoreManager.incompleteComplexExpression.get() ?: false
        if (!incomplete && text.isEmpty()) return null

        val formatName = configExpression.value
        val definitionType = formatName?.let { "${it}_name_parts_list" }

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val expression = StellarisNameFormatExpressionImpl(text, range, configGroup, config, formatName, definitionType, nodes)

        val parameterRanges = ParadoxExpressionManager.getParameterRanges(text)
        val offset = range.startOffset
        val textLength = text.length

        fun inParam(i: Int): Boolean = parameterRanges.any { i in it }

        fun addConstant(s: Int, e: Int) {
            if (e <= s) return
            val nodeText = text.substring(s, e)
            val nodeRange = TextRange.create(s + offset, e + offset)
            nodes += ParadoxTemplateSnippetConstantNode.resolve(nodeText, nodeRange, configGroup)
        }

        fun addLocalisation(nameStart: Int, nameEnd: Int) {
            if (nameEnd <= nameStart) return
            val nameText = text.substring(nameStart, nameEnd)
            val nameRange = TextRange.create(nameStart + offset, nameEnd + offset)
            val locExpr = CwtDataExpression.resolve("localisation", true)
            nodes += ParadoxTemplateSnippetNode.resolve(nameText, nameRange, configGroup, locExpr)
        }

        fun addDefinition(nameStart: Int, nameEnd: Int) {
            if (nameEnd <= nameStart) return
            val nameText = text.substring(nameStart, nameEnd)
            val nameRange = TextRange.create(nameStart + offset, nameEnd + offset)
            val defType = definitionType
            if (defType.isNullOrEmpty()) {
                // 无法推断定义类型，退化为错误标记
                nodes += ParadoxErrorTokenNode(nameText, nameRange, configGroup)
                return
            }
            val defExpr = CwtDataExpression.resolve("definition[$defType]", true)
            nodes += ParadoxTemplateSnippetNode.resolve(nameText, nameRange, configGroup, defExpr)
        }

        fun findMatchingBracket(startIndex: Int, endExclusive: Int): Int {
            var depth = 0
            var i = startIndex
            while (i < endExclusive) {
                val ch = text[i]
                if (!inParam(i)) {
                    when (ch) {
                        '[' -> depth++
                        ']' -> {
                            depth--
                            if (depth == 0) return i
                        }
                    }
                }
                i++
            }
            return -1
        }

        fun findMatchingBrace(startIndex: Int, endExclusive: Int): Int {
            var depth = 0
            var i = startIndex
            while (i < endExclusive) {
                val ch = text[i]
                if (!inParam(i)) {
                    when (ch) {
                        '{' -> depth++
                        '}' -> {
                            depth--
                            if (depth == 0) return i
                        }
                    }
                }
                i++
            }
            return -1
        }

        fun isIdentifierChar(ch: Char): Boolean {
            return ch.isLetterOrDigit() || ch == '_' || ch == '-' || ch == '.' || ch == '\''
        }

        fun parseContent(start: Int, end: Int) {
            var segStart = start
            var i = start
            while (i < end) {
                if (inParam(i)) { i++; continue }
                when (val ch = text[i]) {
                    '<' -> {
                        addConstant(segStart, i)
                        val close = text.indexOf('>', i + 1).takeIf { it in (i + 1)..<end } ?: -1
                        if (close == -1) {
                            val nodeText = text.substring(i, end)
                            nodes += ParadoxErrorTokenNode(nodeText, TextRange.create(i + offset, end + offset), configGroup)
                            return
                        }
                        addDefinition(i + 1, close)
                        i = close + 1
                        segStart = i
                        continue
                    }
                    '[' -> {
                        addConstant(segStart, i)
                        val close = findMatchingBracket(i, end)
                        if (close == -1) {
                            val nodeText = text.substring(i, end)
                            nodes += ParadoxErrorTokenNode(nodeText, TextRange.create(i + offset, end + offset), configGroup)
                            return
                        }
                        val innerText = text.substring(i + 1, close)
                        val innerRange = TextRange.create(i + 1 + offset, close + offset)
                        val cmd = ParadoxCommandExpression.resolve(innerText, innerRange, configGroup)
                            ?: ParadoxErrorTokenNode(innerText, innerRange, configGroup)
                        nodes += cmd
                        i = close + 1
                        segStart = i
                        continue
                    }
                    '{' -> {
                        addConstant(segStart, i)
                        // nested block
                        val close = findMatchingBrace(i, end)
                        if (close == -1) {
                            nodes += ParadoxMarkerNode("{", TextRange.create(i + offset, i + 1 + offset), configGroup)
                            val nodeText = text.substring(i + 1, end)
                            nodes += ParadoxErrorTokenNode(nodeText, TextRange.create(i + 1 + offset, end + offset), configGroup)
                            return
                        }
                        nodes += ParadoxMarkerNode("{", TextRange.create(i + offset, i + 1 + offset), configGroup)
                        parseContent(i + 1, close)
                        nodes += ParadoxMarkerNode("}", TextRange.create(close + offset, close + 1 + offset), configGroup)
                        i = close + 1
                        segStart = i
                        continue
                    }
                    else -> {
                        // try to parse an identifier for localisation call
                        if (isIdentifierChar(ch)) {
                            var j = i + 1
                            while (j < end && !inParam(j) && isIdentifierChar(text[j])) j++
                            // treat as localisation name only if it looks like an identifier
                            if (text.substring(i, j).isParameterAwareIdentifier('.', '-', '\'')) {
                                addConstant(segStart, i)
                                addLocalisation(i, j)
                                i = j
                                segStart = i
                                continue
                            }
                        }
                        // fallthrough: keep scanning
                        i++
                    }
                }
            }
            addConstant(segStart, i)
        }

        // entry: prefer a single root block { ... }
        if (textLength >= 1 && text[0] == '{') {
            nodes += ParadoxMarkerNode("{", TextRange.create(offset, offset + 1), configGroup)
            val endIndex = if (textLength >= 2 && text.last() == '}') textLength - 1 else textLength
            parseContent(1, endIndex)
            if (endIndex == textLength - 1) nodes += ParadoxMarkerNode("}", TextRange.create(offset + endIndex, offset + endIndex + 1), configGroup)
        } else {
            if (!incomplete) return null
            parseContent(0, textLength)
        }

        expression.finishResolving()
        return expression
    }
}

private class StellarisNameFormatExpressionImpl(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val config: CwtConfig<*>,
    override val formatName: String?,
    override val definitionType: String?,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
) : ParadoxComplexExpressionBase(), StellarisNameFormatExpression {
    override val errors: List<ParadoxComplexExpressionError> by lazy { validate() }

    private fun validate(): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        // 基础结构校验：占位与引用名必须是标识符。
        @Suppress("RemoveExplicitTypeArguments")
        val ok = validateAllNodes(errors) {
            when {
                it is ParadoxTemplateSnippetNode -> it.text.isParameterAwareIdentifier('.', '-', '\'')
                it is ParadoxDataSourceNode -> it.text.isParameterAwareIdentifier()
                else -> true
            }
        }
        // 目前未提供专属 malformed 错误码，保留节点级错误（未解析等）。
        ok // ignore result, collected errors already include unresolved ones via getAllErrors
        return errors
    }

    override fun equals(other: Any?) = this === other || other is StellarisNameFormatExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}
