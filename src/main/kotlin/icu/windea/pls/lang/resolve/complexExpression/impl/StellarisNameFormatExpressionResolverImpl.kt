package icu.windea.pls.lang.resolve.complexExpression.impl

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.isParameterAwareIdentifier
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionBase
import icu.windea.pls.lang.resolve.complexExpression.StellarisNameFormatExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxBlankNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxCommandNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxErrorTokenNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxMarkerNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNameFormatClosureNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNameFormatDefinitionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNameFormatLocalisationNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNameFormatTextNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.StellarisNamePartNode
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionValidator

/**
 * 解析器：Stellaris 命名格式表达式。
 */
internal class StellarisNameFormatExpressionResolverImpl : StellarisNameFormatExpression.Resolver {
    override fun resolve(text: String, range: TextRange?, configGroup: CwtConfigGroup, config: CwtConfig<*>): StellarisNameFormatExpression? {
        val configExpression = config.configExpression ?: return null
        if (configExpression.type != CwtDataTypes.StellarisNameFormat) return null

        val incomplete = PlsStates.incompleteComplexExpression.get() ?: false
        if (!incomplete && text.isEmpty()) return null

        val formatName = configExpression.value
        val definitionType = formatName?.let { "${it}_name_parts_list" }

        val nodes = mutableListOf<ParadoxComplexExpressionNode>()
        val range = range ?: TextRange.create(0, text.length)
        val expression = StellarisNameFormatExpressionImpl(text, range, configGroup, config, nodes)

        val offset = range.startOffset
        val textLength = text.length

        // 对于命名格式：不将任何位置视为“参数区间”，以免误将 [ ... ] 内容整体跳过
        @Suppress("UNUSED_PARAMETER")
        fun inParam(i: Int): Boolean = false

        fun addConstant(targetNodes: MutableList<ParadoxComplexExpressionNode>, s: Int, e: Int) {
            if (e <= s) return
            var k = s
            while (k < e) {
                // blanks
                val bStart = k
                while (k < e && text[k].isWhitespace()) k++
                if (k > bStart) {
                    val nodeText = text.substring(bStart, k)
                    val nodeRange = TextRange.create(bStart + offset, k + offset)
                    targetNodes += ParadoxBlankNode(nodeText, nodeRange, configGroup)
                }
                // non-blanks
                val tStart = k
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

        fun endsWithErrorToken(node: ParadoxComplexExpressionNode, expectedEndOffset: Int): Boolean {
            // direct
            if (node is ParadoxErrorTokenNode && node.rangeInExpression.endOffset == expectedEndOffset) return true
            // recursive on children
            val cs = node.nodes
            if (cs.isEmpty()) return false
            return endsWithErrorToken(cs.last(), expectedEndOffset)
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
                            // consume until whitespace or end, then bubble up
                            var innerEnd = i + 1
                            while (innerEnd < end && !text[innerEnd].isWhitespace()) innerEnd++
                            // parse inner as definition node (to support completion), but no closing '>' — mark unmatched
                            children += buildDefinitionNode(i + 1, innerEnd)
                            // add empty error token INSIDE the name part node to mark unmatched '<'
                            children += ParadoxErrorTokenNode("", TextRange.create(innerEnd + offset, innerEnd + offset), configGroup)
                            val wrap = StellarisNamePartNode(text.substring(i, innerEnd), TextRange.create(i + offset, innerEnd + offset), configGroup, children)
                            targetNodes += wrap
                            // ensure trailing error token at current layer end when unmatched and reached end
                            if (innerEnd == end && !endsWithErrorToken(targetNodes.last(), end + offset)) {
                                targetNodes += ParadoxErrorTokenNode("", TextRange.create(end + offset, end + offset), configGroup)
                            }
                            i = innerEnd
                            segStart = i
                            continue
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
                    '>' -> {
                        // stray close marker inside content
                        addConstant(targetNodes, segStart, i)
                        targetNodes += ParadoxErrorTokenNode(">", TextRange.create(i + offset, i + 1 + offset), configGroup)
                        i += 1
                        segStart = i
                        continue
                    }
                    '[' -> {
                        addConstant(targetNodes, segStart, i)
                        val children = mutableListOf<ParadoxComplexExpressionNode>()
                        // add marker for '['
                        children += ParadoxMarkerNode("[", TextRange.create(i + offset, i + 1 + offset), configGroup)
                        val close = findMatchingBracket(i, end)
                        if (close == -1) {
                            // parse command until whitespace or end, then bubble up
                            var innerEnd = i + 1
                            while (innerEnd < end && !text[innerEnd].isWhitespace()) innerEnd++
                            val innerText = text.substring(i + 1, innerEnd)
                            val innerRange = TextRange.create(i + 1 + offset, innerEnd + offset)
                            val cmd = ParadoxCommandExpression.resolve(innerText, innerRange, configGroup)
                                ?: ParadoxErrorTokenNode(innerText, innerRange, configGroup)
                            children += cmd
                            // add empty error token inside the command node to mark unmatched
                            children += ParadoxErrorTokenNode("", TextRange.create(innerEnd + offset, innerEnd + offset), configGroup)
                            val wrap = ParadoxCommandNode(text.substring(i, innerEnd), TextRange.create(i + offset, innerEnd + offset), configGroup, children)
                            targetNodes += wrap
                            // ensure trailing error token at current layer end when unmatched and reached end
                            if (innerEnd == end && !endsWithErrorToken(targetNodes.last(), end + offset)) {
                                targetNodes += ParadoxErrorTokenNode("", TextRange.create(end + offset, end + offset), configGroup)
                            }
                            i = innerEnd
                            segStart = i
                            continue
                        }
                        val innerText = text.substring(i + 1, close)
                        val innerRange = TextRange.create(i + 1 + offset, close + offset)
                        val cmd = ParadoxCommandExpression.resolve(innerText, innerRange, configGroup)
                            ?: ParadoxErrorTokenNode(innerText, innerRange, configGroup)
                        children += cmd
                        // add marker for ']'
                        children += ParadoxMarkerNode("]", TextRange.create(close + offset, close + 1 + offset), configGroup)
                        val wrap = ParadoxCommandNode(text.substring(i, close + 1), TextRange.create(i + offset, close + 1 + offset), configGroup, children)
                        targetNodes += wrap
                        i = close + 1
                        segStart = i
                        continue
                    }
                    ']' -> {
                        // stray close marker inside content
                        addConstant(targetNodes, segStart, i)
                        targetNodes += ParadoxErrorTokenNode("]", TextRange.create(i + offset, i + 1 + offset), configGroup)
                        i += 1
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
                            // parse inner content to keep inner markers (e.g. '<')
                            parseContent(i + 1, end, children)
                            // ensure trailing error token on this layer
                            if (children.isEmpty() || !endsWithErrorToken(children.last(), end + offset)) {
                                children += ParadoxErrorTokenNode("", TextRange.create(end + offset, end + offset), configGroup)
                            }
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
                    '}' -> {
                        // stray close marker inside content
                        addConstant(targetNodes, segStart, i)
                        targetNodes += ParadoxErrorTokenNode("}", TextRange.create(i + offset, i + 1 + offset), configGroup)
                        i += 1
                        segStart = i
                        continue
                    }
                    else -> {
                        // try to parse an identifier for localisation call
                        if (isIdentifierChar(ch)) {
                            var j = i + 1
                            while (j < end && !inParam(j) && isIdentifierChar(text[j])) j++
                            // treat as localisation name only if it looks like an identifier
                            if (text.substring(i, j).isParameterAwareIdentifier(".-'")) {
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

        fun parseTopLevel(start: Int, end: Int) {
            var i = start
            while (i < end) {
                val ch = text[i]
                // collect blanks
                if (ch.isWhitespace()) {
                    var b = i + 1
                    while (b < end && text[b].isWhitespace()) b++
                    val nodeText = text.substring(i, b)
                    val nodeRange = TextRange.create(i + offset, b + offset)
                    nodes += ParadoxBlankNode(nodeText, nodeRange, configGroup)
                    i = b
                    continue
                }
                // closure
                if (ch == '{') {
                    val close = findMatchingBrace(i, end)
                    val children = mutableListOf<ParadoxComplexExpressionNode>()
                    if (close == -1) {
                        children += ParadoxMarkerNode("{", TextRange.create(i + offset, i + 1 + offset), configGroup)
                        // parse inner structured content; keep markers like '<' and '['
                        parseContent(i + 1, end, children)
                        // ensure trailing empty error only if last child does not already end at this layer with error
                        if (children.isEmpty() || !endsWithErrorToken(children.last(), end + offset)) {
                            children += ParadoxErrorTokenNode("", TextRange.create(end + offset, end + offset), configGroup)
                        }
                        val wrap = StellarisNameFormatClosureNode(text.substring(i, end), TextRange.create(i + offset, end + offset), configGroup, children)
                        nodes += wrap
                        return
                    }
                    children += ParadoxMarkerNode("{", TextRange.create(i + offset, i + 1 + offset), configGroup)
                    parseContent(i + 1, close, children)
                    children += ParadoxMarkerNode("}", TextRange.create(close + offset, close + 1 + offset), configGroup)
                    val wrap = StellarisNameFormatClosureNode(text.substring(i, close + 1), TextRange.create(i + offset, close + 1 + offset), configGroup, children)
                    nodes += wrap
                    i = close + 1
                    continue
                }
                // otherwise: error token until next blank or next '{'
                var j = i + 1
                while (j < end && !text[j].isWhitespace() && text[j] != '{') j++
                val nodeText = text.substring(i, j)
                val nodeRange = TextRange.create(i + offset, j + offset)
                nodes += ParadoxErrorTokenNode(nodeText, nodeRange, configGroup)
                i = j
            }
        }

        // entry: if there is no '{' at all, treat leading/trailing blanks separately and middle as one error token
        if (textLength > 0 && !text.contains('{')) {
            var i = 0
            while (i < textLength && text[i].isWhitespace()) i++
            if (i > 0) {
                val t = text.substring(0, i)
                nodes += ParadoxBlankNode(t, TextRange.create(offset, offset + i), configGroup)
            }
            var j = textLength
            while (j > i && text[j - 1].isWhitespace()) j--
            if (j > i) {
                val t = text.substring(i, j)
                nodes += ParadoxErrorTokenNode(t, TextRange.create(offset + i, offset + j), configGroup)
            }
            if (j < textLength) {
                val t = text.substring(j, textLength)
                nodes += ParadoxBlankNode(t, TextRange.create(offset + j, offset + textLength), configGroup)
            }
        } else {
            parseTopLevel(0, textLength)
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
    override fun getErrors(element: ParadoxExpressionElement?) = ParadoxComplexExpressionValidator.validate(this, element)

    override fun equals(other: Any?) = this === other || other is StellarisNameFormatExpression && text == other.text
    override fun hashCode() = text.hashCode()
    override fun toString() = text
}
