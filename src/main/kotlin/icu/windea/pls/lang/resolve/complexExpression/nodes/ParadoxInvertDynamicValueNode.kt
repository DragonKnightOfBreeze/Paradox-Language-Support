package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.indexOfNonBlank

class ParadoxInvertDynamicValueNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val nodes: List<ParadoxComplexExpressionNode>,
    val configs: List<CwtConfig<*>>
) : ParadoxComplexExpressionNodeBase() {
    companion object {
        @JvmStatic
        fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup, configs: List<CwtConfig<*>>): ParadoxInvertDynamicValueNode? {
            if (text.isEmpty()) return null
            if (!text.startsWith("not")) return null

            val incomplete = ChronicleThreadContext.incompleteComplexExpression.get() ?: false

            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            var start = 0
            var current = 0
            run fallback@{
                // TODO 2.1.10
                run {
                    // expect `not`
                    val nodeTextRange = TextRange.from(0, 3)
                    val node = ParadoxKeywordNode("not", nodeTextRange, configGroup)
                    nodes += node
                }
                run {
                    // expect optional blank
                    start = 3
                    current = text.indexOfNonBlank(start)
                    if (start != current) {
                        nodes += ParadoxBlankNode(text.substring(start, current), TextRange.create(start, current), configGroup)
                    }
                }
                run {
                    // expect `(`
                    start = current
                    current = if (text[current] == '(') current else -1
                    if (current == -1) return@fallback
                }
                run {
                    // expect optional blank
                }
                run {
                    // expect identifier (dynamic value)
                }
                run {
                    // expect optional blank
                }
                run {
                    // expect `)`
                }

                return ParadoxInvertDynamicValueNode(text, range, configGroup, nodes, configs)
            }

            // fallback
            if (!incomplete) return null
            nodes += ParadoxErrorTokenNode(text.substring(start), TextRange.create(start, range.endOffset), configGroup)
            return ParadoxInvertDynamicValueNode(text, range, configGroup, nodes, configs)
        }
    }
}
