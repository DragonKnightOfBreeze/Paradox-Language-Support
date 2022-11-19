package icu.windea.pls.script.exp

import com.intellij.openapi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.script.exp.ParadoxScopeFieldExpression.*
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
}

fun Resolver.resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, isKey: Boolean? = null, completionOffset: Int = -1): ParadoxScopeFieldExpression? {
	val nodes = mutableListOf<ParadoxScriptExpressionNode>()
	val errors = mutableListOf<ParadoxScriptExpressionError>()
	var malformed = false
	val offset = textRange.startOffset
	var startIndex: Int
	var endIndex: Int = -1
	while(endIndex < text.length) {
		startIndex = endIndex + 1
		endIndex = text.indexOf('.', startIndex)
		val dotNode = if(endIndex != -1) {
			ParadoxScriptOperatorExpressionNode(".", TextRange.create(endIndex + offset, endIndex + 1 + offset))
		} else {
			endIndex = text.length
			null
		}
		val nodeText = text.substring(startIndex, endIndex)
		//empty node
		if(nodeText.isEmpty() && completionOffset != startIndex) {
			malformed = true
		}
		//unexpected token -> stop
		if(!nodeText.all { it == ':' || it == '_' || it.isExactLetter() || it.isExactDigit() }) {
			malformed = true
			break
		}
		val nodeTextRange = TextRange.create(startIndex + offset, endIndex + offset)
		val node = ParadoxScopeExpressionNode.resolve(nodeText, nodeTextRange, configGroup)
		//handle mismatch situation
		if(startIndex == 0 && node.nodes.isEmpty()) return null
		nodes.add(node)
		if(dotNode != null) nodes.add(dotNode)
	}
	if(malformed) {
		val error = ParadoxScriptExpressionError(textRange, PlsBundle.message("script.expression.malformedScopeFieldExpression", text))
		errors.add(error)
	}
	return ParadoxScopeFieldExpressionImpl(text, textRange, isKey, nodes, errors)
}
