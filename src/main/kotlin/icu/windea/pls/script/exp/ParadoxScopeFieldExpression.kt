package icu.windea.pls.script.exp

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.script.exp.ParadoxScopeFieldExpression.*
import icu.windea.pls.script.exp.errors.*
import icu.windea.pls.script.exp.nodes.*

/**
 * 作用域字段表达式。
 *
 * 语法：
 *
 * ```bnf
 * scope_field_expression ::= scope +
 * scope ::= system_scope | scope_link | scope_link_from_data
 * system_scope ::= TOKEN //predefined by Internal Config (in script_config.pls.cwt)
 * scope_link ::= TOKEN //predefined by CWT Config (in links.cwt)
 * scope_link_from_data ::= scope_link_prefix scope_link_data_source //predefined by CWT Config (in links.cwt)
 * scope_link_prefix ::= TOKEN //e.g. "event_target:" while the link's prefix is "event_target:"
 * scope_link_data_source ::= EXPRESSION //e.g. "some_variable" while the link's data source is "value[variable]"
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
interface ParadoxScopeFieldExpression : ParadoxScriptComplexExpression {
	val scopes: List<ParadoxScopeExpressionNode>
	
	companion object Resolver
}

class ParadoxScopeFieldExpressionImpl(
	override val text: String,
	override val rangeInExpression: TextRange,
	override val isKey: Boolean?,
	override val nodes: MutableList<ParadoxScriptExpressionNode>,
	override val errors: MutableList<ParadoxScriptExpressionError>
) : AbstractExpression(text), ParadoxScopeFieldExpression {
	override val quoted: Boolean = false
	
	override val scopes: List<ParadoxScopeExpressionNode> get() = nodes.filterIsInstance<ParadoxScopeExpressionNode>()
	
	override fun complete(context: ProcessingContext, result: CompletionResultSet) {
		val offsetInParent = context.offsetInParent
		for((index, node) in nodes.withIndex()) {
			if(node is ParadoxScopeExpressionNode) {
				val nodeRange = rangeInExpression
				if(offsetInParent >= nodeRange.startOffset && offsetInParent <= nodeRange.endOffset) {
					val keyword = context.keyword
					context.put(PlsCompletionKeys.keywordKey, text)
					CwtConfigHandler.completeSystemScope(context, result)
					CwtConfigHandler.completeScope(context, result)
					CwtConfigHandler.completeScopeLinkPrefixOrDataSource(context, result)
					context.put(PlsCompletionKeys.keywordKey, keyword)
				}
			}
		}
	}
}

fun Resolver.resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, isKey: Boolean? = null, completionOffset: Int = -1): ParadoxScopeFieldExpression? {
	val nodes = SmartList<ParadoxScriptExpressionNode>()
	val errors = SmartList<ParadoxScriptExpressionError>()
	val offset = textRange.startOffset
	var startIndex: Int
	var endIndex: Int = -1
	while(endIndex < text.length) {
		startIndex = endIndex + 1
		endIndex = text.indexOf('.', startIndex).let { if(it == -1) text.length else it }
		val dotNode = if(endIndex == text.length) {
			ParadoxScriptOperatorExpressionNode(".", TextRange.create(endIndex + offset, endIndex + 1 + offset))
		} else {
			null
		}
		val nodeText = text.substring(startIndex, endIndex)
		//unexpected token -> malformed
		if(!nodeText.all { it == ':' || it == '_' || it == '@' || it.isExactLetter() || it.isExactDigit() }) {
			val error = ParadoxMalformedScopeFieldExpressionError(textRange, PlsBundle.message("script.expression.malformedScopeFieldExpression", text))
			errors.add(error)
			break
		}
		val nodeTextRange = TextRange.create(startIndex + offset, endIndex + offset)
		val node = ParadoxScopeExpressionNode.resolve(nodeText, nodeTextRange, configGroup)
		//handle mismatch situation
		if(startIndex == 0 && node.nodes.isEmpty() && completionOffset == -1) {
			return null
		}
		//handle missing scope situation
		if(startIndex != 0 && endIndex == text.length && nodeText.isEmpty()) {
			val error = ParadoxMissingScopeExpressionError(textRange, PlsBundle.message("script.expression.missingScopeExpression"))
			errors.add(error)
		}
		nodes.add(node)
		if(dotNode != null) nodes.add(dotNode)
	}
	return ParadoxScopeFieldExpressionImpl(text, textRange, isKey, nodes, errors)
}
