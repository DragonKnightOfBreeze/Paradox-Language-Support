package icu.windea.pls.script.exp.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.script.exp.errors.*

class ParadoxScopeLinkFromDataExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange,
	override val nodes: List<ParadoxScriptExpressionNode> = emptyList(),
	override val errors: List<ParadoxScriptExpressionError> = emptyList()
) : ParadoxScriptExpressionNode {
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxScopeLinkFromDataExpressionNode? {
			val linkConfigs = configGroup.linksAsScopeSorted
				.filter { it.prefix != null && it.dataSource != null && text.startsWith(it.prefix) }
			if(linkConfigs.isNotEmpty()) {
				//匹配某一前缀
				val prefixText = linkConfigs.first().prefix!!
				val prefixRange = TextRange.create(textRange.startOffset, textRange.startOffset + prefixText.length)
				val node = ParadoxScopeLinkPrefixExpressionNode.resolve(prefixText, prefixRange, linkConfigs)
				val dataSourceText = text.drop(prefixText.length)
				if(dataSourceText.isEmpty()) {
					//缺少前缀
					val dataSources = linkConfigs.mapNotNullTo(mutableSetOf()) { it.dataSource }.joinToString()
					val error = ParadoxMissingScopeLinkDataSourceExpressionError(textRange, PlsBundle.message("script.expression.missingScopeLinkDataSource", dataSources))
					return ParadoxScopeLinkFromDataExpressionNode(text, textRange, node.toSingletonList(), error.toSingletonList())
				} else {
					val dataSourceRange = TextRange.create(textRange.startOffset + prefixText.length, textRange.endOffset)
					val dataSourceNode = ParadoxScopeLinkDataSourceExpressionNode.resolve(dataSourceText, dataSourceRange, linkConfigs)
					return ParadoxScopeLinkFromDataExpressionNode(text, textRange, listOf(node, dataSourceNode))
				}
			} else {
				//没有前缀且允许没有前缀
				val linkConfigsNoPrefix = configGroup.linksAsScopeNoPrefixSorted
				if(linkConfigsNoPrefix.isNotEmpty()) {
					//这里直接认为匹配
					val node = ParadoxScopeLinkDataSourceExpressionNode.resolve(text, textRange, linkConfigsNoPrefix)
					return ParadoxScopeLinkFromDataExpressionNode(text, textRange, node.toSingletonList())
				}
			}
			return null
		}
	}
}
