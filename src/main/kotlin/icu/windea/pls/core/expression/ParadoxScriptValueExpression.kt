package icu.windea.pls.core.expression

import com.intellij.openapi.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.expression.nodes.*

/**
 * 封装值表达式。
 * 
 * 语法：
 * 
 * ```bnf
 * script_value_expression ::= script_value ("|" (param_name "|" param_value "|")+)?
 * script_value ::= TOKEN //matching config expression "<script_value>"
 * param_name ::= TOKEN //parameter name, no surrounding "$"
 * param_value ::= TOKEN //boolean, int, float or string
 * ```
 * 
 * 示例：
 * 
 * ```
 * some_sv
 * some_sv|PARAM|VALUE|
 * ```
 */
interface ParadoxScriptValueExpression : ParadoxComplexExpression {
	val scriptValueNode: ParadoxScriptValueExpressionNode
	val parameterNodes: List<ParadoxScriptValueParameterExpressionNode>
	
	//TODO
	
	companion object Resolver
}

fun ParadoxScriptValueExpression.Resolver.resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, isKey: Boolean? = null) : ParadoxScriptValueExpression {
	TODO()
}
