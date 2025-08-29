package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtLinkConfig
import icu.windea.pls.config.config.forValue
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.links
import icu.windea.pls.config.sortedByPriority
import icu.windea.pls.core.collections.findIsInstance

class ParadoxDynamicValueFieldNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
    override val configGroup: CwtConfigGroup,
    val linkConfigs: List<CwtLinkConfig>
) : ParadoxComplexExpressionNode.Base(), ParadoxValueFieldNode {
    val prefixNode get() = nodes.findIsInstance<ParadoxValueFieldPrefixNode>()
    val valueNode get() = nodes.findIsInstance<ParadoxValueFieldValueNode>()!!

    override fun getRelatedConfigs(): Collection<CwtConfig<*>> {
        return linkConfigs
    }

    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxDynamicValueFieldNode? {
            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            val offset = textRange.startOffset
            var startIndex = 0

            //匹配某一前缀的场合（如，"event_target:some_country"）
            run r1@{
                if (!text.contains(':')) return@r1
                val linkConfigs = configGroup.links.values.filter { it.forValue() && it.fromData && it.prefix != null }
                    .filter { text.startsWith(it.prefix!!) }
                    .sortedByPriority({ it.dataSourceExpression }, { configGroup })
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

            //匹配某一前缀且使用传参格式的场合（如，"relations(root.owner)"）
            run r1@{
                if (!text.contains('(')) return@r1
                val linkConfigs = configGroup.links.values.filter { it.forValue() && it.fromArgument && it.prefix != null }
                    .filter { text.startsWith(it.prefix!!.dropLast(1) + '(') }
                    .sortedByPriority({ it.dataSourceExpression }, { configGroup })
                if (linkConfigs.isEmpty()) return@r1
                run r2@{
                    val nodeText = linkConfigs.first().prefix!!.dropLast(1)
                    val nodeTextRange = TextRange.from(offset, nodeText.length)
                    val node = ParadoxValueFieldPrefixNode.resolve(nodeText, nodeTextRange, configGroup, linkConfigs)
                    nodes += node
                    startIndex += nodeText.length
                }
                run r2@{
                    val nodeTextRange = TextRange.from(offset + startIndex, 1)
                    val node = ParadoxOperatorNode("(", nodeTextRange, configGroup)
                    nodes += node
                    startIndex += 1
                }
                val valueEndIndex = if(text.endsWith(')')) text.length - 1 else text.length
                run r2@{
                    val nodeText = text.substring(startIndex, valueEndIndex)
                    val nodeTextRange = TextRange.from(offset + startIndex, nodeText.length)
                    val node = ParadoxValueFieldValueNode.resolve(nodeText, nodeTextRange, configGroup, linkConfigs)
                    nodes += node
                    startIndex += nodeText.length
                }
                run r2@{
                    val nodeTextRange = TextRange.from(offset + startIndex, text.length - valueEndIndex)
                    val node = if(nodeTextRange.isEmpty) ParadoxErrorTokenNode("", nodeTextRange, configGroup)
                    else ParadoxOperatorNode(")", nodeTextRange, configGroup)
                    nodes += node
                }
                return ParadoxDynamicValueFieldNode(text, textRange, nodes, configGroup, linkConfigs)
            }

            //没有前缀且允许没有前缀的场合
            run r1@{
                val linkConfigs = configGroup.links.values.filter { it.forValue() && it.fromData && it.prefix == null }
                    .sortedByPriority({ it.dataSourceExpression }, { configGroup })
                if (linkConfigs.isEmpty()) return@r1
                val node = ParadoxValueFieldValueNode.resolve(text, textRange, configGroup, linkConfigs)
                nodes += node
                return ParadoxDynamicValueFieldNode(text, textRange, nodes, configGroup, linkConfigs)
            }

            return null
        }
    }
}
