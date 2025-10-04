package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.isEscapedCharAt
import icu.windea.pls.core.isQuoted
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxDynamicValueExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScopeFieldExpression
import icu.windea.pls.lang.resolve.complexExpression.ParadoxScriptValueExpression
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys

class ParadoxValueFieldValueNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    val linkConfigs: List<CwtLinkConfig>,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
) : ParadoxComplexExpressionNodeBase() {
    override fun getRelatedConfigs(): Collection<CwtConfig<*>> {
        return linkConfigs
    }

    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return ParadoxScriptAttributesKeys.VALUE_FIELD_VALUE_KEY
    }

    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, linkConfigs: List<CwtLinkConfig>): ParadoxValueFieldValueNode {
            // text may contain parameters & may be an argument list inside parentheses for fromArgument links
            // child node can be:
            // * ParadoxDynamicValueExpression / ParadoxScopeFieldExpression / ParadoxScriptValueExpression / ParadoxDataSourceNode
            // Additionally, when splitting arguments: ParadoxBlankNode and ParadoxMarkerNode(",") and ParadoxStringLiteralNode

            val parameterRanges = ParadoxExpressionManager.getParameterRanges(text)

            val nodes = mutableListOf<ParadoxComplexExpressionNode>()

            fun resolveSingle(coreText: String, coreRange: TextRange) {
                // precedence: DynamicValue -> ScopeField -> ScriptValue -> DataSource
                run r1@{
                    val configs = linkConfigs.filter { it.configExpression?.type in CwtDataTypeGroups.DynamicValue }
                    if (configs.isEmpty()) return@r1
                    val node = ParadoxDynamicValueExpression.resolve(coreText, coreRange, configGroup, configs)
                    if (node != null) {
                        nodes += node; return
                    }
                }
                run r1@{
                    val configs = linkConfigs.filter { it.configExpression?.type in CwtDataTypeGroups.ScopeField }
                    if (configs.isEmpty()) return@r1
                    val node = ParadoxScopeFieldExpression.resolve(coreText, coreRange, configGroup)
                    if (node != null) {
                        nodes += node; return
                    }
                }
                run r1@{
                    if (!coreText.contains('|')) return@r1
                    val scriptValueConfig = linkConfigs.find { it.name == "script_value" }
                    if (scriptValueConfig == null) return@r1
                    val node = ParadoxScriptValueExpression.resolve(coreText, coreRange, configGroup, scriptValueConfig)
                    if (node != null) {
                        nodes += node; return
                    }
                }
                run r1@{
                    val node = ParadoxDataSourceNode.resolve(coreText, coreRange, configGroup, linkConfigs)
                    nodes += node
                }
            }

            // probe top-level commas to see if it's an argument list
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
                // original single-value path
                resolveSingle(text, textRange)
                return ParadoxValueFieldValueNode(text, textRange, configGroup, linkConfigs, nodes)
            }

            // argument list path: split by top-level commas, preserve blanks and comma markers
            val offset = textRange.startOffset
            var startIndex = 0
            var i = 0
            var depthParen = 0
            var inSingleQuote = false
            fun emitSegment(endExclusive: Int) {
                if (endExclusive <= startIndex) return
                // leading blanks
                var a = startIndex
                while (a < endExclusive && text[a].isWhitespace()) a++
                if (a > startIndex) nodes += ParadoxBlankNode(text.substring(startIndex, a), TextRange.create(startIndex + offset, a + offset), configGroup)
                // core
                var b = endExclusive - 1
                while (b >= a && text[b].isWhitespace()) b--
                if (b >= a) {
                    val coreText = text.substring(a, b + 1)
                    val coreRange = TextRange.create(a + offset, b + 1 + offset)
                    if (coreText.isQuoted('\'')) nodes += ParadoxStringLiteralNode(coreText, coreRange, configGroup)
                    else resolveSingle(coreText, coreRange)
                }
                // trailing blanks
                if (b + 1 < endExclusive) nodes += ParadoxBlankNode(text.substring(b + 1, endExclusive), TextRange.create(b + 1 + offset, endExclusive + offset), configGroup)
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
                            emitSegment(i)
                            nodes += ParadoxMarkerNode(",", TextRange.create(i + offset, i + 1 + offset), configGroup)
                            startIndex = i + 1
                        }
                    }
                }
                i++
            }
            emitSegment(text.length)
            return ParadoxValueFieldValueNode(text, textRange, configGroup, linkConfigs, nodes)
        }
    }

    companion object : Resolver()
}
