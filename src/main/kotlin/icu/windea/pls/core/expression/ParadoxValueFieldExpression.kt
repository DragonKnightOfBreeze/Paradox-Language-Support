package icu.windea.pls.core.expression

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.expression.ParadoxValueFieldExpression.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.script.highlighter.*

/**
 * 值字段表达式。
 *
 * 语法：
 *
 * ```bnf
 * value_field_expression ::= scope * value_field
 * scope ::= system_scope | scope_link | scope_link_from_data
 * system_scope ::= TOKEN //predefined by CWT Config (in system_scopes.pls.cwt)
 * scope_link ::= TOKEN //predefined by CWT Config (in links.cwt)
 * scope_link_from_data ::= scope_link_prefix scope_link_data_source //predefined by CWT Config (in links.cwt)
 * scope_link_prefix ::= TOKEN //e.g. "event_target:" while the link's prefix is "event_target:"
 * scope_link_data_source ::= expression //e.g. "some_variable" while the link's data source is "value[variable]"
 * value_field ::= value_link | value_link_from_data
 * value_link ::= TOKEN //predefined by CWT Config (in links.cwt, from_data = false, type = both | value)
 * value_link_from_data ::= value_link_prefix value_link_data_source //predefined by CWT Config (in links.cwt, from_data = true, type = both | value)
 * value_link_prefix ::= TOKEN //e.g. "value:" while the link's prefix is "value:"
 * value_link_data_source ::= expression //e.g. "some" while the link's data source is "value[variable]"
 * expression ::= data_expression | value_set_value_expression | sv_expression //see: ParadoxDataExpression, ParadoxValueSetValueExpression
 * sv_expression ::= sv_name ("|" (param_name "|" param_value "|")+)? //e.g. value:some_sv|PARAM1|VALUE1|PARAM2|VALUE2|
 * ```
 *
 * 示例：
 *
 * ```
 * trigger:some_trigger
 * value:some_sv|PARAM1|VALUE1|PARAM2|VALUE2|
 * root.owner.some_variable
 * ```
 */
interface ParadoxValueFieldExpression : ParadoxComplexExpression {
	val scopeNodes: List<ParadoxScopeExpressionNode>
	
	val valueFieldNode: ParadoxValueFieldExpressionNode?
	
	companion object Resolver
}

class ParadoxValueFieldExpressionImpl(
	override val text: String,
	override val rangeInExpression: TextRange,
	override val isKey: Boolean?,
	override val nodes: List<ParadoxExpressionNode>
) : AbstractExpression(text), ParadoxValueFieldExpression {
	override val quoted: Boolean = false
	
	override val scopeNodes: List<ParadoxScopeExpressionNode> = nodes.filterIsInstance<ParadoxScopeExpressionNode>()
	
	override val valueFieldNode: ParadoxValueFieldExpressionNode = nodes.last().cast()
	
	override fun getAttributesKey() = ParadoxScriptAttributesKeys.VALUE_FILED_EXPRESSION_KEY
	
	override fun validate(): List<ParadoxExpressionError> {
		val errors = SmartList<ParadoxExpressionError>()
		var malformed = false
		for((index, node) in nodes.withIndex()) {
			val isLast = index == nodes.lastIndex
			when(node) {
				is ParadoxScopeExpressionNode -> {
					if(node.text.isEmpty()) {
						if(!malformed) {
							malformed = true
						}
					} else {
						if(node is ParadoxScopeLinkFromDataExpressionNode) {
							val dataSourceNode = node.dataSourceNode
							for(dataSourceChildNode in dataSourceNode.nodes) {
								when(dataSourceChildNode) {
									is ParadoxDataExpressionNode -> {
										if(dataSourceChildNode.text.isEmpty()) {
											if(!malformed) {
												malformed = true
											}
										} else if(!malformed && !isValid(dataSourceChildNode)) {
											malformed = true
										}
									}
									is ParadoxValueSetValueExpression -> {
										errors.addAll(dataSourceChildNode.validate())
									}
									is ParadoxErrorTokenExpressionNode -> {
										malformed = true
									}
								}
							}
						}
					}
				}
				is ParadoxValueFieldExpressionNode -> {
					if(node.text.isEmpty()) {
						if(isLast) {
							val error = ParadoxMissingScopeExpressionError(rangeInExpression, PlsBundle.message("script.expression.missingValueField"))
							errors.add(error)
						} else if(!malformed) {
							malformed = true
						}
					} else {
						if(node is ParadoxValueLinkFromDataExpressionNode) {
							val dataSourceNode = node.dataSourceNode
							for(dataSourceChildNode in dataSourceNode.nodes) {
								when(dataSourceChildNode) {
									is ParadoxDataExpressionNode -> {
										if(dataSourceChildNode.text.isEmpty()) {
											if(isLast) {
												val expect = dataSourceChildNode.linkConfigs.mapNotNullTo(mutableSetOf()) { it.expression }.joinToString()
												val error = ParadoxMissingValueLinkDataSourceExpressionError(rangeInExpression, PlsBundle.message("script.expression.missingValueLinkDataSource", expect))
												errors.add(error)
											} else if(!malformed) {
												malformed = true
											}
										} else if(!malformed && !isValid(dataSourceChildNode)) {
											malformed = true
										}
									}
									is ParadoxValueSetValueExpression -> {
										errors.addAll(dataSourceChildNode.validate())
									}
									is ParadoxScriptValueExpression -> {
										errors.addAll(dataSourceChildNode.validate())
									}
									is ParadoxErrorTokenExpressionNode -> {
										malformed = true
									}
								}
							}
						}
					}
				}
			}
		}
		if(malformed) {
			val error = ParadoxMalformedValueFieldExpressionExpressionError(rangeInExpression, PlsBundle.message("script.expression.malformedValueFieldExpression", text))
			errors.add(0, error)
		}
		return errors
	}
	
	private fun isValid(node: ParadoxExpressionNode): Boolean {
		return node.text.all { it.isExactIdentifierChar() }
	}
	
	override fun complete(context: ProcessingContext, result: CompletionResultSet) {
		val keyword = context.keyword
		val isKey = context.isKey
		val prevScope = context.prevScope
		context.put(PlsCompletionKeys.isKeyKey, null)
		
		val offsetInParent = context.offsetInParent
		var prevScopeToUse: String? = null
		for(node in nodes) {
			val nodeRange = node.rangeInExpression
			val inRange = offsetInParent >= nodeRange.startOffset && offsetInParent <= nodeRange.endOffset
			if(!inRange) {
				//如果光标位置之前存在无法解析的scope（除非被解析为scopeLinkFromData，例如，"event_target:xxx"），不要进行补全
				if(node is ParadoxErrorExpressionNode || node.text.isEmpty()) break
			}
			if(node is ParadoxScopeExpressionNode) {
				if(inRange) {
					context.put(PlsCompletionKeys.prevScopeKey, prevScopeToUse)
					val linkFromDataNode = node.castOrNull<ParadoxValueLinkFromDataExpressionNode>()
					val prefixNode = linkFromDataNode?.prefixNode
					val dataSourceNode = linkFromDataNode?.dataSourceNode
					val dataSourceNodeToCheck = dataSourceNode?.nodes?.first()
					val endOffset = dataSourceNode?.rangeInExpression?.startOffset ?: -1
					if(prefixNode != null && dataSourceNode != null && offsetInParent >= dataSourceNode.rangeInExpression.startOffset) {
						val keywordToUse = dataSourceNode.text.substring(0, offsetInParent - endOffset)
						val resultToUse = result.withPrefixMatcher(keywordToUse)
						context.put(PlsCompletionKeys.keywordKey, keywordToUse)
						val prefix = prefixNode.text
						CwtConfigHandler.completeScopeLinkDataSource(context, resultToUse, prefix, dataSourceNodeToCheck)
					} else {
						val inFirstNode = dataSourceNode == null || dataSourceNode.nodes.isEmpty()
							|| offsetInParent <= dataSourceNode.nodes.first().rangeInExpression.endOffset
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
			} else if(node is ParadoxValueFieldExpressionNode) {
				if(inRange) {
					context.put(PlsCompletionKeys.prevScopeKey, prevScopeToUse)
					val linkFromDataNode = node.castOrNull<ParadoxValueLinkFromDataExpressionNode>()
					val prefixNode = linkFromDataNode?.prefixNode
					val dataSourceNode = linkFromDataNode?.dataSourceNode
					val dataSourceNodeToCheck = dataSourceNode?.nodes?.first()
					val endOffset = dataSourceNode?.rangeInExpression?.startOffset ?: -1
					if(prefixNode != null && dataSourceNode != null && offsetInParent >= dataSourceNode.rangeInExpression.startOffset) {
						val keywordToUse = dataSourceNode.text.substring(0, offsetInParent - endOffset)
						val resultToUse = result.withPrefixMatcher(keywordToUse)
						context.put(PlsCompletionKeys.keywordKey, keywordToUse)
						val prefix = prefixNode.text
						CwtConfigHandler.completeScopeLinkDataSource(context, resultToUse, prefix, dataSourceNodeToCheck)
						CwtConfigHandler.completeValueLinkDataSource(context, resultToUse, prefix, dataSourceNodeToCheck)
					} else {
						val inFirstNode = dataSourceNode == null || dataSourceNode.nodes.isEmpty()
							|| offsetInParent <= dataSourceNode.nodes.first().rangeInExpression.endOffset
						val keywordToUse = node.text.substring(0, offsetInParent - nodeRange.startOffset)
						val resultToUse = result.withPrefixMatcher(keywordToUse)
						context.put(PlsCompletionKeys.keywordKey, keywordToUse)
						if(inFirstNode) {
							CwtConfigHandler.completeSystemScope(context, resultToUse)
							CwtConfigHandler.completeScope(context, resultToUse)
							CwtConfigHandler.completeScopeLinkPrefix(context, resultToUse)
							CwtConfigHandler.completeValueLinkValue(context, resultToUse)
							CwtConfigHandler.completeValueLinkPrefix(context, resultToUse)
						}
						CwtConfigHandler.completeScopeLinkDataSource(context, resultToUse, null, dataSourceNodeToCheck)
						CwtConfigHandler.completeValueLinkDataSource(context, resultToUse, null, dataSourceNodeToCheck)
						break
					}
				}
			}
		}
		
		context.put(PlsCompletionKeys.keywordKey, keyword)
		context.put(PlsCompletionKeys.isKeyKey, isKey)
		context.put(PlsCompletionKeys.prevScopeKey, prevScope)
	}
}

fun Resolver.resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, isKey: Boolean? = null, canBeMismatched: Boolean = false): ParadoxValueFieldExpression? {
	val nodes = SmartList<ParadoxExpressionNode>()
	val offset = textRange.startOffset
	var isLast = false
	var index: Int
	var dotIndex = -1
	while(dotIndex < text.length) {
		index = dotIndex + 1
		dotIndex = text.indexOf('.', index)
		if(text.indexOf('@', index).let { it != -1 && dotIndex > it }) {
			dotIndex = -1
		}
		if(text.indexOf('|', index).let { it != -1 && dotIndex > it }) {
			dotIndex = -1
		}
		val dotNode = if(dotIndex != -1) {
			val dotRange = TextRange.create(dotIndex + offset, dotIndex + 1 + offset)
			ParadoxOperatorExpressionNode(".", dotRange)
		} else {
			null
		}
		if(dotIndex == -1) {
			dotIndex = text.length
			isLast = true
		}
		//resolve node
		val nodeText = text.substring(index, dotIndex)
		val nodeTextRange = TextRange.create(index + offset, dotIndex + offset)
		val node = when {
			isLast -> ParadoxValueFieldExpressionNode.resolve(nodeText, nodeTextRange, configGroup)
			else -> ParadoxScopeExpressionNode.resolve(nodeText, nodeTextRange, configGroup)
		}
		//handle mismatch situation
		if(!canBeMismatched && index == 0 && node is ParadoxErrorExpressionNode) {
			return null
		}
		nodes.add(node)
		if(dotNode != null) nodes.add(dotNode)
	}
	return ParadoxValueFieldExpressionImpl(text, textRange, isKey, nodes)
}
