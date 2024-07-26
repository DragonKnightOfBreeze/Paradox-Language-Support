package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.collections.*

class ParadoxScopeLinkFromDataNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxComplexExpressionNode> = emptyList(),,
    override val configGroup: CwtConfigGroup,
    val linkConfigs: List<CwtLinkConfig>,
) : ParadoxComplexExpressionNode.Base(), ParadoxScopeFieldNode {
    val prefixNode get() = nodes.findIsInstance<ParadoxScopeLinkPrefixNode>()
    val dataSourceNode get() = nodes.findIsInstance<ParadoxScopeLinkDataSourceNode>()!!
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxScopeLinkFromDataNode? {
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
                val dataSourceNode = ParadoxScopeLinkDataSourceNode.resolve(dataSourceText, dataSourceRange, configGroup, linkConfigs)
                nodes.add(dataSourceNode)
                return ParadoxScopeLinkFromDataNode(text, textRange, nodes, configGroup, linkConfigs)
            } else {
                //没有前缀且允许没有前缀
                val linkConfigsNoPrefix = configGroup.linksAsScopeWithoutPrefixSorted
                if(linkConfigsNoPrefix.isNotEmpty()) {
                    //这里直接认为匹配
                    val node = ParadoxScopeLinkDataSourceNode.resolve(text, textRange, configGroup, linkConfigsNoPrefix)
                    nodes.add(node)
                    return ParadoxScopeLinkFromDataNode(text, textRange, nodes, configGroup, linkConfigsNoPrefix)
                }
            }
            return null
        }
    }
}
