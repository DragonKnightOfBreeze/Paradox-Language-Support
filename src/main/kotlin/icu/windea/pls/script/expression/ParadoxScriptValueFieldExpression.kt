package icu.windea.pls.script.expression

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.CwtConfigHandler.completeScope
import icu.windea.pls.config.cwt.CwtConfigHandler.completeValueFieldPrefixOrDataSource
import icu.windea.pls.config.cwt.CwtConfigHandler.completeValueOfValueField
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.script.psi.*

/**
 * 值字段表达式。
 *
 * 一些例子：`trigger:xxx` `root.trigger:xxx` `value:xxx|PN|PV|`
 */
class ParadoxScriptValueFieldExpression(
	expressionString: String,
	configGroup: CwtConfigGroup,
	infos: List<ParadoxScriptExpressionInfo> = emptyList(),
	errors: List<ParadoxScriptExpressionError> = emptyList()
) : ParadoxScriptExpression(expressionString, configGroup, infos, errors) {
	companion object Resolver : ParadoxScriptExpressionResolver<ParadoxScriptValueFieldExpression>() {
		val EmptyExpression by lazy { ParadoxScriptValueFieldExpression("", MockCwtConfigGroup).apply { empty = true } }
		
		override fun doResolve(expressionString: String, configGroup: CwtConfigGroup): ParadoxScriptValueFieldExpression {
			if(expressionString.isEmpty()) return EmptyExpression
			
			var isValid = true
			var isMatched = false
			val wholeRange = TextRange.create(0, expressionString.length)
			val infos = SmartList<ParadoxScriptExpressionInfo>()
			val errors = SmartList<ParadoxScriptExpressionError>()
			val textRanges = SmartList<TextRange>()
			//这里需要找到DS的结束位置
			val textToIterateLength = when {
				expressionString.startsWith("value:") -> expressionString.substringAfter("value:").indexOf("|")
				expressionString.contains(".value:") -> expressionString.substringAfter(".value:").indexOf("|")
				else -> -1
			}.let { if(it != -1) it else expressionString.length }
			val textToIterate = expressionString.substring(0, textToIterateLength)
			var startIndex: Int
			var endIndex: Int = -1
			while(endIndex < textToIterate.length) {
				startIndex = endIndex + 1
				endIndex = textToIterate.indexOf('.', startIndex).let { if(it != -1) it else expressionString.length }
				textRanges.add(TextRange.create(startIndex, endIndex))
			}
			//加入"."的expressionInfo
			for(pipeIndex in textToIterate.indicesOf('.')) {
				infos.add(ParadoxScriptOperatorExpressionInfo(".", TextRange.create(pipeIndex, pipeIndex + 1)))
			}
			for((index, textRange) in textRanges.withIndex()) {
				val text = textRange.substring(expressionString)
					.let { if(it.startsWith("value:")) it.substringBefore('|') else it }
				if(text.isParameterAwareExpression()) {
					//如果子表达式可能带有参数，则跳过继续解析
					continue
				}
				if(text.isEmpty() || !text.isValidSubExpression()) {
					//如果表达式格式不正确
					isValid = false
					val error = ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.valueField.malformed", expressionString), wholeRange)
					errors.clear()
					errors.add(error)
					break
				}
				//可以解析，或者不是最后一个，继续
				if(index != textRanges.lastIndex) {
					val resolved = CwtConfigHandler.resolveScope(text, configGroup)
					val info = ParadoxScriptScopeExpressionInfo(text, textRange, resolved, configGroup.linksAsScopePrefixes)
					infos.add(info)
					isMatched = true //可解析 -> 可匹配 / 也认为匹配，实际上这里无法判断
				} else {
					val resolved = CwtConfigHandler.resolveValueOfValueField(text, configGroup)
					//可以解析，继续
					if(resolved != null) {
						val info = ParadoxScriptValueOfValueFieldExpressionInfo(text, textRange, resolved)
						infos.add(info)
						isMatched = true //可解析 -> 可匹配 
						continue
					}
					
					val matchedLinkConfigs = configGroup.linksAsValue.values
						.filter { it.prefix != null && it.dataSource != null && text.startsWith(it.prefix) }
					if(matchedLinkConfigs.isNotEmpty()) {
						//匹配某一前缀
						val prefix = matchedLinkConfigs.first().prefix!!
						val prefixRange = TextRange.create(textRange.startOffset, textRange.startOffset + prefix.length)
						
						val directlyResolvedList = matchedLinkConfigs.mapNotNull { it.pointer.element }
						val prefixInfo = ParadoxScriptValueFieldPrefixExpressionInfo(prefix, prefixRange, directlyResolvedList)
						infos.add(prefixInfo)
						val textWithoutPrefix = text.drop(prefix.length)
						if(textWithoutPrefix.isEmpty()) {
							//缺少dataSource
							val dataSourcesText = matchedLinkConfigs.joinToString { "'${it.dataSource}'" }
							val error = ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.valueField.missingDs", dataSourcesText), textRange)
							errors.add(error)
							isMatched = true //认为匹配
						} else {
							val startIndexDs = textRange.startOffset + prefix.length
							val endIndexDs = if(prefix == "value:") {
								expressionString.indexOf('|', startIndexDs).let { if(it != -1) it else textRange.endOffset }
							} else {
								textRange.endOffset
							}
							val dataSourceRange = TextRange.create(startIndexDs, endIndexDs)
							val dataSourceText = dataSourceRange.substring(expressionString)
							val dataSourceInfo = ParadoxScriptValueFieldDataSourceExpressionInfo(dataSourceText, dataSourceRange, matchedLinkConfigs)
							infos.add(dataSourceInfo)
							isMatched = true //认为匹配
							
							//特殊处理带参数的SV表达式
							if(prefix == "value:" && endIndexDs != textRange.endOffset) {
								var continuousPipe = false
								val svName = dataSourceText
								val textRangesSv = SmartList<TextRange>()
								var startIndexSv: Int
								var endIndexSv: Int = endIndexDs
								while(endIndexSv != textRange.endOffset) {
									startIndexSv = endIndexSv + 1
									endIndexSv = expressionString.indexOf('|', startIndexSv).let { if(it != -1) it else textRange.endOffset }
									if(endIndexSv == textRange.endOffset) {
										//末尾的管道符
										continue
									} else if(startIndexSv == endIndexSv) {
										//连续的管道符
										continuousPipe = true
										continue
									}
									textRangesSv.add(TextRange.create(startIndexSv, endIndexSv))
								}
								var flag = false
								for(textRangeSv in textRangesSv) {
									flag = !flag
									val textSv = textRangeSv.substring(expressionString)
									if(textSv.isParameterAwareExpression()) {
										//如果子表达式可能带有参数，则跳过继续解析
										continue
									}
									val info = if(flag) {
										ParadoxScriptSvParameterExpressionInfo(textSv, textRangeSv, svName)
									} else {
										ParadoxScriptSvParameterValueExpressionInfo(textSv, textRangeSv)
									}
									infos.add(info)
								}
								//加入"|"的expressionInfo
								for(pipeIndex in expressionString.indicesOf('|', prefixRange.endOffset)) {
									infos.add(ParadoxScriptMarkerExpressionInfo("|", TextRange.create(pipeIndex, pipeIndex + 1)))
								}
								
								if(continuousPipe || expressionString.last() != '|' || flag) {
									//不合法的SV表达式：连续的管道符，或者不以管道符结尾，或者最后一个参数名缺少对应的参数值
									val error = ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.sv.malformed", text), textRange)
									errors.add(error)
								}
							}
						}
					} else {
						//没有前缀
						//无法解析的value，或者要求有前缀
						val info = ParadoxScriptValueOfValueFieldExpressionInfo(text, textRange, null, configGroup.linksAsValuePrefixes)
						infos.add(info)
						isMatched = true //也认为匹配，实际上这里无法判断
						
						//这里认为必须要有前缀
					}
				}
			}
			
			return ParadoxScriptValueFieldExpression(expressionString, configGroup, infos, errors)
				.apply { valid = isValid }
				.apply { matched = isMatched }
		}
		
		private fun String.isValidSubExpression(): Boolean {
			return all { it == '_' || it == ':' || it == '|' || it.isExactLetter() || it.isExactDigit() }
		}
	}
	
	override fun ProcessingContext.doComplete(result: CompletionResultSet) {
		//基于点号进行代码提示，因此允许最终会导致表达式不合法的情况
		//TODO 兼容带参数的SV表达式
		val offsetInParent = offsetInParent
		val start = expressionString.lastIndexOf('.').let { if(it == -1) 0 else it + 1 }
		val end = expressionString.indexOf('.', offsetInParent).let { if(it == -1) expressionString.length else it }
		val isLast = end == expressionString.length
		
		val prevScope = if(start == 0) null else expressionString.substring(0, start - 1).substringAfterLast('.')
		if(prevScope != null) put(PlsCompletionKeys.prevScopeKey, prevScope)
		
		val keyword = keyword
		val keywordToUse = expressionString.substring(start, end)
		put(PlsCompletionKeys.keywordKey, keywordToUse)
		
		if(isLast) {
			completeScope(result)
			completeValueOfValueField(result)
			completeValueFieldPrefixOrDataSource(result)
		} else {
			completeScope(result)
		}
		
		put(PlsCompletionKeys.keywordKey, keyword)
		put(PlsCompletionKeys.prevScopeKey, null)
		
		result.restartCompletionOnAnyPrefixChange() //要求重新匹配
	}
}