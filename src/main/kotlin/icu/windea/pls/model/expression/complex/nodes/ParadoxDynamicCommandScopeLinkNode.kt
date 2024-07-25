package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.configGroup.*

class ParadoxDynamicCommandScopeLinkNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>
) : ParadoxComplexExpressionNode.Base(), ParadoxCommandScopeLinkNode {
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxDynamicCommandScopeLinkNode? {
            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            run r1@{
                val offset = textRange.startOffset
                var startIndex = 0
                run r2@{
                    val hardCodedPrefix = "event_target:"
                    if(!text.startsWith(hardCodedPrefix)) return@r2
                    val nodeText = hardCodedPrefix
                    val nodeTextRange = TextRange.from(offset, hardCodedPrefix.length)
                    val node = ParadoxDynamicCommandScopeLinkPrefixNode.resolve(nodeText, nodeTextRange, configGroup) ?: return null
                    nodes += node
                    startIndex = hardCodedPrefix.length
                }
                run r2@{
                    val nodeText = text.substring(startIndex)
                    val nodeTextRange = TextRange.from(offset + startIndex, nodeText.length)
                    val node = ParadoxDynamicCommandScopeLinkValueNode.resolve(nodeText, nodeTextRange, configGroup) ?: return null
                    nodes += node
                }
            }
            return ParadoxDynamicCommandScopeLinkNode(text, textRange, nodes)
        }
    }
}
