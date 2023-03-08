package icu.windea.pls.core.expression

import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.ParadoxTemplateExpression.*
import icu.windea.pls.core.expression.nodes.*

/**
 * 模版表达式。
 * 
 * 示例：
 * 
 * ```
 * job_<job>_add -> job_researcher_add
 * ```
 */
interface ParadoxTemplateExpression: ParadoxComplexExpression {
	val configExpression: CwtTemplateExpression
	
	val referenceNodes: List<ParadoxTemplateExpressionNode>
	
	companion object Resolver
}

class ParadoxTemplateExpressionImpl(
	override val text: String,
	override val rangeInExpression: TextRange,
	override val nodes: List<ParadoxTemplateExpressionNode>,
	override val configExpression: CwtTemplateExpression,
	override val configGroup: CwtConfigGroup
): AbstractExpression(text), ParadoxTemplateExpression {
	override val isKey: Boolean? = null
	override val quoted: Boolean = text.isLeftQuoted()
	
	override val referenceNodes: List<ParadoxTemplateExpressionNode> = nodes.filterTo(SmartList()) { it.configExpression != null }
}

fun Resolver.resolve(text: String, textRange: TextRange, configExpression: CwtTemplateExpression, configGroup: CwtConfigGroup): ParadoxTemplateExpression? {
	if(configExpression.isEmpty()) return null
	
	//可以用引号包围
	val snippets = configExpression.snippetExpressions
	if(snippets.isEmpty()) return null
	val nodes = mutableListOf<ParadoxTemplateExpressionNode>()
	var i1 = 0
	var i2 = 0
	var prevSnippet: CwtDataExpression? = null
	for((index, snippet) in snippets.withIndex()) {
		if(snippet.type == CwtDataType.Constant) {
			val expressionString = snippet.expressionString
			i2 = if(index == snippets.lastIndex) {
				text.lastIndexOf(expressionString, ignoreCase = true)
			} else {
				text.indexOf(expressionString, i1, ignoreCase = true)
			}
			if(i2 == -1) return null
			if(prevSnippet != null && i1 != i2) {
				val nodeText = text.substring(i1, i2)
				val nodeRange = TextRange.create(i1, i2)
				val node = ParadoxTemplateExpressionNode(nodeText, nodeRange, prevSnippet, configGroup)
				nodes.add(node)
			}
			i1 = i2 + expressionString.length
			val nodeText = expressionString
			val nodeRange = TextRange.create(i2, i1)
			val node = ParadoxTemplateExpressionNode(nodeText, nodeRange, null, configGroup)
			nodes.add(node)
		} else {
			prevSnippet = snippet
		}
	}
	if(prevSnippet != null && i1 != text.length) {
		val nodeText = text.substring(i1)
		val nodeRange = TextRange.create(i1, text.length)
		val node = ParadoxTemplateExpressionNode(nodeText, nodeRange, prevSnippet, configGroup)
		nodes.add(node)
	}
	if(nodes.size != snippets.size) {
		return null
	}
	return ParadoxTemplateExpressionImpl(text, textRange, nodes, configExpression, configGroup)
}