package icu.windea.pls.core.expression

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.ParadoxScriptValueExpression.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.script.highlighter.*

/**
 * 封装值表达式。
 *
 * 语法：
 *
 * ```bnf
 * script_value_expression ::= script_value ("|" (param_name "|" param_value "|")+)?
 * script_value ::= TOKEN //matching config expression "<script_value>"
 * param_name ::= TOKEN //parameter name, no surrounding "$"
 * param_value ::= TOKEN //boolean, int, float or string
 * ```
 *
 * 示例：
 *
 * ```
 * some_sv
 * some_sv|PARAM|VALUE|
 * ```
 */
interface ParadoxScriptValueExpression : ParadoxComplexExpression {
	val scriptValueNode: ParadoxScriptValueExpressionNode
	val parameterNodes: List<ParadoxScriptValueParameterExpressionNode>
	
	//TODO
	
	companion object Resolver
}

class ParadoxScriptValueExpressionImpl(
	override val text: String,
	override val rangeInExpression: TextRange,
	override val isKey: Boolean?,
	override val nodes: List<ParadoxScriptExpressionNode>
) : AbstractExpression(text), ParadoxScriptValueExpression {
	override val quoted: Boolean = false
	
	override val scriptValueNode: ParadoxScriptValueExpressionNode get() = nodes.first().cast()
	override val parameterNodes: List<ParadoxScriptValueParameterExpressionNode> get() = nodes.filterIsInstance<ParadoxScriptValueParameterExpressionNode>()
	
	override fun getAttributesKey() = ParadoxScriptAttributesKeys.SCRIPT_VALUE_EXPRESSION_KEY
	
	override fun validate(): List<ParadoxExpressionError> {
		var malformed = false
		val errors = SmartList<ParadoxExpressionError>()
		var pipeCount = 0
		var lastIsParameter = false
		for((index, node) in nodes.withIndex()) {
			val isLast = index == nodes.lastIndex
			if(node is ParadoxOperatorExpressionNode) {
				pipeCount++
			} else {
				if(!malformed && isLast || node.text.isEmpty() || !node.text.all { it.isExactIdentifierChar() }) {
					malformed = true
				}
				when(node) {
					is ParadoxScriptValueParameterExpressionNode -> lastIsParameter = true
					is ParadoxScriptValueParameterValueExpressionNode -> lastIsParameter = false
				}
			}
		}
		//0, 1, 3, 5, ...
		if(!malformed && pipeCount != 0 && pipeCount % 2 == 0) {
			malformed = true
		}
		if(malformed) {
			val error = ParadoxMalformedScriptValueExpressionExpressionError(rangeInExpression, PlsBundle.message("script.expression.malformedScriptValueExpression", text))
			errors.add(error)
		}
		if(lastIsParameter) {
			val error = ParadoxMissingParameterValueExpressionExpressionError(rangeInExpression, PlsBundle.message("script.expression.missingParameterValueExpression"))
			errors.add(error)
		}
		return errors
	}
	
	override fun complete(context: ProcessingContext, result: CompletionResultSet) {
		super.complete(context, result)
	}
}

fun Resolver.resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, isKey: Boolean? = null): ParadoxScriptValueExpression {
	val nodes = SmartList<ParadoxScriptExpressionNode>()
	val offset = textRange.startOffset
	var n = 0
	var scriptValueName: String? = null
	var parameterName: String? = null
	var index: Int
	var pipeIndex = -1
	while(pipeIndex < text.length) {
		index = pipeIndex + 1
		pipeIndex = text.indexOf('|', index)
		val pipeNode = if(pipeIndex != -1) {
			val pipeRange = TextRange.create(pipeIndex + offset, pipeIndex + 1 + offset)
			ParadoxMarkerExpressionNode("|", pipeRange)
		} else {
			null
		}
		if(pipeIndex == -1){
			pipeIndex = text.length
		}
		val nodeText = text.substring(index, pipeIndex)
		val nodeRange = TextRange.create(index + offset, pipeIndex + offset)
		val node = when {
			n == 0 -> {
				scriptValueName = nodeText
				ParadoxScriptValueExpressionNode.resolve(nodeText, nodeRange, configGroup)
			}
			n % 2 == 1 -> {
				parameterName = nodeText
				ParadoxScriptValueParameterExpressionNode.resolve(nodeText, nodeRange, scriptValueName, configGroup)
			}
			n % 2 == 0 -> {
				ParadoxScriptValueParameterValueExpressionNode.resolve(nodeText, nodeRange, scriptValueName, parameterName, configGroup)
			}
			else -> throw InternalError()
		}
		nodes.add(node)
		if(pipeNode != null) nodes.add(pipeNode)
		n++
	}
	return ParadoxScriptValueExpressionImpl(text, textRange, isKey, nodes)
}
