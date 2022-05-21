package icu.windea.pls.core

import icu.windea.pls.*

/**
 * 值的类型。
 * @see icu.windea.pls.script.psi.ParadoxScriptExpression
 */
enum class ParadoxValueType(
	override val id: String,
	override val text: String
) : IdAware, TextAware {
	UnknownType("unknown", "(unknown)"),
	BooleanType("boolean", "boolean"),
	IntType("int", "int"),
	FloatType("float", "float"),
	NumberType("number", "number"), //int | float
	StringType("string", "string"),
	ColorType("color", "color"),
	BlockType("block", "block"),
	
	InlineMathType("inline_math", "inline math");
	//TODO 补充高级类型
	
	override fun toString(): String {
		return text
	}
}