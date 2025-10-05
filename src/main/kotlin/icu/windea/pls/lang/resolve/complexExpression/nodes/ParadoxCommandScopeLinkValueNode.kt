package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.isEscapedCharAt
import icu.windea.pls.core.isQuoted
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.localisation.editor.ParadoxLocalisationAttributesKeys

class ParadoxCommandScopeLinkValueNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    val linkConfigs: List<CwtLinkConfig>,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
) : ParadoxComplexExpressionNodeBase(), ParadoxLinkValueNode {
    override val argumentNodes: List<ParadoxComplexExpressionNode>
        get() = nodes.filter { it !is ParadoxBlankNode && it !is ParadoxMarkerNode }

    override fun getRelatedConfigs(): Collection<CwtConfig<*>> {
        return linkConfigs
    }

    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return ParadoxLocalisationAttributesKeys.COMMAND_SCOPE_LINK_VALUE_KEY
    }

    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, linkConfigs: List<CwtLinkConfig>): ParadoxCommandScopeLinkValueNode {
            // text may contain parameters & may be an argument list inside parentheses
            // Support multi-args separated by commas with optional blanks and single-quoted literal arguments.

            val parameterRanges = ParadoxExpressionManager.getParameterRanges(text)

            val nodes = mutableListOf<ParadoxComplexExpressionNode>()

            fun resolveSingle(coreText: String, coreRange: TextRange, cfgs: List<CwtLinkConfig>) {
                val node = ParadoxDataSourceNode.resolve(coreText, coreRange, configGroup, cfgs)
                nodes += node
            }

            // probe top-level commas
            var hasTopLevelComma = false
            run {
                var i = 0
                var depthParen = 0
                var inSingleQuote = false
                while (i < text.length) {
                    val ch = text[i]
                    val inParam = parameterRanges.any { i in it }
                    if (!inParam) {
                        if (ch == '\'' && !text.isEscapedCharAt(i)) inSingleQuote = !inSingleQuote
                        else if (!inSingleQuote) when (ch) {
                            '(' -> depthParen++
                            ')' -> if (depthParen > 0) depthParen--
                            ',' -> if (depthParen == 0) {
                                hasTopLevelComma = true; return@run
                            }
                        }
                    }
                    i++
                }
            }

            if (!hasTopLevelComma) {
                val cfgs = linkConfigs.mapNotNull { CwtLinkConfig.delegatedWith(it, 0) }.ifEmpty { linkConfigs }
                resolveSingle(text, textRange, cfgs)
                return ParadoxCommandScopeLinkValueNode(text, textRange, configGroup, linkConfigs, nodes)
            }

            // split by top-level commas, preserving blanks and comma markers
            val offset = textRange.startOffset
            var startIndex = 0
            var i = 0
            var depthParen = 0
            var inSingleQuote = false
            var argIndex = 0
            fun emitSegment(endExclusive: Int, fromComma: Boolean) {
                // leading blanks
                var a = startIndex
                while (a < endExclusive && text[a].isWhitespace()) a++
                if (a > startIndex) {
                    val blankRange = TextRange.create(startIndex + offset, a + offset)
                    nodes += ParadoxBlankNode(text.substring(startIndex, a), blankRange, configGroup)
                }
                // core
                var b = endExclusive - 1
                while (b >= a && text[b].isWhitespace()) b--
                if (b >= a) {
                    val coreText = text.substring(a, b + 1)
                    val coreRange = TextRange.create(a + offset, b + 1 + offset)
                    if (coreText.isQuoted('\'')) {
                        nodes += ParadoxStringLiteralNode(coreText, coreRange, configGroup)
                    } else {
                        val cfgs = linkConfigs.mapNotNull { CwtLinkConfig.delegatedWith(it, argIndex) }.ifEmpty { linkConfigs }
                        resolveSingle(coreText, coreRange, cfgs)
                    }
                } else if (fromComma) {
                    // empty argument -> insert error token node at the position before comma
                    val p = startIndex + offset
                    nodes += ParadoxErrorTokenNode("", TextRange.create(p, p), configGroup)
                }
                // trailing blanks
                if (b + 1 < endExclusive) {
                    val blankRange2 = TextRange.create(b + 1 + offset, endExclusive + offset)
                    nodes += ParadoxBlankNode(text.substring(b + 1, endExclusive), blankRange2, configGroup)
                }
                argIndex++
            }
            while (i < text.length) {
                val ch = text[i]
                val inParam = parameterRanges.any { i in it }
                if (!inParam) {
                    if (ch == '\'' && !text.isEscapedCharAt(i)) inSingleQuote = !inSingleQuote
                    else if (!inSingleQuote) when (ch) {
                        '(' -> depthParen++
                        ')' -> if (depthParen > 0) depthParen--
                        ',' -> if (depthParen == 0) {
                            emitSegment(i, true)
                            // emit comma marker
                            nodes += ParadoxMarkerNode(",", TextRange.create(i + offset, i + 1 + offset), configGroup)
                            startIndex = i + 1
                        }
                    }
                }
                i++
            }
            emitSegment(text.length, false)
            return ParadoxCommandScopeLinkValueNode(text, textRange, configGroup, linkConfigs, nodes)
        }
    }

    companion object : Resolver()
}
