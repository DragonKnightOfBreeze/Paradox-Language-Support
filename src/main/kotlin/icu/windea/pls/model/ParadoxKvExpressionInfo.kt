package icu.windea.pls.model

import com.intellij.openapi.util.*
import icu.windea.pls.script.psi.*

/**
 * 脚本文件中keyExpression或valueExpression的信息。
 * @see ParadoxScriptPropertyKey
 * @see ParadoxScriptString
 */
data class ParadoxKvExpressionInfo(
	/** 表达式的类型。 */
	val type: ParadoxKvExpressionType,
	/** （相对于父节点的）整个表达式文本范围。 */
	val wholeRange: TextRange,
	/**
	 * （相对于父节点的）表达式的文本范围列表。
	 * * [ParadoxKvExpressionType.LiteralType]：唯一的[TextRange]表示整个表达式的文本范围。
	 * * [ParadoxKvExpressionType.ParameterType]：唯一的[TextRange]表示参数的文本范围。
	 * * [ParadoxKvExpressionType.StringTemplateType]：各个[TextRange]表示各个参数/字符串片段的文本范围。
	 * * [ParadoxKvExpressionType.ScopeExpression]：各个[TextRange]表示各个作用域连接的文本范围。
	 * * [ParadoxKvExpressionType.ScopeValueExpression]：第一个[TextRange]表示前缀的文本范围，第二个[TextRange]表示值表达式的文本范围。
	 * * [ParadoxKvExpressionType.ScriptValueExpression]：第一个[TextRange]表示前缀的文本范围，第二个[TextRange]表示SV名字的文本范围，其余的[TextRange]轮流表示输入参数名字/输入参数值的文本范围。
	 */
	val ranges: List<TextRange>
)