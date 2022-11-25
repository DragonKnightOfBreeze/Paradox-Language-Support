package icu.windea.pls.core.expression

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.ParadoxScopeFieldExpression.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.script.exp.errors.*
import icu.windea.pls.script.highlighter.*

/**
 * 作用域字段表达式。
 *
 * 语法：
 *
 * ```bnf
 * scope_field_expression ::= scope +
 * scope ::= system_scope | scope_link | scope_link_from_data
 * system_scope ::= TOKEN //predefined by Internal Config (in script_config.pls.cwt)
 * scope_link ::= TOKEN //predefined by CWT Config (in links.cwt, from_data = false, type = both | scope)
 * scope_link_from_data ::= scope_link_prefix scope_link_data_source //predefined by CWT Config (in links.cwt, from_data = true, type = both | scope)
 * scope_link_prefix ::= TOKEN //e.g. "event_target:" while the link's prefix is "event_target:"
 * scope_link_data_source ::= EXPRESSION //e.g. "some_variable" while the link's data source is "value[variable]"
 * expression ::= data_expression | value_set_value_expression //see: ParadoxDataExpression, ParadoxValueSetValueExpression
 * ```
 *
 * 示例：
 *
 * ```
 * root
 * root.owner
 * event_target:some_target
 * ```
 */
interface ParadoxScopeFieldExpression : ParadoxComplexExpression {
	val scopeNodes: List<ParadoxScopeExpressionNode>
	
	companion object Resolver
}

class ParadoxScopeFieldExpressionImpl(
	override val text: String,
	override val rangeInExpression: TextRange,
	override val isKey: Boolean?,
	override val nodes: List<ParadoxScriptExpressionNode>,
	override val errors: List<ParadoxExpressionError>
) : AbstractExpression(text), ParadoxScopeFieldExpression {
	override val quoted: Boolean = false
	
	override val scopeNodes: List<ParadoxScopeExpressionNode> get() = nodes.filterIsInstance<ParadoxScopeExpressionNode>()
	
	override fun getAttributesKey() = ParadoxScriptAttributesKeys.SCOPE_FILED_EXPRESSION_KEY
	
	override fun complete(context: ProcessingContext, result: CompletionResultSet) {
		//要求重新匹配
		result.restartCompletionOnAnyPrefixChange()
		
		val keyword = context.keyword
		val isKey = context.isKey
		val prevScope = context.prevScope
		context.put(PlsCompletionKeys.isKeyKey, null)
		
		val offsetInParent = context.offsetInParent
		var prevScopeToUse: String? = null
		for(node in nodes) {
			val nodeRange = node.rangeInExpression
			val inRange = offsetInParent >= nodeRange.startOffset && offsetInParent <= nodeRange.endOffset
			if(node is ParadoxScopeExpressionNode) {
				if(inRange) {
					context.put(PlsCompletionKeys.prevScopeKey, prevScopeToUse)
					val childNode = node.nodes.findIsInstance<ParadoxScopeLinkFromDataExpressionNode>()
					val prefixNode = childNode?.prefixNode
					val dataSourceNode = childNode?.dataSourceNode
					val dataSourceNodeToCheck = dataSourceNode?.nodes?.first()
					val endOffset = prefixNode?.rangeInExpression?.endOffset ?: -1
					if(prefixNode != null && offsetInParent >= endOffset) {
						val keywordToUse = node.text.substring(0, offsetInParent - endOffset)
						val resultToUse = result.withPrefixMatcher(keywordToUse)
						val prefix = prefixNode.text
						CwtConfigHandler.completeScopeLinkDataSource(context, resultToUse, prefix, dataSourceNodeToCheck)
					} else {
						val inFirstNode = dataSourceNode == null
							|| offsetInParent <= dataSourceNode.nodes.first().nodes.first().rangeInExpression.endOffset
						val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
						val resultToUse = result.withPrefixMatcher(keywordToUse)
						context.put(PlsCompletionKeys.keywordKey, keywordToUse)
						if(inFirstNode) {
							CwtConfigHandler.completeSystemScope(context, resultToUse)
							CwtConfigHandler.completeScope(context, resultToUse)
							CwtConfigHandler.completeScopeLinkPrefix(context, resultToUse)
						}
						CwtConfigHandler.completeScopeLinkDataSource(context, resultToUse, null, dataSourceNodeToCheck)
						break
					}
				}
				prevScopeToUse = node.text
			}
		}
		
		context.put(PlsCompletionKeys.keywordKey, keyword)
		context.put(PlsCompletionKeys.isKeyKey, isKey)
		context.put(PlsCompletionKeys.prevScopeKey, prevScope)
	}
}

fun Resolver.resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, isKey: Boolean? = null, canBeMismatched: Boolean = false): ParadoxScopeFieldExpression? {
	val nodes = SmartList<ParadoxScriptExpressionNode>()
	val errors = SmartList<ParadoxExpressionError>()
	val offset = textRange.startOffset
	var isLast = false
	var index: Int
	var dotIndex = -1
	while(dotIndex < text.length) {
		index = dotIndex + 1
		dotIndex = text.indexOf('.', index)
		if(dotIndex == -1) {
			dotIndex = text.length
			isLast = true
		}
		//resolve node
		val nodeText = text.substring(index, dotIndex)
		val nodeTextRange = TextRange.create(index + offset, dotIndex + offset)
		val node = ParadoxScopeExpressionNode.resolve(nodeText, nodeTextRange, configGroup)
		//handle mismatch situation
		if(index == 0 && node.nodes.isEmpty() && !canBeMismatched) {
			return null
		}
		nodes.add(node)
		if(!isLast) {
			//resolve dot node
			val dotNode = ParadoxScriptOperatorExpressionNode(".", TextRange.create(dotIndex + offset, dotIndex + 1 + offset))
			nodes.add(dotNode)
		}
	}
	if(nodes.isEmpty()) return null
	return ParadoxScopeFieldExpressionImpl(text, textRange, isKey, nodes, errors)
}

