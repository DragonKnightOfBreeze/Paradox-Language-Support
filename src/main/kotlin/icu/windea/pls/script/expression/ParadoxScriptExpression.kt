package icu.windea.pls.script.expression

import com.intellij.codeInspection.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import java.util.concurrent.*

sealed class ParadoxScriptExpression(
	expressionString: String,
	val configGroup: CwtConfigGroup,
	val infos: List<ParadoxScriptExpressionInfo>,
	val errors: List<ParadoxScriptExpressionError>
) : AbstractExpression(expressionString) {
	//var mayNotMatch: Boolean = false
	//
	//fun mayNotMatch() = apply { mayNotMatch = true }
}

abstract class ParadoxScriptExpressionResolver<T : ParadoxScriptExpression> {
	protected val cache: MutableMap<String, T?> by lazy { ConcurrentHashMap() }
	
	open fun resolve(expression: String, configGroup: CwtConfigGroup): T? {
		return cache.getOrPut(configGroup.gameType.id + " " + expression) {
			doResolve(expression, configGroup)
		}
	}
	
	protected abstract fun doResolve(expressionString: String, configGroup: CwtConfigGroup): T?
}

sealed class ParadoxScriptScopeExpression(
	expressionString: String,
	configGroup: CwtConfigGroup,
	infos: List<ParadoxScriptExpressionInfo> = emptyList(),
	errors: List<ParadoxScriptExpressionError> = emptyList()
) : ParadoxScriptExpression(expressionString, configGroup, infos, errors) {
	companion object Resolver : ParadoxScriptExpressionResolver<ParadoxScriptScopeExpression>() {
		override fun doResolve(expressionString: String, configGroup: CwtConfigGroup): ParadoxScriptScopeExpression? {
			return ParadoxScriptScopeLinkExpression.resolve(expressionString, configGroup)
				?: ParadoxScriptScopeFieldExpression.resolve(expressionString, configGroup)
		}
	}
}

class ParadoxScriptScopeLinkExpression(
	expressionString: String,
	configGroup: CwtConfigGroup,
	infos: List<ParadoxScriptExpressionInfo> = emptyList(),
	errors: List<ParadoxScriptExpressionError> = emptyList()
) : ParadoxScriptScopeExpression(expressionString, configGroup, infos, errors) {
	companion object Resolver : ParadoxScriptExpressionResolver<ParadoxScriptScopeLinkExpression>() {
		override fun doResolve(expressionString: String, configGroup: CwtConfigGroup): ParadoxScriptScopeLinkExpression? {
			if(expressionString.isEmpty() || expressionString.contains(':')) return null
			
			val wholeRange = TextRange.create(0, expressionString.length)
			val dotIndices = expressionString.indicesOf('.')
			if(dotIndices.isNotEmpty()) {
				var isMalformed = false
				val infos = SmartList<ParadoxScriptScopeExpressionInfo>()
				val errors = SmartList<ParadoxScriptExpressionError>()
				for(i in 0..dotIndices.size) {
					val start = wholeRange.startOffset
					val end = wholeRange.endOffset
					val textRange = when {
						i == 0 -> TextRange.create(start, start + dotIndices[i])
						i == dotIndices.size -> TextRange.create(start + dotIndices[i - 1] + 1, end)
						else -> TextRange.create(start + dotIndices[i - 1] + 1, start + dotIndices[i])
					}
					val text = textRange.substring(expressionString)
					if(text.isEmpty() || !text.isExactSnakeCase()) {
						//跳过不合法的部分
						if(!isMalformed) {
							isMalformed = true
							val error = ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.scopeLink.error.1", expressionString), wholeRange)
							errors.add(0, error)
						}
						continue
					}
					val resolved = CwtConfigHandler.resolveScope(text, configGroup)
					val info = ParadoxScriptScopeExpressionInfo(text, textRange, resolved)
					infos.add(info)
					if(resolved == null) {
						val error = ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.scopeLink.error.2", text), textRange, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
						errors.add(error)
					}
				}
				return ParadoxScriptScopeLinkExpression(expressionString, configGroup, infos, errors)
			} else {
				if(expressionString.isExactSnakeCase()) {
					val resolved = CwtConfigHandler.resolveScope(expressionString, configGroup)
					val info = ParadoxScriptScopeExpressionInfo(expressionString, wholeRange, resolved)
					val errors = if(resolved == null) {
						val error = ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.scopeLink.error.2", expressionString), wholeRange, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
						listOf(error)
					} else emptyList()
					return ParadoxScriptScopeLinkExpression(expressionString, configGroup, listOf(info), errors)
				} else {
					//格式不正确
					val error = ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.scopeLink.error.1", expressionString), wholeRange)
					return ParadoxScriptScopeLinkExpression(expressionString, configGroup, errors = listOf(error))
				}
			}
		}
	}
}

class ParadoxScriptScopeFieldExpression(
	expressionString: String,
	configGroup: CwtConfigGroup,
	infos: List<ParadoxScriptExpressionInfo> = emptyList(),
	errors: List<ParadoxScriptExpressionError> = emptyList()
) : ParadoxScriptScopeExpression(expressionString, configGroup, infos, errors) {
	companion object Resolver : ParadoxScriptExpressionResolver<ParadoxScriptScopeFieldExpression>() {
		override fun doResolve(expressionString: String, configGroup: CwtConfigGroup): ParadoxScriptScopeFieldExpression? {
			if(expressionString.isEmpty()) return null
			
			return null //TODO
		}
	}
}
