package icu.windea.pls.script.exp.nodes

import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.script.exp.errors.*
import kotlin.collections.mapNotNullTo

class ParadoxValueLinkFromDataExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange,
	override val nodes: List<ParadoxScriptExpressionNode> = emptyList(),
	override val errors: List<ParadoxScriptExpressionError> = emptyList()
) : ParadoxScriptExpressionNode {
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxValueLinkFromDataExpressionNode? {
			val linkConfigs = configGroup.linksAsValueWithPrefixSorted
				.filter { it.prefix != null && text.startsWith(it.prefix) }
			if(linkConfigs.isNotEmpty()) {
				//匹配某一前缀
				val nodes = SmartList<ParadoxScriptExpressionNode>()
				val errors = SmartList<ParadoxScriptExpressionError>()
				//prefix node
				val prefixText = linkConfigs.first().prefix!!
				val prefixRange = TextRange.create(textRange.startOffset, textRange.startOffset + prefixText.length)
				val prefixNode = ParadoxValueLinkPrefixExpressionNode.resolve(prefixText, prefixRange, linkConfigs)
				nodes.add(prefixNode)
				//data source node
				val dataSourceText = text.drop(prefixText.length)
				if(dataSourceText.isEmpty()) {
					//missing data source
					val dataSources = linkConfigs.mapNotNullTo(mutableSetOf()) { it.dataSource }.joinToString()
					val error = ParadoxMissingValueLinkDataSourceExpressionError(textRange, PlsBundle.message("script.expression.missingValueLinkDataSource", dataSources))
					errors.add(error)
				}
				val dataSourceRange = TextRange.create(textRange.startOffset + prefixText.length, textRange.endOffset)
				val dataSourceNode = ParadoxValueLinkDataSourceExpressionNode.resolve(dataSourceText, dataSourceRange, linkConfigs)
				nodes.add(dataSourceNode)
				return ParadoxValueLinkFromDataExpressionNode(text, textRange, nodes, errors)
			} else {
				//没有前缀且允许没有前缀
				val linkConfigsNoPrefix = configGroup.linksAsValueWithoutPrefixSorted
				if(linkConfigsNoPrefix.isNotEmpty()) {
					//这里直接认为匹配
					val node = ParadoxValueLinkDataSourceExpressionNode.resolve(text, textRange, linkConfigsNoPrefix)
					return ParadoxValueLinkFromDataExpressionNode(text, textRange, node.toSingletonList())
				}
			}
			return null
		}
	}
}
