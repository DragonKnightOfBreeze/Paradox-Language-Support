package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configContext.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.collections.*

class ParadoxDynamicCommandScopeLinkNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup,
    val linkConfigs: List<CwtLinkConfig>,
) : ParadoxComplexExpressionNode.Base(), ParadoxCommandScopeLinkNode {
    val prefixNode get() = nodes.findIsInstance<ParadoxCommandScopeLinkPrefixNode>()
    val dataSourceNode get() = nodes.findIsInstance<ParadoxCommandScopeLinkValueNode>()!!

    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxDynamicCommandScopeLinkNode? {
            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            val offset = textRange.startOffset
            var startIndex = 0

            //匹配某一前缀的场合
            run r1@{
                val linkConfigs = configGroup.localisationLinks.values.filter { it.forScope() && it.fromData && it.prefix != null && text.startsWith(it.prefix!!) }
                    .sortedByPriority({ it.dataSourceExpression!! }, { configGroup })
                if (linkConfigs.isEmpty()) return@r1
                run r2@{
                    val nodeText = linkConfigs.first().prefix!!
                    val nodeTextRange = TextRange.from(offset, nodeText.length)
                    val node = ParadoxCommandScopeLinkPrefixNode.resolve(nodeText, nodeTextRange, configGroup, linkConfigs)
                    nodes += node
                    startIndex += nodeText.length
                }
                run r2@{
                    val nodeText = text.substring(startIndex)
                    val nodeTextRange = TextRange.from(offset + startIndex, nodeText.length)
                    val node = ParadoxCommandScopeLinkValueNode.resolve(nodeText, nodeTextRange, configGroup, linkConfigs)
                    nodes += node
                }
                return ParadoxDynamicCommandScopeLinkNode(text, textRange, nodes, configGroup, linkConfigs)
            }

            //事件目标的前缀可以省略
            run r1@{
                val linkConfigs = configGroup.localisationLinksOfEventTarget
                if (linkConfigs.isEmpty()) return@r1
                val node = ParadoxCommandScopeLinkValueNode.resolve(text, textRange, configGroup, linkConfigs)
                nodes += node
                return ParadoxDynamicCommandScopeLinkNode(text, textRange, nodes, configGroup, linkConfigs)
            }

            return null
        }
    }
}
