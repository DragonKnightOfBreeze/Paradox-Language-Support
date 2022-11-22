package icu.windea.pls.script.expression

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.CwtConfigHandler.completeScope
import icu.windea.pls.config.cwt.CwtConfigHandler.completeScopeLinkPrefixOrDataSource
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.script.psi.*

/**
 * 作用域字段表达式。
 *
 * 一些例子：`root` `root.owner` `event_target:xxx` `root.event_target:xxx`
 */
@Deprecated("")
class ParadoxScriptScopeFieldExpression(
	expressionString: String,
	configGroup: CwtConfigGroup,
	infos: List<ParadoxScriptExpressionInfo> = emptyList(),
	errors: List<ParadoxScriptExpressionError> = emptyList()
) : ParadoxScriptComplexExpression(expressionString, configGroup, infos, errors) {
	val prefixInfo = infos.findIsInstance<ParadoxScriptValueLinkPrefixExpressionInfo>()
	val dataSourceInfo = infos.findIsInstance<ParadoxScriptValueFieldDataSourceExpressionInfo>()
	
	//TODO 参考CWT规则，作用域本身的别名（如：`root`）也可以包含点号
	//NOTE 参考CWT规则，可能没有前缀，前缀后的文本需要基于data_source对应的表达式（如：`value_set[event_target]`）进行解析
	
	companion object Resolver : ParadoxScriptComplexExpressionResolver<ParadoxScriptScopeFieldExpression>() {
		val EmptyExpression by lazy { ParadoxScriptScopeFieldExpression("", MockCwtConfigGroup).apply { empty = true } }
		
		override fun doResolve(expressionString: String, configGroup: CwtConfigGroup): ParadoxScriptScopeFieldExpression {
			if(expressionString.isEmpty()) return EmptyExpression
			
			var isValid = true
			var isMatched = true
			val wholeRange = TextRange.create(0, expressionString.length)
			val infos = SmartList<ParadoxScriptExpressionInfo>()
			val errors = SmartList<ParadoxScriptExpressionError>()
			val textRanges = SmartList<TextRange>()
			val expressionStringToCheck = expressionString
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
			for(textRange in textRanges) {
				val text = textRange.substring(expressionString)
				val textToCheck = text
				if(textToCheck.isParameterAwareExpression()) {
					//如果子表达式可能带有参数，则直接结束解析
					break
				}
				if(textToCheck.isEmpty() || !textToCheck.isValidSubExpression()) {
					//如果表达式格式不正确
					isValid = false
					isMatched = false
					val error = ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.scopeField.malformed", expressionString), wholeRange)
					errors.clear()
					errors.add(error)
					break
				}
				val resolved = CwtConfigHandler.resolveSystemScope(textToCheck, configGroup)
					?: CwtConfigHandler.resolveScope(textToCheck, configGroup)
				//可以解析，继续
				if(resolved != null) {
					val info = ParadoxScriptScopeExpressionInfo(textToCheck, textRange, resolved, configGroup.linksAsScopePrefixes)
					infos.add(info)
					continue
				}
				val matchedLinkConfigs = configGroup.linksAsScopeSorted
					.filter { it.prefix != null && it.dataSource != null && textToCheck.startsWith(it.prefix) }
				if(matchedLinkConfigs.isNotEmpty()) {
					//匹配某一前缀
					val prefix = matchedLinkConfigs.first().prefix!!
					val prefixRange = TextRange.create(textRange.startOffset, textRange.startOffset + prefix.length)
					val directlyResolvedList = matchedLinkConfigs.mapNotNull { it.pointer.element }
					val prefixInfo = ParadoxScriptScopeLinkPrefixExpressionInfo(prefix, prefixRange, directlyResolvedList)
					infos.add(prefixInfo)
					val dataSourceText = textToCheck.drop(prefix.length)
					if(dataSourceText.isEmpty()) {
						//缺少dataSource
						val dataSourcesText = matchedLinkConfigs.joinToString { "'${it.dataSource}'" }
						val error = ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.scopeField.missingDs", dataSourcesText), textRange)
						errors.add(error)
					} else {
						val dataSourceRange = TextRange.create(textRange.startOffset + prefix.length, textRange.endOffset)
						val dataSourceInfo = ParadoxScriptScopeFieldDataSourceExpressionInfo(dataSourceText, dataSourceRange, matchedLinkConfigs)
						infos.add(dataSourceInfo)
					}
				} else {
					//没有前缀
					val linkConfigsNoPrefix = configGroup.linksAsScopeNoPrefixSorted
					if(linkConfigsNoPrefix.isEmpty()) {
						//无法解析的scope，或者要求有前缀
						val info = ParadoxScriptScopeExpressionInfo(textToCheck, textRange, null, configGroup.linksAsScopePrefixes)
						infos.add(info)
						isMatched = false //不匹配
					} else {
						val dataSourceInfo = ParadoxScriptScopeFieldDataSourceExpressionInfo(textToCheck, textRange, linkConfigsNoPrefix)
						infos.add(dataSourceInfo)
						//也认为匹配，实际上这里无法判断
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
		//要求重新匹配
		result.restartCompletionOnAnyPrefixChange()
		
		val offsetInParent = offsetInParent
		val expressionStringToCheck = expressionString
		val start = expressionStringToCheck.lastIndexOf('.').let { if(it == -1) 0 else it + 1 }
		val end = expressionStringToCheck.indexOf('.', offsetInParent).let { if(it == -1) expressionString.length else it }
		val isLast = end == expressionString.length
		
		val keywordToUse = expressionString.substring(start, offsetInParent)
		put(PlsCompletionKeys.keywordKey, keywordToUse)
		
		val prevScope = if(start == 0) null else expressionString.substring(0, start - 1).substringAfterLast('.')
		if(prevScope != null) put(PlsCompletionKeys.prevScopeKey, prevScope)
		
		if(isLast) {
			completeScope(this, result)
			completeScopeLinkPrefixOrDataSource(this, result)
		} else {
			completeScope(this, result)
		}
		
		put(PlsCompletionKeys.keywordKey, expressionString)
		put(PlsCompletionKeys.prevScopeKey, null)
	}
}
