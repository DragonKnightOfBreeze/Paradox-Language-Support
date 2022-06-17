package icu.windea.pls.model

/**
 * 脚本文件中keyExpression或valueExpression的类型。
 * @see icu.windea.pls.script.psi.ParadoxScriptPropertyKey
 * @see icu.windea.pls.script.psi.ParadoxScriptString
 */
enum class ParadoxKvExpressionType {
	/** 字面量（也包含任何不会被解析为其他类型的情况） */
	LiteralType, //e.g. prop
	
	/** 参数 */
	ParameterType, //e.g. $PARAM$
	
	/** 字符串模版（嵌入参数的字面量） */
	StringTemplateType, //e.g. civic_$PARAMd$
	
	/** （嵌套的）作用域表达式 */
	ScopeExpression, //e.g. root.owner
	
	/** 作用域值表达式 */
	ScopeValueExpression, //e.g. event_target:xxx
	
	/** 封装值表达式 */
	ScriptValueExpression //e.g. value:xxx
}