package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.collections.*

class ParadoxValueLinkFromDataExpressionNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val nodes: List<ParadoxExpressionNode> = emptyList(),
    val linkConfigs: List<CwtLinkConfig>,
) : ParadoxValueFieldExpressionNode {
    val prefixNode get() = nodes.findIsInstance<ParadoxValueLinkPrefixExpressionNode>()
    val dataSourceNode get() = nodes.findIsInstance<ParadoxValueLinkDataSourceExpressionNode>()!!
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxValueLinkFromDataExpressionNode? {
            val nodes = mutableListOf<ParadoxExpressionNode>()
            val offset = textRange.startOffset
            val linkConfigs = configGroup.linksAsValueWithPrefixSorted
                .filter { it.prefix != null && text.startsWith(it.prefix) }
            if(linkConfigs.isNotEmpty()) {
                //匹配某一前缀
                //prefix node
                val prefixText = linkConfigs.first().prefix!!
                val prefixRange = TextRange.create(offset, prefixText.length + offset)
                val prefixNode = ParadoxValueLinkPrefixExpressionNode.resolve(prefixText, prefixRange, linkConfigs)
                nodes.add(prefixNode)
                //data source node
                val dataSourceText = text.drop(prefixText.length)
                val dataSourceRange = TextRange.create(prefixText.length + offset, text.length + offset)
                val dataSourceNode = ParadoxValueLinkDataSourceExpressionNode.resolve(dataSourceText, dataSourceRange, linkConfigs)
                nodes.add(dataSourceNode)
                return ParadoxValueLinkFromDataExpressionNode(text, textRange, nodes, linkConfigs)
            } else {
                //没有前缀且允许没有前缀
                val linkConfigsNoPrefix = configGroup.linksAsValueWithoutPrefixSorted
                if(linkConfigsNoPrefix.isNotEmpty()) {
                    //这里直接认为匹配
                    val node = ParadoxValueLinkDataSourceExpressionNode.resolve(text, textRange, linkConfigsNoPrefix)
                    nodes.add(node)
                    return ParadoxValueLinkFromDataExpressionNode(text, textRange, nodes, linkConfigs)
                }
            }
            return null
        }
    }
}
