package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.util.*
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
            val linkConfigs = configGroup.linksAsValueWithPrefixSorted
                .filter { it.prefix != null && text.startsWith(it.prefix!!) }
            if(linkConfigs.isNotEmpty()) {
                //匹配某一前缀
                //prefix node
                val prefixText = linkConfigs.first().prefix!!
                val prefixRange = TextRange.create(offset, prefixText.length + offset)
                val prefixNode = ParadoxValueFieldPrefixNode.resolve(prefixText, prefixRange, configGroup, linkConfigs)
                nodes.add(prefixNode)
                //data source node
                val dataSourceText = text.drop(prefixText.length)
                val dataSourceRange = TextRange.create(prefixText.length + offset, text.length + offset)
                val dataSourceNode = ParadoxValueFieldValueNode.resolve(dataSourceText, dataSourceRange, configGroup, linkConfigs)
                nodes.add(dataSourceNode)
                return ParadoxDynamicValueFieldNode(text, textRange, nodes, configGroup, linkConfigs)
            } else {
                //没有前缀且允许没有前缀
                val linkConfigsNoPrefix = configGroup.linksAsValueWithoutPrefixSorted
                if(linkConfigsNoPrefix.isNotEmpty()) {
                    //这里直接认为匹配
                    val node = ParadoxValueFieldValueNode.resolve(text, textRange, configGroup,linkConfigsNoPrefix)
                    nodes.add(node)
                    return ParadoxDynamicValueFieldNode(text, textRange, nodes, configGroup, linkConfigs)
                }
            }
            return null
        }
    }
}
