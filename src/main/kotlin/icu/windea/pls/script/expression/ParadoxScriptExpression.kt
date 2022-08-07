package icu.windea.pls.script.expression

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.CwtConfigHandler.completeScope
import icu.windea.pls.config.cwt.CwtConfigHandler.completeScopeFieldPrefixOrDataSource
import icu.windea.pls.config.cwt.CwtConfigHandler.completeValueFieldPrefixOrDataSource
import icu.windea.pls.config.cwt.CwtConfigHandler.completeValueOfValueField
import icu.windea.pls.core.codeInsight.completion.*

/**
 * 脚本表达式。
 *
 * 一些例子：`root.owner` `event_target:xxx` `value:xxx|xxx|xxx|`
 *
 * @property errors 通常不会包括无法解析的异常。
 */
sealed class ParadoxScriptExpression(
	expressionString: String,
	val configGroup: CwtConfigGroup,
	val infos: List<ParadoxScriptExpressionInfo>,
	val errors: List<ParadoxScriptExpressionError>
) : AbstractExpression(expressionString) {
	protected var empty = false
	protected var valid = false
	protected var matched = false
	
	fun isEmpty() = empty
	
	fun isValid() = empty || valid
	
	fun isMatched() = matched
	
	fun complete(result: CompletionResultSet, context: ProcessingContext) {
		val offsetInParent = context.offsetInParent
		if(offsetInParent < 0 || offsetInParent > expressionString.length) return
		
		context.doComplete(result)
	}
	
	protected abstract fun ProcessingContext.doComplete(result: CompletionResultSet)
}

abstract class ParadoxScriptExpressionResolver<T : ParadoxScriptExpression> {
	abstract fun resolve(expressionString: String, configGroup: CwtConfigGroup): T
}

/**
 * 作用域字段表达式。
 *
 * 一些例子：`root` `root.owner` `event_target:xxx` `root.event_target:xxx`
 */
class ParadoxScriptScopeFieldExpression(
	expressionString: String,
	configGroup: CwtConfigGroup,
	infos: List<ParadoxScriptExpressionInfo> = emptyList(),
	errors: List<ParadoxScriptExpressionError> = emptyList()
) : ParadoxScriptExpression(expressionString, configGroup, infos, errors) {
	//TODO 参考CWT规则，作用域本身的别名（如：`root`）也可以包含点号
	//NOTE 参考CWT规则，可能没有前缀，前缀后的文本需要机遇data_source对应的表达式（如：`value_set[event_target]`）进行解析
	
	companion object Resolver : ParadoxScriptExpressionResolver<ParadoxScriptScopeFieldExpression>() {
		val EmptyExpression by lazy { ParadoxScriptScopeFieldExpression("", MockCwtConfigGroup).apply { empty = true } }
		
		override fun resolve(expressionString: String, configGroup: CwtConfigGroup): ParadoxScriptScopeFieldExpression {
			if(expressionString.isEmpty()) return EmptyExpression
			val length = expressionString.length
			
			var isValid = true
			var isMatched = false
			val wholeRange = TextRange.create(0, length)
			val infos = SmartList<ParadoxScriptExpressionInfo>()
			val errors = SmartList<ParadoxScriptExpressionError>()
			val textRanges = SmartList<TextRange>()
			var startIndex: Int
			var endIndex: Int = -1
			while(endIndex != length) {
				startIndex = endIndex + 1
				endIndex = expressionString.indexOf('.', startIndex).let { if(it != -1) it else length }
				textRanges.add(TextRange.create(startIndex, endIndex))
			}
			for(textRange in textRanges) {
				val text = textRange.substring(expressionString)
				if(text.isEmpty() || !text.isValidSubExpression()) {
					//如果表达式格式不正确
					isValid = false
					val error = ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.scopeField.malformed", expressionString), wholeRange)
					errors.clear()
					errors.add(error)
					break
				}
				val resolved = CwtConfigHandler.resolveScope(text, configGroup)
				//可以解析，继续
				if(resolved != null) {
					val info = ParadoxScriptScopeExpressionInfo(text, textRange, resolved, configGroup.linksAsScopePrefixes)
					infos.add(info)
					isMatched = true //可解析 -> 可匹配 
					continue
				}
				val matchedLinkConfigs = configGroup.linksAsScope.values
					.filter { it.prefix != null && it.dataSource != null && text.startsWith(it.prefix) }
				if(matchedLinkConfigs.isNotEmpty()) {
					//匹配某一前缀
					val prefix = matchedLinkConfigs.first().prefix!!
					val prefixRange = TextRange.create(textRange.startOffset, textRange.startOffset + prefix.length)
					val directlyResolvedList = matchedLinkConfigs.mapNotNull { it.pointer.element }
					val prefixInfo = ParadoxScriptScopeFieldPrefixExpressionInfo(prefix, prefixRange, directlyResolvedList, matchedLinkConfigs)
					infos.add(prefixInfo)
					val dataSourceText = text.drop(prefix.length)
					if(dataSourceText.isEmpty()) {
						//缺少dataSource
						val dataSourcesText = matchedLinkConfigs.joinToString { "'${it.dataSource}'" }
						val error = ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.scopeField.missingDs", dataSourcesText), textRange)
						errors.add(error)
						isMatched = true //认为匹配
					} else {
						val dataSourceRange = TextRange.create(textRange.startOffset + prefix.length, textRange.endOffset)
						val dataSourceInfo = ParadoxScriptScopeFieldDataSourceExpressionInfo(dataSourceText, dataSourceRange, matchedLinkConfigs)
						infos.add(dataSourceInfo)
						isMatched = true //认为匹配
					}
				} else {
					//没有前缀
					val linkConfigsNoPrefix = configGroup.linksAsScopeNoPrefix.values
					if(linkConfigsNoPrefix.isEmpty()) {
						//无法解析的scope，或者要求有前缀
						val info = ParadoxScriptScopeExpressionInfo(text, textRange, null, configGroup.linksAsScopePrefixes)
						infos.add(info)
						isMatched = true //也认为匹配，实际上这里无法判断
					} else {
						val dataSourceInfo = ParadoxScriptScopeFieldDataSourceExpressionInfo(text, textRange, linkConfigsNoPrefix)
						infos.add(dataSourceInfo)
						isMatched = true //也认为匹配，实际上这里无法判断
					}
				}
			}
			
			return ParadoxScriptScopeFieldExpression(expressionString, configGroup, infos, errors)
				.apply { valid = isValid }
				.apply { matched = isMatched }
		}
		
		private fun String.isValidSubExpression(): Boolean {
			return all { it == '_' || it == ':' || it.isExactLetter() || it.isExactDigit() }
		}
	}
	
	override fun ProcessingContext.doComplete(result: CompletionResultSet) {
		//基于点号进行代码提示，因此允许最终会导致表达式不合法的情况
		val length = expressionString.length
		val offsetInParent = offsetInParent
		val start = expressionString.take(offsetInParent).lastIndexOf('.').let { if(it == -1) 0 else it + 1 }
		val end = expressionString.indexOf('.', offsetInParent).let { if(it == -1) length else it }
		val isLast = end == length
		
		val prevScope = if(start == 0) null else expressionString.substring(0, start - 1).substringAfterLast('.')
		if(prevScope != null) put(PlsCompletionKeys.prevScopeKey, prevScope)
		
		val keyword = keyword
		val keywordToUse = expressionString.substring(start, end)
		put(PlsCompletionKeys.keywordKey, keywordToUse)
		
		if(isLast) {
			completeScope(result)
			completeScopeFieldPrefixOrDataSource(result)
		} else {
			completeScope(result)
		}
		
		put(PlsCompletionKeys.keywordKey, keyword)
		put(PlsCompletionKeys.prevScopeKey, null)
		
		result.restartCompletionOnAnyPrefixChange() //要求重新匹配
	}
}

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
		
		override fun resolve(expressionString: String, configGroup: CwtConfigGroup): ParadoxScriptValueFieldExpression {
			if(expressionString.isEmpty()) return EmptyExpression
			val length = expressionString.length
			
			var isValid = true
			var isMatched = false
			val wholeRange = TextRange.create(0, length)
			val infos = SmartList<ParadoxScriptExpressionInfo>()
			val errors = SmartList<ParadoxScriptExpressionError>()
			val textRanges = SmartList<TextRange>()
			var startIndex: Int
			var endIndex: Int = -1
			while(endIndex != length) {
				startIndex = endIndex + 1
				endIndex = expressionString.indexOf('.', startIndex).let { if(it != -1) it else length }
				textRanges.add(TextRange.create(startIndex, endIndex))
			}
			for((index, textRange) in textRanges.withIndex()) {
				val text = textRange.substring(expressionString)
				if(text.isEmpty() || !text.isValidSubExpression()) {
					//如果表达式格式不正确
					isValid = false
					val error = ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.valueField.malformed", expressionString), wholeRange)
					errors.clear()
					errors.add(error)
					break
				}
				//可以解析，或者不是最后一个，继续
				if(index != textRanges.lastIndex){
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
						val prefixInfo = ParadoxScriptValueFieldPrefixExpressionInfo(prefix, prefixRange, directlyResolvedList, matchedLinkConfigs)
						infos.add(prefixInfo)
						val dataSourceText = text.drop(prefix.length)
						if(dataSourceText.isEmpty()) {
							//缺少dataSource
							val dataSourcesText = matchedLinkConfigs.joinToString { "'${it.dataSource}'" }
							val error = ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.valueField.missingDs", dataSourcesText), textRange)
							errors.add(error)
							isMatched = true //认为匹配
						} else {
							val dataSourceRange = TextRange.create(textRange.startOffset + prefix.length, textRange.endOffset)
							val dataSourceInfo = ParadoxScriptValueFieldDataSourceExpressionInfo(dataSourceText, dataSourceRange, matchedLinkConfigs)
							infos.add(dataSourceInfo)
							isMatched = true //认为匹配
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
		val length = expressionString.length
		val offsetInParent = offsetInParent
		val start = expressionString.lastIndexOf('.').let { if(it == -1) 0 else it + 1 }
		val end = expressionString.indexOf('.', offsetInParent).let { if(it == -1) length else it }
		val isLast = end == length
		
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