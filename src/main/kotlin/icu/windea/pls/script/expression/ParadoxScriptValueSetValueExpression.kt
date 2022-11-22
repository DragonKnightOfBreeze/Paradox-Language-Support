package icu.windea.pls.script.expression

import com.intellij.codeInsight.completion.*
import com.intellij.openapi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*

/**
 * 值集值值的表达式。
 * 
 * 一些例子：`some_flag` `some_flag@root` `some_flag@root.owner`
 */
class ParadoxScriptValueSetValueExpression(
	expressionString: String,
	configGroup: CwtConfigGroup,
	infos: List<ParadoxScriptExpressionInfo> = emptyList(),
	errors: List<ParadoxScriptExpressionError> = emptyList()
) : ParadoxScriptComplexExpression(expressionString, configGroup, infos, errors) {
	companion object Resolver : ParadoxScriptComplexExpressionResolver<ParadoxScriptValueSetValueExpression>() {
		val EmptyExpression by lazy { ParadoxScriptValueSetValueExpression("", MockCwtConfigGroup).apply { empty = true } }
		
		override fun doResolve(expressionString: String, configGroup: CwtConfigGroup): ParadoxScriptValueSetValueExpression {
			if(expressionString.isEmpty()) return EmptyExpression
			
			var isValid = true
			val wholeRange = TextRange.create(0, expressionString.length)
			val separatorIndex = expressionString.indexOf('@')
			val infos =  SmartList<ParadoxScriptExpressionInfo>()
			val errors = SmartList<ParadoxScriptExpressionError>()
			if(separatorIndex == -1) {
				infos.add(ParadoxScriptValueSetValueExpressionInfo(expressionString, wholeRange))
			} else {
				infos.add(ParadoxScriptValueSetValueExpressionInfo(expressionString.substring(0,separatorIndex), TextRange.create(0, separatorIndex)))
				if(separatorIndex == expressionString.length - 1 || expressionString.indexOf('@', separatorIndex + 1) != -1) {
					//以@结束或者多个@ -> 不合法
					isValid = false
					val error = ParadoxScriptExpressionError(PlsBundle.message("script.inspection.expression.vsv.malformed", expressionString), wholeRange)
					errors.clear()
					errors.add(error)
				}
				//TODO 解析'@'后面的作用域信息
			}
			return ParadoxScriptValueSetValueExpression(expressionString, configGroup, infos, errors)
				.apply { valid = isValid }
		}
	}
	
	override fun ProcessingContext.doComplete(result: CompletionResultSet) {
		
	}
}
