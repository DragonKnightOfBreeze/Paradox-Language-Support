package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.collections.*

class ParadoxDynamicScopeLinkNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),
    override val configGroup: CwtConfigGroup,
    val linkConfigs: List<CwtLinkConfig>,
) : ParadoxComplexExpressionNode.Base(), ParadoxScopeLinkNode {
    val prefixNode get() = nodes.findIsInstance<ParadoxScopeLinkPrefixNode>()
    val dataSourceNode get() = nodes.findIsInstance<ParadoxScopeLinkValueNode>()!!
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxDynamicScopeLinkNode? {
            val nodes = mutableListOf<ParadoxComplexExpressionNode>()
            val offset = textRange.startOffset
            val linkConfigs = configGroup.linksAsScopeWithPrefixSorted
                .filter { it.prefix != null && text.startsWith(it.prefix!!) }
            if(linkConfigs.isNotEmpty()) {
                //匹配某一前缀
                //prefix node
                val prefixText = linkConfigs.first().prefix!!
                val prefixRange = TextRange.create(offset, offset + prefixText.length)
                val prefixNode = ParadoxScopeLinkPrefixNode.resolve(prefixText, prefixRange, configGroup, linkConfigs)
                nodes.add(prefixNode)
                //data source node
                val dataSourceText = text.drop(prefixText.length)
                val dataSourceRange = TextRange.create(prefixText.length + offset, text.length + offset)
                val dataSourceNode = ParadoxScopeLinkValueNode.resolve(dataSourceText, dataSourceRange, configGroup, linkConfigs)
                nodes.add(dataSourceNode)
                return ParadoxDynamicScopeLinkNode(text, textRange, nodes, configGroup, linkConfigs)
            } else {
                //没有前缀且允许没有前缀
                val linkConfigsNoPrefix = configGroup.linksAsScopeWithoutPrefixSorted
                if(linkConfigsNoPrefix.isNotEmpty()) {
                    //这里直接认为匹配
                    val node = ParadoxScopeLinkValueNode.resolve(text, textRange, configGroup, linkConfigsNoPrefix)
                    nodes.add(node)
                    return ParadoxDynamicScopeLinkNode(text, textRange, nodes, configGroup, linkConfigsNoPrefix)
                }
            }
            return null
        }
    }
}
