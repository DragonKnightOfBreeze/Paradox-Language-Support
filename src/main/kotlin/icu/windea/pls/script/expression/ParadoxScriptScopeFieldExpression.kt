package icu.windea.pls.script.expression

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.CwtConfigHandler.completeScope
import icu.windea.pls.config.cwt.CwtConfigHandler.completeScopeFieldPrefixOrDataSource
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.script.psi.*

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
		
		override fun doResolve(expressionString: String, configGroup: CwtConfigGroup): ParadoxScriptScopeFieldExpression {
			if(expressionString.isEmpty()) return EmptyExpression
			
			var isValid = true
			var isMatched = false
			val wholeRange = TextRange.create(0, expressionString.length)
			val infos = SmartList<ParadoxScriptExpressionInfo>()
			val errors = SmartList<ParadoxScriptExpressionError>()
			val textRanges = SmartList<TextRange>()
			val textToIterate = expressionString
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
			for(textRange in textRanges) {
				val text = textRange.substring(expressionString)
				if(text.isParameterAwareExpression()) {
					//如果子表达式可能带有参数，则直接结束解析
					break
				}
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
					val prefixInfo = ParadoxScriptScopeFieldPrefixExpressionInfo(prefix, prefixRange, directlyResolvedList)
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
		val offsetInParent = offsetInParent
		val start = expressionString.take(offsetInParent).lastIndexOf('.').let { if(it == -1) 0 else it + 1 }
		val end = expressionString.indexOf('.', offsetInParent).let { if(it == -1) expressionString.length else it }
		val isLast = end == expressionString.length
		
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