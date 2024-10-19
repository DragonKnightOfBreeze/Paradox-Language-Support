package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.collections.*

class ParadoxDynamicValueFieldNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
    override val configGroup: CwtConfigGroup,
    val linkConfigs: List<CwtLinkConfig>
) : ParadoxComplexExpressionNode.Base(), ParadoxValueFieldNode {
    val prefixNode get() = nodes.findIsInstance<ParadoxValueFieldPrefixNode>()
    val dataSourceNode get() = nodes.findIsInstance<ParadoxValueFieldValueNode>()!!

    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxDynamicValueFieldNode? {
            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            val offset = textRange.startOffset
            var startIndex = 0

            //匹配某一前缀的场合
            run r1@{
                val linkConfigs = configGroup.links.values.filter { it.forValue() && it.fromData && it.prefix != null && text.startsWith(it.prefix!!) }
                    .sortedByPriority({ it.dataSourceExpression!! }, { configGroup })
                if (linkConfigs.isEmpty()) return@r1
                run r2@{
                    val nodeText = linkConfigs.first().prefix!!
                    val nodeTextRange = TextRange.from(offset, nodeText.length)
                    val node = ParadoxValueFieldPrefixNode.resolve(nodeText, nodeTextRange, configGroup, linkConfigs)
                    nodes += node
                    startIndex += nodeText.length
                }
                run r2@{
                    val nodeText = text.substring(startIndex)
                    val nodeTextRange = TextRange.from(offset + startIndex, nodeText.length)
                    val node = ParadoxValueFieldValueNode.resolve(nodeText, nodeTextRange, configGroup, linkConfigs)
                    nodes += node
                }
                return ParadoxDynamicValueFieldNode(text, textRange, nodes, configGroup, linkConfigs)
            }

            //没有前缀且允许没有前缀的场合
            run r1@{
                val linkConfigs = configGroup.links.values.filter { it.forValue() && it.fromData && it.prefix == null }
                    .sortedByPriority({ it.dataSourceExpression!! }, { configGroup })
                if (linkConfigs.isEmpty()) return@r1
                val node = ParadoxValueFieldValueNode.resolve(text, textRange, configGroup, linkConfigs)
                nodes += node
                return ParadoxDynamicValueFieldNode(text, textRange, nodes, configGroup, linkConfigs)
            }

            return null
        }
    }
}
