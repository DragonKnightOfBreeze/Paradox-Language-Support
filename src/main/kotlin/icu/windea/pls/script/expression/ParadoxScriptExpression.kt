package icu.windea.pls.script.expression

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.CwtConfigHandler.completeScriptExpression
import icu.windea.pls.config.cwt.CwtConfigHandler.contextElement
import icu.windea.pls.config.cwt.CwtConfigHandler.getScopeFieldPrefixVariants
import icu.windea.pls.config.cwt.CwtConfigHandler.getScopeVariants
import icu.windea.pls.config.cwt.CwtConfigHandler.offsetInParent
import icu.windea.pls.script.psi.*
import java.util.concurrent.*

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
	protected val cache: MutableMap<String, T> by lazy { ConcurrentHashMap() }
	
	open fun resolve(expression: String, configGroup: CwtConfigGroup): T {
		return cache.getOrPut(configGroup.gameType.id + " " + expression) {
			doResolve(expression, configGroup)
		}
	}
	
	protected abstract fun doResolve(expressionString: String, configGroup: CwtConfigGroup): T
}

/**
 * 作用域表达式。用于表示一个作用域。
 *
 * 一些例子：`root` `root.owner` `event_target:xxx` `root.event_target:xxx`
 */
class ParadoxScriptScopeExpression(
	expressionString: String,
	configGroup: CwtConfigGroup,
	infos: List<ParadoxScriptExpressionInfo> = emptyList(),
	errors: List<ParadoxScriptExpressionError> = emptyList()
) : ParadoxScriptExpression(expressionString, configGroup, infos, errors) {
	//TODO 参考CWT规则，作用域本身的别名（如：`root`）也可以包含点号
	//NOTE 参考CWT规则，可能没有前缀，前缀后的文本需要机遇data_source对应的表达式（如：`value_set[event_target]`）进行解析
	
	companion object Resolver : ParadoxScriptExpressionResolver<ParadoxScriptScopeExpression>() {
		val EmptyExpression by lazy { ParadoxScriptScopeExpression("", MockCwtConfigGroup).apply { empty = true } }
		
		override fun doResolve(expressionString: String, configGroup: CwtConfigGroup): ParadoxScriptScopeExpression {
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
					val error = ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.scope.malformed", expressionString), wholeRange)
					errors.add(0, error)
					break
				}
				//尝试解析为scope
				val resolved = CwtConfigHandler.resolveScope(text, configGroup)
				//可以解析，继续
				if(resolved != null) {
					val info = ParadoxScriptScopeExpressionInfo(text, textRange, resolved)
					infos.add(info)
					isMatched = true //可解析 -> 可匹配 
					continue
				}
				//尝试将最后一段文本解析为scopeField
				if(index == textRanges.lastIndex) {
					val matchedLinkConfigs = configGroup.linksAsScope.values
						.filter { it.prefix != null && expressionString.startsWith(it.prefix) && it.dataSource != null }
						.sortedByDescending { it.dataSource!!.priority } //需要按照优先级重新排序
					if(matchedLinkConfigs.isNotEmpty()) {
						//匹配某一前缀
						val prefix = matchedLinkConfigs.first().prefix!!
						val prefixRange = TextRange.create(textRange.startOffset, textRange.startOffset + prefix.length)
						val directlyResolvedList = matchedLinkConfigs.mapNotNull { it.pointer.element }
						val prefixInfo = ParadoxScriptScopeFieldPrefixExpressionInfo(prefix, prefixRange, directlyResolvedList, matchedLinkConfigs)
						infos.add(prefixInfo)
						val dataSourceText = expressionString.drop(prefix.length)
						if(dataSourceText.isEmpty()) {
							//缺少dataSource
							val dataSourcesText = matchedLinkConfigs.joinToString { "'${it.dataSource}'" }
							val error = ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.scope.missingDs", dataSourcesText), textRange)
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
						val linkConfigs = configGroup.linksAsScope.values.filter { it.prefix == null && it.dataSource != null }
						if(linkConfigs.isEmpty()) {
							//无法解析的scope，或者要求有前缀
							val possiblePrefixList = configGroup.linksAsScope.values.mapNotNull { it.prefix }
							val info = ParadoxScriptScopeExpressionInfo(text, textRange, null, possiblePrefixList)
							infos.add(info)
							isMatched = true //也认为匹配，实际上这里无法判断
						} else {
							val dataSourceInfo = ParadoxScriptScopeFieldDataSourceExpressionInfo(expressionString, textRange, linkConfigs)
							infos.add(dataSourceInfo)
							isMatched = true //也认为匹配，实际上这里无法判断
						}
					}
				}
			}
			
			return ParadoxScriptScopeExpression(expressionString, configGroup, infos, errors)
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
		val start = expressionString.lastIndexOf('.').let { if(it == -1) 0 else it + 1 }
		val end = expressionString.indexOf('.', offsetInParent).let { if(it == -1) length else it }
		val isLast = end == length
		val prefixInfo = infos.find { it is ParadoxScriptScopeFieldPrefixExpressionInfo }
		val prefix = prefixInfo?.text
		val keywordToUse = expressionString.substring(start + (prefix?.length ?: 0), end)
		val resultToUse = result.withPrefixMatcher(keywordToUse)
		//加上scope
		resultToUse.addAllElements(getScopeVariants())
		if(isLast) {
			if(prefix == null) {
				//加上scopeFieldPrefix
				resultToUse.addAllElements(getScopeFieldPrefixVariants())
			} else {
				//加上scopeFieldDataSource
				val linkConfigs = prefixInfo.castOrNull<ParadoxScriptScopeFieldPrefixExpressionInfo>()?.linkConfigs
				val contextElement = contextElement
				if(!linkConfigs.isNullOrEmpty() && (contextElement is ParadoxScriptExpressionElement)) {
					for(linkConfig in linkConfigs) {
						completeScriptExpression(contextElement, linkConfig.dataSource!!, linkConfig.config, result, null) //TODO 传递scope
					}
				}
			}
		}
		
		resultToUse.restartCompletionOnAnyPrefixChange() //要求重新匹配
	}
}
