package icu.windea.pls.core.expression

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.expression.ParadoxScriptValueExpression.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

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
	val config: CwtConfig<*>
	
	val scriptValueNode: ParadoxScriptValueExpressionNode
	val parameterNodes: List<ParadoxScriptValueArgumentExpressionNode>
	
	companion object Resolver
}

class ParadoxScriptValueExpressionImpl(
	override val text: String,
	override val isKey: Boolean?,
	override val rangeInExpression: TextRange,
	override val nodes: List<ParadoxExpressionNode>,
	override val config: CwtConfig<*>,
	override val configGroup: CwtConfigGroup
) : AbstractExpression(text), ParadoxScriptValueExpression {
	override val quoted: Boolean = false
	
	override val scriptValueNode: ParadoxScriptValueExpressionNode get() = nodes.first().cast()
	override val parameterNodes: List<ParadoxScriptValueArgumentExpressionNode> get() = nodes.filterIsInstance<ParadoxScriptValueArgumentExpressionNode>()
	
	override fun validate(): List<ParadoxExpressionError> {
		var malformed = false
		val errors = SmartList<ParadoxExpressionError>()
		var pipeCount = 0
		var lastIsParameter = false
		for((index, node) in nodes.withIndex()) {
			val isLast = index == nodes.lastIndex
			if(node is ParadoxTokenExpressionNode) {
				pipeCount++
			} else {
				if(isLast && node.text.isEmpty()) continue
				if(!malformed && (node.text.isEmpty() || !isValid(node))) {
					malformed = true
				}
				when(node) {
					is ParadoxScriptValueArgumentExpressionNode -> lastIsParameter = true
					is ParadoxScriptValueArgumentValueExpressionNode -> lastIsParameter = false
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
	
	private fun isValid(node: ParadoxExpressionNode): Boolean {
		return when(node){
			is ParadoxScriptValueArgumentExpressionNode -> node.text.isExactIdentifier()
			is ParadoxScriptValueArgumentValueExpressionNode -> node.text.isExactParameterizedIdentifier('.','-','+') //兼容数字文本
			else -> node.text.isExactParameterizedIdentifier()
		}
	}
	
	override fun complete(context: ProcessingContext, result: CompletionResultSet) {
		val scopeContext = context.scopeContext
		val scopeMatched = context.scopeMatched
		val keyword = context.keyword
		val isKey = context.isKey
		val offsetInParent = context.offsetInParent
		
		context.put(PlsCompletionKeys.scopeContextKey, null) //don't check now
		
		for(node in nodes) {
			val nodeRange = node.rangeInExpression
			val inRange = offsetInParent >= nodeRange.startOffset && offsetInParent <= nodeRange.endOffset
			if(node is ParadoxScriptValueExpressionNode) {
				if(inRange) {
					val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
					val resultToUse = result.withPrefixMatcher(keywordToUse)
					context.put(PlsCompletionKeys.keywordKey, keywordToUse)
					val config = context.config
					val configs = context.configs
					context.put(PlsCompletionKeys.configKey, this.config)
					context.put(PlsCompletionKeys.configsKey, null)
					ParadoxConfigHandler.completeScriptExpression(context, resultToUse)
					context.put(PlsCompletionKeys.configKey, config)
					context.put(PlsCompletionKeys.configsKey, configs)
				}
			} else if(node is ParadoxScriptValueArgumentExpressionNode) {
				if(inRange && scriptValueNode.text.isNotEmpty()) {
					val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
					val resultToUse = result.withPrefixMatcher(keywordToUse)
					context.put(PlsCompletionKeys.keywordKey, keywordToUse)
					ParadoxParameterHandler.completeArguments(context.contextElement, context, resultToUse)
				}
			} else if(node is ParadoxScriptValueArgumentValueExpressionNode && getSettings().inference.argumentValueConfig) {
				if(inRange && scriptValueNode.text.isNotEmpty()) {
					//尝试提示传入参数的值
					run {
						val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
						val resultToUse = result.withPrefixMatcher(keywordToUse)
						val element = context.contextElement as? ParadoxScriptStringExpressionElement ?: return@run
						val parameterElement = node.argumentNode?.getReference(element)?.resolve() ?: return@run
						val inferredConfig = ParadoxParameterHandler.inferConfig(parameterElement) ?: return@run
						val config = context.config
						val configs = context.configs
						context.put(PlsCompletionKeys.configKey, inferredConfig)
						context.put(PlsCompletionKeys.configsKey, null)
						context.put(PlsCompletionKeys.keywordKey, keywordToUse)
						ParadoxConfigHandler.completeScriptExpression(context, resultToUse)
						context.put(PlsCompletionKeys.configKey, config)
						context.put(PlsCompletionKeys.configsKey, configs)
					}
				}
			}
		}
		context.put(PlsCompletionKeys.scopeContextKey, scopeContext)
		context.put(PlsCompletionKeys.scopeMatchedKey, scopeMatched)
		context.put(PlsCompletionKeys.keywordKey, keyword)
		context.put(PlsCompletionKeys.isKeyKey, isKey)
	}
}

fun Resolver.resolve(text: String, textRange: TextRange, config: CwtConfig<*>, configGroup: CwtConfigGroup, isKey: Boolean? = null): ParadoxScriptValueExpression {
	val nodes = SmartList<ParadoxExpressionNode>()
	val offset = textRange.startOffset
	var n = 0
	var scriptValueNode: ParadoxScriptValueExpressionNode? = null
	var parameterNode: ParadoxScriptValueArgumentExpressionNode? = null
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
				ParadoxScriptValueExpressionNode.resolve(nodeText, nodeRange, config, configGroup)
					.also { scriptValueNode = it }
			}
			n % 2 == 1 -> {
				ParadoxScriptValueArgumentExpressionNode.resolve(nodeText, nodeRange, scriptValueNode, configGroup)
					.also { parameterNode = it }
			}
			n % 2 == 0 -> {
				ParadoxScriptValueArgumentValueExpressionNode.resolve(nodeText, nodeRange, scriptValueNode, parameterNode, configGroup)
			}
			else -> throw InternalError()
		}
		nodes.add(node)
		if(pipeNode != null) nodes.add(pipeNode)
		n++
	}
	return ParadoxScriptValueExpressionImpl(text, isKey, textRange, nodes, config, configGroup)
}
