package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.collections.findIsInstance

class ParadoxDynamicScopeLinkNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    val linkConfigs: List<CwtLinkConfig>,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
) : ParadoxComplexExpressionNodeBase(), ParadoxScopeLinkNode {
    val prefixNode get() = nodes.findIsInstance<ParadoxScopeLinkPrefixNode>()
    val valueNode get() = nodes.findIsInstance<ParadoxScopeLinkValueNode>()!!

    override fun getRelatedConfigs(): Collection<CwtConfig<*>> {
        return linkConfigs
    }

    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxDynamicScopeLinkNode? {
            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            val offset = textRange.startOffset
            var startIndex = 0

            // 匹配某一前缀且使用传参格式的场合（如 `relations(root.owner)`）
            run r1@{
                val prefix = text.substringBefore('(', "")
                if (prefix.isEmpty()) return@r1
                val linkConfigs = configGroup.linksModel.forScopeFromArgumentSortedByPrefix[prefix]
                if (linkConfigs.isNullOrEmpty()) return@r1
                run r2@{
                    val nodeText = prefix // = linkConfigs.first().prefixFromArgument!!
                    val nodeTextRange = TextRange.from(offset, nodeText.length)
                    val node = ParadoxScopeLinkPrefixNode.resolve(nodeText, nodeTextRange, configGroup, linkConfigs)
                    nodes += node
                    startIndex += nodeText.length
                }
                run r2@{
                    val nodeTextRange = TextRange.from(offset + startIndex, 1)
                    val node = ParadoxMarkerNode("(", nodeTextRange, configGroup)
                    nodes += node
                    startIndex += 1
                }
                val valueEndIndex = if (text.endsWith(')')) text.length - 1 else text.length
                run r2@{
                    val nodeText = text.substring(startIndex, valueEndIndex)
                    val nodeTextRange = TextRange.from(offset + startIndex, nodeText.length)
                    val node = ParadoxScopeLinkValueNode.resolve(nodeText, nodeTextRange, configGroup, linkConfigs)
                    nodes += node
                    startIndex += nodeText.length
                }
                run r2@{
                    val nodeTextRange = TextRange.from(offset + startIndex, text.length - valueEndIndex)
                    val node = if (nodeTextRange.isEmpty) ParadoxErrorTokenNode("", nodeTextRange, configGroup)
                    else ParadoxMarkerNode(")", nodeTextRange, configGroup)
                    nodes += node
                }
                return ParadoxDynamicScopeLinkNode(text, textRange, configGroup, linkConfigs, nodes)
            }

            // 匹配某一前缀的场合（如 `event_target:some_job`）
            run r1@{
                val linkConfigs = configGroup.linksModel.forScopeFromDataSorted
                    .filter { text.startsWith(it.prefix!!) }
                if (linkConfigs.isEmpty()) return@r1

                val prefixText = linkConfigs.maxByOrNull { it.prefix!!.length }!!.prefix!!

                run r2@{
                    val hasTrailingColon = prefixText.endsWith(':')
                    val nodeText = if (hasTrailingColon) prefixText.dropLast(1) else prefixText
                    val nodeTextRange = TextRange.from(offset, nodeText.length)
                    val node = ParadoxScopeLinkPrefixNode.resolve(nodeText, nodeTextRange, configGroup, linkConfigs)
                    nodes += node
                    startIndex += nodeText.length
                    if (hasTrailingColon) {
                        val markerRange = TextRange.from(offset + startIndex, 1)
                        nodes += ParadoxMarkerNode(":", markerRange, configGroup)
                        startIndex += 1
                    }
                }
                run r2@{
                    val nodeText = text.substring(startIndex)
                    val nodeTextRange = TextRange.from(offset + startIndex, nodeText.length)
                    val node = ParadoxScopeLinkValueNode.resolve(nodeText, nodeTextRange, configGroup, linkConfigs)
                    nodes += node
                }
                return ParadoxDynamicScopeLinkNode(text, textRange, configGroup, linkConfigs, nodes)
            }

            // 没有前缀且允许没有前缀的场合
            run r1@{
                val linkConfigs = configGroup.linksModel.forScopeNoPrefixSorted
                if (linkConfigs.isEmpty()) return@r1
                val node = ParadoxScopeLinkValueNode.resolve(text, textRange, configGroup, linkConfigs)
                nodes += node
                return ParadoxDynamicScopeLinkNode(text, textRange, configGroup, linkConfigs, nodes)
            }

            return null
        }
    }

    companion object : Resolver()
}
