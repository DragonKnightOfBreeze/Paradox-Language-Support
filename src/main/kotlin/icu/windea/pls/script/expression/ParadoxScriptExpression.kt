package icu.windea.pls.script.expression

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.expression.*

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

