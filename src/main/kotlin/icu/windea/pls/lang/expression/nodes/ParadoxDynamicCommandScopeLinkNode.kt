package icu.windea.pls.lang.expression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtLinkConfig
import icu.windea.pls.config.config.forScope
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.localisationLinks
import icu.windea.pls.config.sortedByPriority
import icu.windea.pls.core.collections.findIsInstance

class ParadoxDynamicCommandScopeLinkNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode>,
    override val configGroup: CwtConfigGroup,
    val linkConfigs: List<CwtLinkConfig>,
) : ParadoxComplexExpressionNode.Base(), ParadoxCommandScopeLinkNode {
    val prefixNode get() = nodes.findIsInstance<ParadoxCommandScopeLinkPrefixNode>()
    val valueNode get() = nodes.findIsInstance<ParadoxCommandScopeLinkValueNode>()!!

    override fun getRelatedConfigs(): Collection<CwtConfig<*>> {
        return linkConfigs
    }

    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxDynamicCommandScopeLinkNode? {
            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            val offset = textRange.startOffset
            var startIndex = 0

            //匹配某一前缀的场合
            run r1@{
                val linkConfigs = configGroup.localisationLinks.values.filter { it.forScope() && it.fromData && it.prefix != null && text.startsWith(it.prefix!!) }
                    .sortedByPriority({ it.dataSourceExpression }, { configGroup })
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

            //匹配某一前缀且使用传参格式的场合（如，"relations(root.owner)"）
            run r1@{
                if (!text.contains('(')) return@r1
                val linkConfigs = configGroup.localisationLinks.values.filter { it.forScope() && it.fromArgument && it.prefix != null }
                    .filter { text.startsWith(it.prefix!!.dropLast(1) + '(') }
                    .sortedByPriority({ it.dataSourceExpression }, { configGroup })
                if (linkConfigs.isEmpty()) return@r1
                run r2@{
                    val nodeText = linkConfigs.first().prefix!!.dropLast(1)
                    val nodeTextRange = TextRange.from(offset, nodeText.length)
                    val node = ParadoxCommandScopeLinkPrefixNode.resolve(nodeText, nodeTextRange, configGroup, linkConfigs)
                    nodes += node
                    startIndex += nodeText.length
                }
                run r2@{
                    val nodeTextRange = TextRange.from(offset + startIndex, 1)
                    val node = ParadoxOperatorNode("(", nodeTextRange, configGroup)
                    nodes += node
                    startIndex += 1
                }
                val valueEndIndex = if (text.endsWith(')')) text.length - 1 else text.length
                run r2@{
                    val nodeText = text.substring(startIndex, valueEndIndex)
                    val nodeTextRange = TextRange.from(offset + startIndex, nodeText.length)
                    val node = ParadoxCommandScopeLinkValueNode.resolve(nodeText, nodeTextRange, configGroup, linkConfigs)
                    nodes += node
                    startIndex += nodeText.length
                }
                run r2@{
                    val nodeTextRange = TextRange.from(offset + startIndex, text.length - valueEndIndex)
                    val node = if (nodeTextRange.isEmpty) ParadoxErrorTokenNode("", nodeTextRange, configGroup)
                    else ParadoxOperatorNode(")", nodeTextRange, configGroup)
                    nodes += node
                }
                return ParadoxDynamicCommandScopeLinkNode(text, textRange, nodes, configGroup, linkConfigs)
            }

            //没有前缀且允许没有前缀的场合
            run r1@{
                val linkConfigs = configGroup.localisationLinks.values.filter { it.forScope() && it.fromData && it.prefix == null }
                    .sortedByPriority({ it.dataSourceExpression }, { configGroup })
                if (linkConfigs.isEmpty()) return@r1
                val node = ParadoxCommandScopeLinkValueNode.resolve(text, textRange, configGroup, linkConfigs)
                nodes += node
                return ParadoxDynamicCommandScopeLinkNode(text, textRange, nodes, configGroup, linkConfigs)
            }

            return null
        }
    }
}
