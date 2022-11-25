package icu.windea.pls.script.exp.nodes

import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.script.exp.errors.*

class ParadoxScopeLinkFromDataExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange,
	override val nodes: List<ParadoxScriptExpressionNode> = emptyList(),
	override val errors: List<ParadoxExpressionError> = emptyList()
) : ParadoxScriptExpressionNode {
	val prefixNode get() = nodes.findIsInstance<ParadoxScopeLinkPrefixExpressionNode>()
	val dataSourceNode get() = nodes.findIsInstance<ParadoxScopeLinkDataSourceExpressionNode>()!!
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxScopeLinkFromDataExpressionNode? {
			val nodes = SmartList<ParadoxScriptExpressionNode>()
			val errors = SmartList<ParadoxExpressionError>()
			val linkConfigs = configGroup.linksAsScopeWithPrefixSorted
				.filter { it.prefix != null && text.startsWith(it.prefix) }
			if(linkConfigs.isNotEmpty()) {
				//匹配某一前缀
				//prefix node
				val prefixText = linkConfigs.first().prefix!!
				val prefixRange = TextRange.create(textRange.startOffset, textRange.startOffset + prefixText.length)
				val prefixNode = ParadoxScopeLinkPrefixExpressionNode.resolve(prefixText, prefixRange, linkConfigs)
				nodes.add(prefixNode)
				//data source node
				val dataSourceText = text.drop(prefixText.length)
				if(dataSourceText.isEmpty()) {
					//missing data source
					val dataSources = linkConfigs.mapNotNullTo(mutableSetOf()) { it.dataSource }.joinToString()
					val error = ParadoxMissingScopeLinkDataSourceExpressionError(textRange, PlsBundle.message("script.expression.missingScopeLinkDataSource", dataSources))
					errors.add(error)
				}
				val dataSourceRange = TextRange.create(textRange.startOffset + prefixText.length, textRange.endOffset)
				val dataSourceNode = ParadoxScopeLinkDataSourceExpressionNode.resolve(dataSourceText, dataSourceRange, linkConfigs)
				nodes.add(dataSourceNode)
				return ParadoxScopeLinkFromDataExpressionNode(text, textRange, nodes, errors)
			} else {
				//没有前缀且允许没有前缀
				val linkConfigsNoPrefix = configGroup.linksAsScopeWithoutPrefixSorted
				if(linkConfigsNoPrefix.isNotEmpty()) {
					//这里直接认为匹配
					val node = ParadoxScopeLinkDataSourceExpressionNode.resolve(text, textRange, linkConfigsNoPrefix)
					nodes.add(node)
					return ParadoxScopeLinkFromDataExpressionNode(text, textRange, nodes, errors)
				}
			}
			return null
		}
	}
}
