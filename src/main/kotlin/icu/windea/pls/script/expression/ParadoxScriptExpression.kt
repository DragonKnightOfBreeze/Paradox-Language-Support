package icu.windea.pls.script.expression

import com.intellij.openapi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import java.util.concurrent.*

sealed class ParadoxScriptExpression(
	expressionString: String,
	val infos: List<ParadoxScriptExpressionInfo>,
	val configGroup: CwtConfigGroup
) : AbstractExpression(expressionString)

abstract class ParadoxScriptExpressionResolver<T: ParadoxScriptExpression>{
	protected val cache:MutableMap<String, T> = ConcurrentHashMap()
	
	fun resolve(expression: String, configGroup: CwtConfigGroup): T {
		return cache.getOrPut(configGroup.gameType.id + " " + expression){ 
			doResolve(expression, configGroup)
		}
	}
	
	protected abstract fun doResolve(expressionString: String, configGroup: CwtConfigGroup):T
}

sealed class ParadoxScriptScopeFieldExpression(
	expressionString: String,
	infos: List<ParadoxScriptExpressionInfo>,
	configGroup: CwtConfigGroup
) : ParadoxScriptExpression(expressionString, infos, configGroup)

class ParadoxScriptScopeLinkExpression(
	expressionString: String,
	infos: List<ParadoxScriptExpressionInfo>,
	configGroup: CwtConfigGroup
) : ParadoxScriptScopeFieldExpression(expressionString, infos, configGroup) {
	companion object Resolver: ParadoxScriptExpressionResolver<ParadoxScriptScopeLinkExpression>() {
		override fun doResolve(expressionString: String, configGroup: CwtConfigGroup): ParadoxScriptScopeLinkExpression {
			val wholeRange = TextRange.create(0, expressionString.length)
			val dotIndices = expressionString.indicesOf('.')
			if(dotIndices.isNotEmpty()) {
				val infos = mutableListOf<ParadoxScriptScopeExpressionInfo>()
				for(i in 0..dotIndices.size) { 
					val start = wholeRange.startOffset
					val end = wholeRange.endOffset
					val textRange = when {
						i == 0 -> TextRange.create(start, start + dotIndices[i])
						i == dotIndices.size -> TextRange.create(start + dotIndices[i - 1] + 1, end)
						else -> TextRange.create(start + dotIndices[i - 1] + 1, start + dotIndices[i])
					}
					val text = textRange.substring(expressionString)
					if(text.isEmpty() || !text.all { it.isExactLetter() || it.isExactDigit() || it == '_'}) continue //跳过不合法的部分
					val resolved = CwtConfigHandler.resolveScope(text, configGroup)
					val info = ParadoxScriptScopeExpressionInfo(text, textRange, resolved)
					infos.add(info)
				}
				return ParadoxScriptScopeLinkExpression(expressionString, infos, configGroup)
			} else {
				val resolved = CwtConfigHandler.resolveScope(expressionString, configGroup)
				val info = ParadoxScriptScopeExpressionInfo(expressionString, wholeRange, resolved)
				return ParadoxScriptScopeLinkExpression(expressionString, listOf(info), configGroup)
			}
		}
	}
}

sealed class ParadoxScriptScopeValueExpression(
	expressionString: String,
	infos: List<ParadoxScriptExpressionInfo>,
	configGroup: CwtConfigGroup
) : ParadoxScriptScopeFieldExpression(expressionString, infos, configGroup)
