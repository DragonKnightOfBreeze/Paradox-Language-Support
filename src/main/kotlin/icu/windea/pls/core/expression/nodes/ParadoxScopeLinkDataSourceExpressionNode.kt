package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.script.highlighter.*

class ParadoxScopeLinkDataSourceExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange,
	val linkConfigs: List<CwtLinkConfig>,
	override val nodes: List<ParadoxScriptExpressionNode>
) : ParadoxScriptExpressionNode {
	override fun getAttributesKey() = ParadoxScriptAttributesKeys.SCOPE_LINK_DATA_SOURCE_KEY
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, linkConfigs: List<CwtLinkConfig>): ParadoxScopeLinkDataSourceExpressionNode {
			//text may contain parameters
			//child node can be valueSetValueExpression
			val nodes = SmartList<ParadoxScriptExpressionNode>()
			val atIndex = text.indexOf('@')
			if(atIndex != -1) {
				val configExpressions = linkConfigs.mapNotNull { it.dataSource }.filter { it.type == CwtDataTypes.Value }
				if(configExpressions.isEmpty()) {
					val dataText = text.substring(0, atIndex)
					val dataRange = TextRange.create(0, atIndex)
					val dataNode = ParadoxDataExpressionNode.resolve(dataText, dataRange, linkConfigs)
					nodes.add(dataNode)
				} else {
					val configGroup = linkConfigs.first().info.configGroup
					val node = ParadoxValueSetValueExpression.resolve(text, textRange, configExpressions, configGroup)
					nodes.add(node)
				}
			}
			if(nodes.isEmpty()) {
				val node = ParadoxDataExpressionNode.resolve(text, textRange, linkConfigs)
				nodes.add(node)
			}
			return ParadoxScopeLinkDataSourceExpressionNode(text, textRange, linkConfigs, nodes)
		}
	}
}
