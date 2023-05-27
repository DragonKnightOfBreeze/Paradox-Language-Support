package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.script.highlighter.*

class ParadoxValueLinkDataSourceExpressionNode (
	override val text: String,
	override val rangeInExpression: TextRange,
	val linkConfigs: List<CwtLinkConfig>,
	override val nodes: List<ParadoxExpressionNode>
) : ParadoxExpressionNode {
	override fun getAttributesKey() = ParadoxScriptAttributesKeys.VALUE_LINK_DATA_SOURCE_KEY
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, linkConfigs: List<CwtLinkConfig>): ParadoxValueLinkDataSourceExpressionNode {
			//text may contain parameters
			//child node can be valueSetValueExpression / scriptValueExpression
			val nodes = mutableListOf<ParadoxExpressionNode>()
			//if(nodes.isEmpty()) {
			//	val scopeFieldConfig = linkConfigs.find { it.expression?.type?.isScopeFieldType() == true }
			//	if(scopeFieldConfig != null) {
			//		val configGroup = linkConfigs.first().info.configGroup
			//		val node = ParadoxScopeExpressionNode.resolve(text, textRange, configGroup)
			//		nodes.add(node)
			//	}
			//}
			if(nodes.isEmpty()) {
				val configs = linkConfigs.filter { it.dataSource?.type == CwtDataType.Value }
				if(configs.isNotEmpty()) {
					val configGroup = linkConfigs.first().info.configGroup
					val node = ParadoxValueSetValueExpression.resolve(text, textRange, configs, configGroup)!!
					nodes.add(node)
				}
				//val offset = textRange.startOffset
				//val atIndex = text.indexOf('@')
				//if(atIndex != -1) {
				//	if(configs.isEmpty()) {
				//		val dataText = text.substring(0, atIndex)
				//		val dataRange = TextRange.create(offset, atIndex + offset)
				//		val dataNode = ParadoxDataExpressionNode.resolve(dataText, dataRange, linkConfigs)
				//		nodes.add(dataNode)
				//		val errorText = text.substring(atIndex)
				//		val errorRange = TextRange.create(atIndex + offset, text.length + offset)
				//		val errorNode = ParadoxErrorTokenExpressionNode(errorText, errorRange)
				//		nodes.add(errorNode)
				//	} else {
				//		val configGroup = linkConfigs.first().info.configGroup
				//		val node = ParadoxValueSetValueExpression.resolve(text, textRange, configs, configGroup)
				//		nodes.add(node)
				//	}
				//} else {
				//	if(configs.isNotEmpty()) {
				//		val configGroup = linkConfigs.first().info.configGroup
				//		val node = ParadoxValueSetValueExpression.resolve(text, textRange, configs, configGroup)
				//		nodes.add(node)
				//	}
				//}
			}
			if(nodes.isEmpty()) {
				val offset = textRange.startOffset
				val pipeIndex = text.indexOf('|')
				if(pipeIndex != -1) {
					val scriptValueConfig = linkConfigs.find { it.name == "script_value" }
					if(scriptValueConfig == null) {
						val dataText = text.substring(0, pipeIndex)
						val dataRange = TextRange.create(offset, pipeIndex + offset)
						val dataNode = ParadoxDataExpressionNode.resolve(dataText, dataRange, linkConfigs)
						nodes.add(dataNode)
						val errorText = text.substring(pipeIndex)
						val errorRange = TextRange.create(pipeIndex + offset, text.length + offset)
						val errorNode = ParadoxErrorTokenExpressionNode(errorText, errorRange)
						nodes.add(errorNode)
					} else {
						val configGroup = linkConfigs.first().info.configGroup
						val node = ParadoxScriptValueExpression.resolve(text, textRange, scriptValueConfig, configGroup)
						nodes.add(node)
					}
				}
			}
			if(nodes.isEmpty()) {
				val node = ParadoxDataExpressionNode.resolve(text, textRange, linkConfigs)
				nodes.add(node)
			}
			return ParadoxValueLinkDataSourceExpressionNode(text, textRange, linkConfigs, nodes)
		}
	}
}
