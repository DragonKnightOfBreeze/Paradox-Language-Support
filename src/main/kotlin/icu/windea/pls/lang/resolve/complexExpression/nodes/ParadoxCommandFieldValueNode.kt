package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.isEscapedCharAt
import icu.windea.pls.core.isQuoted
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.localisation.editor.ParadoxLocalisationAttributesKeys

class ParadoxCommandFieldValueNode(
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
        return ParadoxLocalisationAttributesKeys.COMMAND_FIELD_VALUE_KEY
    }

    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, linkConfigs: List<CwtLinkConfig>): ParadoxCommandFieldValueNode {
            val incomplete = PlsStates.incompleteComplexExpression.get() ?: false
            val parameterRanges = ParadoxExpressionManager.getParameterRanges(text)
            val separatorChar = if (linkConfigs.any { it.argumentSeparator.usePipe() }) '|' else ','

            val nodes = mutableListOf<ParadoxComplexExpressionNode>()

            // detect top-level separators to decide whether it's an argument list
            var hasTopLevelSeparator = false
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
                            separatorChar -> if (depthParen == 0) {
                                hasTopLevelSeparator = true; return@run
                            }
                        }
                    }
                    i++
                }
            }

            if (!hasTopLevelSeparator) {
                // original single-value resolution path
                val linkConfigsForDs = linkConfigs.mapNotNull { CwtLinkConfig.delegatedWith(it, 0) }.ifEmpty { linkConfigs }
                nodes += resolveDsNode(text, textRange, configGroup, linkConfigsForDs)
                return ParadoxCommandFieldValueNode(text, textRange, configGroup, linkConfigs, nodes)
            }

            // argument list path: split by top-level separators, emit blanks and markers
            val offset = textRange.startOffset
            var startIndex = 0
            var i = 0
            var depthParen = 0
            var inSingleQuote = false
            var argIndex = 0
            fun emitSegment(endExclusive: Int, fromSeparator: Boolean) {
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
                        val linkConfigsForDs = linkConfigs.mapNotNull { CwtLinkConfig.delegatedWith(it, argIndex) }.ifEmpty { linkConfigs }
                        nodes += resolveDsNode(coreText, coreRange, configGroup, linkConfigsForDs)
                    }
                } else if (fromSeparator) {
                    // empty argument -> insert error token node at the position before separator
                    val p = startIndex + offset
                    nodes += ParadoxErrorTokenNode("", TextRange.create(p, p), configGroup)
                } else if (incomplete) {
                    // trailing empty argument in incomplete mode -> emit an empty argument node via resolveSingle
                    val coreRange = TextRange.create(a + offset, a + offset)
                    val linkConfigsForDs = linkConfigs.mapNotNull { CwtLinkConfig.delegatedWith(it, argIndex) }.ifEmpty { linkConfigs }
                    nodes += resolveDsNode("", coreRange, configGroup, linkConfigsForDs)
                }
                // trailing blanks
                if (b + 1 < endExclusive) {
                    val blankRange = TextRange.create(b + 1 + offset, endExclusive + offset)
                    nodes += ParadoxBlankNode(text.substring(b + 1, endExclusive), blankRange, configGroup)
                }
                argIndex++
            }
            while (i < text.length) {
                val c = text[i]
                val inParam = parameterRanges.any { i in it }
                if (!inParam) {
                    if (c == '\'' && !text.isEscapedCharAt(i)) inSingleQuote = !inSingleQuote
                    else if (!inSingleQuote) when (c) {
                        '(' -> depthParen++
                        ')' -> if (depthParen > 0) depthParen--
                        separatorChar -> if (depthParen == 0) {
                            emitSegment(i, true)
                            // emit separator marker
                            nodes += ParadoxMarkerNode(c.toString(), TextRange.create(i + offset, i + 1 + offset), configGroup)
                            startIndex = i + 1
                        }
                    }
                }
                i++
            }
            emitSegment(text.length, false)
            return ParadoxCommandFieldValueNode(text, textRange, configGroup, linkConfigs, nodes)
        }

        private fun resolveDsNode(text: String, textRange: TextRange, configGroup: CwtConfigGroup, configs: List<CwtLinkConfig>): ParadoxComplexExpressionNode {
            configs.filter { it.configExpression?.type == CwtDataTypes.Command }.orNull()
                ?.let { ParadoxCommandExpression.resolve(text, textRange, configGroup) }
                ?.let { return it }
            return ParadoxDataSourceNode.resolve(text, textRange, configGroup, configs)
        }
    }

    companion object : Resolver()
}
