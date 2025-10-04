package icu.windea.pls.lang.resolve.complexExpression.impl

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionBase
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionErrorBuilder
import icu.windea.pls.lang.resolve.complexExpression.StellarisNameFormatExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxBlankNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorTokenNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNameFormatDefinitionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNameFormatClosureNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNamePartNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNameFormatLocalisationNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNameFormatTextNode
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
        val expression = StellarisNameFormatExpressionImpl(text, range, configGroup, config, nodes)

        val offset = range.startOffset
        val textLength = text.length

        // 对于命名格式：不将任何位置视为“参数区间”，以免误将 [ ... ] 内容整体跳过
        fun inParam(i: Int): Boolean = false

        fun addConstant(targetNodes: MutableList<ParadoxComplexExpressionNode>, s: Int, e: Int) {
            if (e <= s) return
            var k = s
            while (k < e) {
                // blanks
                var bStart = k
                while (k < e && text[k].isWhitespace()) k++
                if (k > bStart) {
                    val nodeText = text.substring(bStart, k)
                    val nodeRange = TextRange.create(bStart + offset, k + offset)
                    targetNodes += ParadoxBlankNode(nodeText, nodeRange, configGroup)
                }
                // non-blanks
                var tStart = k
                while (k < e && !text[k].isWhitespace()) k++
                if (k > tStart) {
                    val nodeText = text.substring(tStart, k)
                    val nodeRange = TextRange.create(tStart + offset, k + offset)
                    targetNodes += StellarisNameFormatTextNode.resolve(nodeText, nodeRange, configGroup)
                }
            }
        }

        fun addLocalisation(targetNodes: MutableList<ParadoxComplexExpressionNode>, nameStart: Int, nameEnd: Int) {
            if (nameEnd <= nameStart) return
            val nameText = text.substring(nameStart, nameEnd)
            val nameRange = TextRange.create(nameStart + offset, nameEnd + offset)
            targetNodes += StellarisNameFormatLocalisationNode.resolve(nameText, nameRange, configGroup)
        }

        fun buildDefinitionNode(nameStart: Int, nameEnd: Int): ParadoxComplexExpressionNode {
            if (nameEnd <= nameStart) return ParadoxErrorTokenNode("", TextRange.create(nameStart + offset, nameStart + offset), configGroup)
            val nameText = text.substring(nameStart, nameEnd)
            val nameRange = TextRange.create(nameStart + offset, nameEnd + offset)
            val defType = definitionType
            return if (defType.isNullOrEmpty()) ParadoxErrorTokenNode(nameText, nameRange, configGroup)
            else StellarisNameFormatDefinitionNode.resolve(nameText, nameRange, configGroup, defType)
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

        fun parseContent(start: Int, end: Int, targetNodes: MutableList<ParadoxComplexExpressionNode>) {
            var segStart = start
            var i = start
            while (i < end) {
                if (inParam(i)) {
                    i++; continue
                }
                when (val ch = text[i]) {
                    '<' -> {
                        addConstant(targetNodes, segStart, i)
                        val children = mutableListOf<ParadoxComplexExpressionNode>()
                        // add marker for '<'
                        children += ParadoxMarkerNode("<", TextRange.create(i + offset, i + 1 + offset), configGroup)
                        val close = text.indexOf('>', i + 1).takeIf { it in (i + 1)..<end } ?: -1
                        if (close == -1) {
                            // content as error token without including '<'
                            val nodeText = text.substring(i + 1, end)
                            children += ParadoxErrorTokenNode(nodeText, TextRange.create(i + 1 + offset, end + offset), configGroup)
                            // wrap into name-parts list node
                            val wrap = StellarisNamePartNode(text.substring(i, end), TextRange.create(i + offset, end + offset), configGroup, children)
                            targetNodes += wrap
                            return
                        }
                        children += buildDefinitionNode(i + 1, close)
                        // add marker for '>'
                        children += ParadoxMarkerNode(">", TextRange.create(close + offset, close + 1 + offset), configGroup)
                        val wrap = StellarisNamePartNode(text.substring(i, close + 1), TextRange.create(i + offset, close + 1 + offset), configGroup, children)
                        targetNodes += wrap
                        i = close + 1
                        segStart = i
                        continue
                    }
                    '[' -> {
                        addConstant(targetNodes, segStart, i)
                        // add marker for '['
                        targetNodes += ParadoxMarkerNode("[", TextRange.create(i + offset, i + 1 + offset), configGroup)
                        val close = findMatchingBracket(i, end)
                        if (close == -1) {
                            // content as error token without including '['
                            val nodeText = text.substring(i + 1, end)
                            targetNodes += ParadoxErrorTokenNode(nodeText, TextRange.create(i + 1 + offset, end + offset), configGroup)
                            return
                        }
                        val innerText = text.substring(i + 1, close)
                        val innerRange = TextRange.create(i + 1 + offset, close + offset)
                        val cmd = ParadoxCommandExpression.resolve(innerText, innerRange, configGroup)
                            ?: ParadoxErrorTokenNode(innerText, innerRange, configGroup)
                        targetNodes += cmd
                        // add marker for ']'
                        targetNodes += ParadoxMarkerNode("]", TextRange.create(close + offset, close + 1 + offset), configGroup)
                        i = close + 1
                        segStart = i
                        continue
                    }
                    '{' -> {
                        addConstant(targetNodes, segStart, i)
                        // nested block -> wrap as closure node
                        val close = findMatchingBrace(i, end)
                        val children = mutableListOf<ParadoxComplexExpressionNode>()
                        if (close == -1) {
                            children += ParadoxMarkerNode("{", TextRange.create(i + offset, i + 1 + offset), configGroup)
                            val nodeText = text.substring(i + 1, end)
                            children += ParadoxErrorTokenNode(nodeText, TextRange.create(i + 1 + offset, end + offset), configGroup)
                            val wrap = StellarisNameFormatClosureNode(text.substring(i, end), TextRange.create(i + offset, end + offset), configGroup, children)
                            targetNodes += wrap
                            return
                        }
                        children += ParadoxMarkerNode("{", TextRange.create(i + offset, i + 1 + offset), configGroup)
                        parseContent(i + 1, close, children)
                        children += ParadoxMarkerNode("}", TextRange.create(close + offset, close + 1 + offset), configGroup)
                        val wrap = StellarisNameFormatClosureNode(text.substring(i, close + 1), TextRange.create(i + offset, close + 1 + offset), configGroup, children)
                        targetNodes += wrap
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
                                addConstant(targetNodes, segStart, i)
                                addLocalisation(targetNodes, i, j)
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
            addConstant(targetNodes, segStart, i)
        }

        // entry: prefer a single root block { ... }
        if (textLength >= 1 && text[0] == '{') {
            val endIndex = if (textLength >= 2 && text.last() == '}') textLength - 1 else textLength
            val children = mutableListOf<ParadoxComplexExpressionNode>()
            children += ParadoxMarkerNode("{", TextRange.create(offset, offset + 1), configGroup)
            parseContent(1, endIndex, children)
            if (endIndex == textLength - 1) children += ParadoxMarkerNode("}", TextRange.create(offset + endIndex, offset + endIndex + 1), configGroup)
            val wrap = StellarisNameFormatClosureNode(text.substring(0, if (endIndex == textLength - 1) textLength else textLength), TextRange.create(offset, offset + (if (endIndex == textLength - 1) textLength else textLength)), configGroup, children)
            nodes += wrap
        } else {
            if (!incomplete) return null
            parseContent(0, textLength, nodes)
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
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
) : ParadoxComplexExpressionBase(), StellarisNameFormatExpression {
    override val errors: List<ParadoxComplexExpressionError> by lazy { validate() }

    private fun validate(): List<ParadoxComplexExpressionError> {
        val errors = mutableListOf<ParadoxComplexExpressionError>()
        val result = validateAllNodes(errors) {
            when (it) {
                is StellarisNameFormatDefinitionNode -> it.text.isParameterAwareIdentifier()
                is StellarisNameFormatLocalisationNode -> it.text.isParameterAwareIdentifier('.', '-', '\'')
                else -> true
            }
        }
        val malformed = !result
        if (malformed) errors += ParadoxComplexExpressionErrorBuilder.malformedStellarisNameFormatExpression(rangeInExpression, text)
        return errors
    }

    override fun equals(other: Any?) = this === other || other is StellarisNameFormatExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}
