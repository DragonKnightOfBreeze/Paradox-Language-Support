package icu.windea.pls.script.expression

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.script.exp.*

/**
 * 复杂脚本表达式。 
 *
 * 一些例子：`root.owner` `event_target:xxx` `value:xxx|xxx|xxx|`
 *
 * @property errors 解析时加入的异常。不包括无法解析的异常。
 */
@Deprecated("")
sealed class ParadoxScriptComplexExpression(
	expressionString: String,
	val configGroup: CwtConfigGroup,
	val infos: List<ParadoxScriptExpressionInfo>,
	val errors: List<ParadoxScriptExpressionError>
) : AbstractExpression(expressionString), ParadoxScriptExpression {
	override val quoted: Boolean get() = false //always unquoted
	override val type: ParadoxDataType get() = ParadoxDataType.StringType //always string
	override val isKey: Boolean? get() = null //unspecified
	
	protected var empty = false
	protected var valid = true
	protected var matched = true
	
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

