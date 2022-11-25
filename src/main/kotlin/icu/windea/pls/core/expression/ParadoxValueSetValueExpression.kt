package icu.windea.pls.core.expression

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.ParadoxValueSetValueExpression.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.script.highlighter.*

/**
 * 值集值表达式。
 *
 * 语法：
 *
 * ```bnf
 * value_set_value_expression ::= value_set_value ("@" scope_field_expression)?
 * value_set_value ::= TOKEN //matching config expression "value[xxx]" or "value_set[xxx]"
 * //"event_target:t1.v1@event_target:t2.v2@..." is not used in vanilla files but allowed here
 * ```
 *
 * 示例：
 *
 * ```
 * some_variable
 * some_variable@root
 * ```
 */
interface ParadoxValueSetValueExpression : ParadoxComplexExpression {
	val configExpressions: List<CwtDataExpression>
	val configGroup: CwtConfigGroup
	
	val valueSetValueNode: ParadoxValueSetValueExpressionNode
	val scopeFieldExpression: ParadoxScopeFieldExpression?
	
	companion object Resolver
}

class ParadoxValueSetValueExpressionImpl(
	override val text: String,
	override val rangeInExpression: TextRange,
	override val isKey: Boolean?,
	override val nodes: List<ParadoxScriptExpressionNode>,
	override val errors: List<ParadoxExpressionError>,
	override val configExpressions: List<CwtDataExpression>,
	override val configGroup: CwtConfigGroup
) : AbstractExpression(text), ParadoxValueSetValueExpression {
	override val quoted: Boolean = false
	
	override val valueSetValueNode: ParadoxValueSetValueExpressionNode get() = nodes.get(0).cast()
	override val scopeFieldExpression: ParadoxScopeFieldExpression? get() = nodes.getOrNull(2)?.cast()
	
	override fun getAttributesKey() = ParadoxScriptAttributesKeys.VALUE_SET_VALUE_EXPRESSION_KEY
	
	override fun complete(context: ProcessingContext, result: CompletionResultSet) {
		val keyword = context.keyword
		val isKey = context.isKey
		context.put(PlsCompletionKeys.isKeyKey, null)
		
		val offsetInParent = context.offsetInParent
		for(node in nodes) {
			val nodeRange = node.rangeInExpression
			val inRange = offsetInParent >= nodeRange.startOffset && offsetInParent <= nodeRange.endOffset
			if(node is ParadoxValueSetValueExpressionNode) {
				if(inRange) {
					val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
					val resultToUse = result.withPrefixMatcher(keywordToUse)
					context.put(PlsCompletionKeys.keywordKey, keywordToUse)
					CwtConfigHandler.completeValueSetValue(context, resultToUse)
					break
				}
			} else if(node is ParadoxScopeFieldExpression) {
				if(inRange) {
					val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
					val resultToUse = result.withPrefixMatcher(keywordToUse)
					context.put(PlsCompletionKeys.keywordKey, keywordToUse)
					node.complete(context, resultToUse)
					break
				}
			}
		}
		context.put(PlsCompletionKeys.keywordKey, keyword)
		context.put(PlsCompletionKeys.isKeyKey, isKey)
	}
}

fun Resolver.resolve(text: String, textRange: TextRange, configExpression: CwtDataExpression, configGroup: CwtConfigGroup, isKey: Boolean? = null): ParadoxValueSetValueExpression? {
	return resolve(text, textRange, configExpression.toSingletonList(), configGroup, isKey)
}

fun Resolver.resolve(text: String, textRange: TextRange, configExpressions: List<CwtDataExpression>, configGroup: CwtConfigGroup, isKey: Boolean? = null): ParadoxValueSetValueExpression? {
	val nodes = SmartList<ParadoxScriptExpressionNode>()
	val errors = SmartList<ParadoxExpressionError>()
	val offset = textRange.startOffset
	val atIndex = text.indexOf('@')
	run {
		if(atIndex == -1) {
			atIndex == text.length
		}
		//resolve valueSetValueNode
		val nodeText = text.substring(0, atIndex)
		val nodeTextRange = TextRange.create(offset, atIndex + offset)
		val node = icu.windea.pls.core.expression.nodes.ParadoxValueSetValueExpressionNode.resolve(nodeText, nodeTextRange, configExpressions, configGroup)
		if(node == null) return null //unexpected
		nodes.add(node)
		if(atIndex != text.length) {
			//resolve at token
			val atNode = ParadoxScriptMarkerExpressionNode("@", TextRange.create(atIndex + offset, atIndex + 1 + offset))
			nodes.add(atNode)
			//resolve scope expression
			val expText = text.substring(atIndex + 1)
			if(expText.isEmpty()) {
				val error = ParadoxMissingScopeFieldExpressionExpressionError(textRange, PlsBundle.message("script.expression.missingScopeField"))
				errors.add(error)
			}
			val expTextRange = TextRange.create(atIndex + 1, text.length)
			val expNode = ParadoxScopeFieldExpression.resolve(expText, expTextRange, configGroup, null, true)
			nodes.add(expNode)
		}
	}
	if(nodes.isEmpty()) return null
	return ParadoxValueSetValueExpressionImpl(text, textRange, isKey, nodes, errors, configExpressions, configGroup)
}
