package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.indexOfNonBlank

class ParadoxNegatedDynamicValueNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val nodes: List<ParadoxComplexExpressionNode>,
    val configs: List<CwtConfig<*>>
) : ParadoxComplexExpressionNodeBase() {
    companion object {
        private const val NOT_TOKEN = "not"
        private const val NOT_TOKEN_LENGTH = NOT_TOKEN.length

        @JvmStatic
        fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup, configs: List<CwtConfig<*>>): ParadoxNegatedDynamicValueNode? {
            if (text.isEmpty()) return null
            if (!text.startsWith(NOT_TOKEN)) return null

            val incomplete = ChronicleThreadContext.incompleteComplexExpression.get() ?: false

            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            var start = 0
            var current = 0
            run fallback@{
                // TODO 2.1.10
                run {
                    // expect `not`
                    current = NOT_TOKEN_LENGTH
                    val node = ParadoxKeywordNode(NOT_TOKEN, TextRange.from(0, current), configGroup)
                    nodes += node
                }
                run {
                    // expect optional blank
                    start = current
                    current = text.indexOfNonBlank(start)
                    if (start == current) return@run
                    nodes += ParadoxBlankNode(text.substring(start, current), TextRange.create(start, current), configGroup)
                }
                run {
                    // expect `(`
                    start = current
                    current = if (text[current] == '(') current + 1 else -1
                    if (current == -1) return@fallback
                    nodes += ParadoxMarkerNode("(", TextRange.create(start, current), configGroup)
                }
                run {
                    // expect optional blank
                    start = current
                    current = text.indexOfNonBlank(start)
                    if (start == current) return@run
                    nodes += ParadoxBlankNode(text.substring(start, current), TextRange.create(start, current), configGroup)
                }
                run {
                    // expect identifier (dynamic value)
                }
                run {
                    // expect optional blank
                    start = current
                    current = text.indexOfNonBlank(start)
                    if (start == current) return@run
                    nodes += ParadoxBlankNode(text.substring(start, current), TextRange.create(start, current), configGroup)
                }
                run {
                    // expect `)`
                    start = current
                    current = if (text[current] == ')') current + 1 else -1
                    if (current == -1) return@fallback
                    nodes += ParadoxMarkerNode(")", TextRange.create(start, current), configGroup)
                }

                return ParadoxNegatedDynamicValueNode(text, range, configGroup, nodes, configs)
            }

            // fallback
            if (!incomplete) return null
            nodes += ParadoxErrorTokenNode(text.substring(start), TextRange.create(start, range.endOffset), configGroup)
            return ParadoxNegatedDynamicValueNode(text, range, configGroup, nodes, configs)
        }
    }
}
