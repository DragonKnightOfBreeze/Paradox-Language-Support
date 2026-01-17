package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.isEscapedCharAt
import icu.windea.pls.core.isQuoted
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDynamicValueExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys

class ParadoxScopeLinkValueNode(
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
        return ParadoxScriptAttributesKeys.SCOPE_LINK_VALUE_KEY
    }

    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, linkConfigs: List<CwtLinkConfig>): ParadoxScopeLinkValueNode {
            val incomplete = PlsStates.incompleteComplexExpression.get() ?: false
            val parameterRanges = ParadoxExpressionManager.getParameterRanges(text)
            val separatorChar = if(linkConfigs.any { it.argumentSeparator.usePipe() }) '|' else ','

            val nodes = mutableListOf<ParadoxComplexExpressionNode>()

            fun resolveSingle(coreText: String, coreRange: TextRange, cfgs: List<CwtLinkConfig>) {
                run {
                    val configs = cfgs.filter { it.configExpression?.type in CwtDataTypeSets.DynamicValue }
                    if (configs.isEmpty()) return@run
                    val node = ParadoxDynamicValueExpression.resolve(coreText, coreRange, configGroup, configs) ?: return@run
                    nodes += node
                    return
                }
                run {
                    val configs = cfgs.filter { it.configExpression?.type in CwtDataTypeSets.ScopeField }
                    if (configs.isEmpty()) return@run
                    val node = ParadoxScopeFieldExpression.resolve(coreText, coreRange, configGroup) ?: return@run
                    nodes += node
                    return
                }
                run {
                    val node = ParadoxDataSourceNode.resolve(coreText, coreRange, configGroup, cfgs)
                    nodes += node
                }
            }

            // Detect top-level commas to decide whether it's an argument list
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
                            separatorChar -> if (depthParen == 0) {
                                hasTopLevelComma = true; return@run
                            }
                        }
                    }
                    i++
                }
            }

            if (!hasTopLevelComma) {
                // original single-value resolution path
                val cfgs = linkConfigs.mapNotNull { CwtLinkConfig.delegatedWith(it, 0) }.ifEmpty { linkConfigs }
                resolveSingle(text, textRange, cfgs)
                return ParadoxScopeLinkValueNode(text, textRange, configGroup, linkConfigs, nodes)
            }

            // Argument list path: split by top-level commas, emit blanks and markers
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
                } else if (incomplete) {
                    // trailing empty argument in incomplete mode -> emit an empty argument node via resolveSingle
                    val coreRange = TextRange.create(a + offset, a + offset)
                    val cfgs = linkConfigs.mapNotNull { CwtLinkConfig.delegatedWith(it, argIndex) }.ifEmpty { linkConfigs }
                    resolveSingle("", coreRange, cfgs)
                }
                // trailing blanks
                if (b + 1 < endExclusive) {
                    val blankRange2 = TextRange.create(b + 1 + offset, endExclusive + offset)
                    nodes += ParadoxBlankNode(text.substring(b + 1, endExclusive), blankRange2, configGroup)
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
                            // emit comma marker
                            nodes += ParadoxMarkerNode(c.toString(), TextRange.create(i + offset, i + 1 + offset), configGroup)
                            startIndex = i + 1
                        }
                    }
                }
                i++
            }
            emitSegment(text.length, false)
            return ParadoxScopeLinkValueNode(text, textRange, configGroup, linkConfigs, nodes)
        }
    }

    companion object : Resolver()
}
