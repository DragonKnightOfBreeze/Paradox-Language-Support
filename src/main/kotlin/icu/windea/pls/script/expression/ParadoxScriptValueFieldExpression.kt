package icu.windea.pls.script.expression

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.CwtConfigHandler.completeParametersForScriptValueExpression
import icu.windea.pls.config.cwt.CwtConfigHandler.completeScope
import icu.windea.pls.config.cwt.CwtConfigHandler.completeValueLinkValue
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.script.psi.*

/**
 * 值字段表达式。
 *
 * 一些例子：`trigger:xxx` `root.trigger:xxx` `value:xxx|PN|PV|`
 */
@Deprecated("")
class ParadoxScriptValueFieldExpression(
	expressionString: String,
	configGroup: CwtConfigGroup,
	infos: List<ParadoxScriptExpressionInfo> = emptyList(),
	errors: List<ParadoxScriptExpressionError> = emptyList()
) : ParadoxScriptComplexExpression(expressionString, configGroup, infos, errors) {
	val prefixInfo = infos.findIsInstance<ParadoxScriptValueLinkPrefixExpressionInfo>()
	val dataSourceInfo = infos.findIsInstance<ParadoxScriptValueFieldDataSourceExpressionInfo>()
	val scriptValueParametersStartIndex = if(dataSourceInfo == null) -1 else expressionString.indexOf('|', dataSourceInfo.textRange.endOffset)
	
	companion object Resolver : ParadoxScriptComplexExpressionResolver<ParadoxScriptValueFieldExpression>() {
		val EmptyExpression by lazy { ParadoxScriptValueFieldExpression("", MockCwtConfigGroup).apply { empty = true } }
		
		override fun doResolve(expressionString: String, configGroup: CwtConfigGroup): ParadoxScriptValueFieldExpression {
			if(expressionString.isEmpty()) return EmptyExpression
			
			var isValid = true
			var isMatched = true
			val wholeRange = TextRange.create(0, expressionString.length)
			val infos = SmartList<ParadoxScriptExpressionInfo>()
			val errors = SmartList<ParadoxScriptExpressionError>()
			val textRanges = SmartList<TextRange>()
			//这里需要找到DS的结束位置
			val expressionStringToCheckLength = when {
				expressionString.startsWith("value:") -> expressionString.indexOf("|", 6)
				expressionString.contains(".value:") -> expressionString.indexOf("|", 7)
				else -> -1
			}.let { if(it != -1) it else expressionString.length }
			val expressionStringToCheck = expressionString.substring(0, expressionStringToCheckLength)
			var startIndex: Int
			var endIndex: Int = -1
			while(endIndex < expressionStringToCheck.length) {
				startIndex = endIndex + 1
				endIndex = expressionStringToCheck.indexOf('.', startIndex).let { if(it != -1) it else expressionString.length }
				textRanges.add(TextRange.create(startIndex, endIndex))
			}
			//加入"."的expressionInfo
			for(pipeIndex in expressionStringToCheck.indicesOf('.')) {
				infos.add(ParadoxScriptOperatorExpressionInfo(".", TextRange.create(pipeIndex, pipeIndex + 1)))
			}
			for((index, textRange) in textRanges.withIndex()) {
				val text = textRange.substring(expressionString)
				val textToCheck = text
					.let { if(it.startsWith("value:")) it.substringBefore('|') else it }
				if(textToCheck.isParameterAwareExpression()) {
					//如果子表达式可能带有参数，则跳过继续解析
					continue
				}
				if(textToCheck.isEmpty() || !textToCheck.isValidSubExpression()) {
					//如果表达式格式不正确
					isValid = false
					isMatched = false
					val error = ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.valueField.malformed", expressionString), wholeRange)
					errors.clear()
					errors.add(error)
					break
				}
				//可以解析，或者不是最后一个，继续
				if(index != textRanges.lastIndex) {
					val resolved = CwtConfigHandler.resolveSystemScope(textToCheck, configGroup)
						?: CwtConfigHandler.resolveScope(textToCheck, configGroup)
					val info = ParadoxScriptScopeExpressionInfo(textToCheck, textRange, resolved, emptySet())
					infos.add(info)
					if(resolved == null) isMatched = false
				} else {
					val resolved = CwtConfigHandler.resolveValueLinkValue(textToCheck, configGroup)
					//可以解析，继续
					if(resolved != null) {
						val info = ParadoxScriptValueLinkValueExpressionInfo(textToCheck, textRange, resolved)
						infos.add(info)
						continue
					}
					
					val matchedLinkConfigs = configGroup.linksAsValueWithPrefixSorted
						.filter { it.prefix != null && it.dataSource != null && textToCheck.startsWith(it.prefix) }
					if(matchedLinkConfigs.isNotEmpty()) {
						//匹配某一前缀
						val prefix = matchedLinkConfigs.first().prefix!!
						val prefixRange = TextRange.create(textRange.startOffset, textRange.startOffset + prefix.length)
						
						val directlyResolvedList = matchedLinkConfigs.mapNotNull { it.pointer.element }
						val prefixInfo = ParadoxScriptValueLinkPrefixExpressionInfo(prefix, prefixRange, directlyResolvedList)
						infos.add(prefixInfo)
						val textWithoutPrefix = textToCheck.drop(prefix.length)
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
										break
									}
									textRangesSv.add(TextRange.create(startIndexSv, endIndexSv))
								}
								var flag = false
								for(textRangeSv in textRangesSv) {
									flag = !flag
									if(textRangeSv.isEmpty) {
										//连续的管道符 
										continuousPipe = true
										continue
									}
									val textSv = textRangeSv.substring(expressionString)
									if(textSv.isParameterAwareExpression()) {
										//如果子表达式可能带有参数，则跳过继续解析
										continue
									}
									val info = if(flag) {
										ParadoxScriptSvParameterExpressionInfo(textSv, textRangeSv, svName, configGroup)
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
						//这里认为如果声明了数据源，则必须要有前缀
						//无法解析的value，或者要求有前缀
						val info = ParadoxScriptValueLinkValueExpressionInfo(textToCheck, textRange, null, emptySet())
						infos.add(info)
						isMatched = false //不匹配
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
		//要求重新匹配
		result.restartCompletionOnAnyPrefixChange()
		
		val offsetInParent = offsetInParent
		//这里需要找到DS的结束位置
		val expressionStringToCheckLength = when {
			expressionString.startsWith("value:") -> expressionString.substringAfter("value:").indexOf("|")
			expressionString.contains(".value:") -> expressionString.substringAfter(".value:").indexOf("|")
			else -> -1
		}.let { if(it != -1) it else expressionString.length }
		val expressionStringToCheck = expressionString.substring(0, expressionStringToCheckLength)
		val start = expressionStringToCheck.lastIndexOf('.').let { if(it == -1) 0 else it + 1 }
		val end = expressionStringToCheck.indexOf('.', offsetInParent).let { if(it == -1) expressionString.length else it }
		val isLast = end == expressionString.length
		
		val text = expressionString.substring(start, end)
		val pipeIndex = if(isLast && text.startsWith("value:")) text.indexOf('|') else -1
		val textToCheck = if(pipeIndex == -1) text else text.substring(0, pipeIndex)
		//兼容带参数的SV表达式
		if(pipeIndex != -1 && pipeIndex < offsetInParent - start) {
			//在必要时提示参数名
			val svName = textToCheck.removePrefix("value:")
			val offsetIndex = offsetInParent - start
			var paramNameKeyword: String? = null
			val paramNames = mutableSetOf<String>()
			var startIndexSv: Int
			var endIndexSv: Int = pipeIndex
			var flag = false
			while(endIndexSv != text.length) {
				flag = !flag
				startIndexSv = endIndexSv + 1
				endIndexSv = text.indexOf('|', startIndexSv).let { if(it != -1) it else text.length }
				if(flag) {
					if(offsetIndex in startIndexSv..endIndexSv) {
						paramNameKeyword = text.substring(startIndexSv, offsetIndex)
					}
					val paramName = text.substring(startIndexSv, endIndexSv)
					if(paramName.isNotEmpty()) paramNames.add(paramName)
				}
			}
			if(paramNameKeyword != null) {
				put(PlsCompletionKeys.keywordKey, paramNameKeyword)
				//开始提示
				completeParametersForScriptValueExpression(svName, paramNames, this, result)
				put(PlsCompletionKeys.keywordKey, expressionString)
			}
			return
		}
		
		val keywordToUse = expressionString.substring(start, offsetInParent)
		put(PlsCompletionKeys.keywordKey, keywordToUse)
		
		val prevScope = if(start == 0) null else expressionString.substring(0, start - 1).substringAfterLast('.')
		if(prevScope != null) put(PlsCompletionKeys.prevScopeKey, prevScope)
		
		if(isLast) {
			completeScope(this, result)
			completeValueLinkValue(this, result)
		} else {
			completeScope(this, result)
		}
		
		put(PlsCompletionKeys.keywordKey, expressionString)
		put(PlsCompletionKeys.prevScopeKey, null)
	}
}
