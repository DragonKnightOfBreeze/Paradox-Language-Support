package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.indexOf

class ParadoxNegatedDynamicValueNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    override val nodes: List<ParadoxComplexExpressionNode>,
    val config: List<CwtConfig<*>>
) : ParadoxComplexExpressionNodeBase() {
    companion object {
        @JvmStatic
        fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup, configs: List<CwtConfig<*>>): ParadoxNegatedDynamicValueNode? {
            if (text.isEmpty()) return null
            if (!text.startsWith("not")) return null

            val incomplete = ChronicleThreadContext.incompleteComplexExpression.get() ?: false

            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            var start = 0
            var current = 0
            run fallback@{
                run {
                    // expect: KEYWORD
                    // start = current
                    current = 3
                    val node = ParadoxKeywordNode("not", TextRange.from(start, current), configGroup)
                    nodes += node
                }
                run {
                    // expect: optional blank
                    if (current == text.length) return@run
                    start = current
                    current = text.indexOf(start) { !it.isWhitespace() }.takeIf { it >= 0 } ?: text.length
                    if (start == current) return@run
                    nodes += ParadoxBlankNode(text.substring(start, current), TextRange.create(start, current), configGroup)
                    if (current == text.length) return@fallback
                }
                run {
                    // expect: "("
                    if (current == text.length) return@run
                    start = current
                    current = if (text[current] == '(') current + 1 else return@fallback
                    nodes += ParadoxMarkerNode("(", TextRange.create(start, current), configGroup)
                }
                run {
                    // expect: optional blank
                    if (current == text.length) return@run
                    start = current
                    current = text.indexOf(start) { !it.isWhitespace() }.takeIf { it >= 0 } ?: text.length
                    if (start == current) return@run
                    nodes += ParadoxBlankNode(text.substring(start, current), TextRange.create(start, current), configGroup)
                }
                run {
                    // expect: dynamic_value
                    if (current == text.length) return@run
                    start = current
                    current = text.indexOf(start) { it in "()," || it.isWhitespace() }.takeIf { it >= 0 } ?: text.length
                    if (start == current && !incomplete) return@run
                    nodes += ParadoxDynamicValueNode.resolve(text.substring(start, current), TextRange.create(start, current), configGroup, configs) ?: return@fallback
                }
                run {
                    // expect optional blank
                    if (current == text.length) return@run
                    start = current
                    current = text.indexOf(start) { !it.isWhitespace() }.takeIf { it >= 0 } ?: text.length
                    if (start == current) return@run
                    nodes += ParadoxBlankNode(text.substring(start, current), TextRange.create(start, current), configGroup)
                    if (current == text.length) return@fallback
                }
                run {
                    // expect: ")"
                    if (current == text.length) return@run
                    start = current
                    current = if (text[current] == ')') current + 1 else return@fallback
                    nodes += ParadoxMarkerNode(")", TextRange.create(start, current), configGroup)
                }
                run {
                    // check error
                    if (current == text.length) return@run
                    nodes += ParadoxErrorTokenNode(text.substring(current), TextRange.create(current, text.length), configGroup)
                }

                return ParadoxNegatedDynamicValueNode(text, range, configGroup, nodes, configs)
            }

            // fallback
            if (!incomplete) return null
            nodes += ParadoxErrorTokenNode(text.substring(start), TextRange.create(start, text.length), configGroup)
            return ParadoxNegatedDynamicValueNode(text, range, configGroup, nodes, configs)
        }
    }
}
