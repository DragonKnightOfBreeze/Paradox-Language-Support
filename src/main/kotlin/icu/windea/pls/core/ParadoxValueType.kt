package icu.windea.pls.core

import icu.windea.pls.*

/**
 * 值的类型。
 * @see icu.windea.pls.script.psi.ParadoxScriptExpression
 */
enum class ParadoxValueType(
	override val text: String,
	val advanceType: Boolean = false
) : TextAware {
	UnknownType("(unknown)"),
	BooleanType("boolean"),
	IntType("int"),
	FloatType("float"),
	StringType("string"),
	ColorType("color"),
	BlockType("block"),
	CodeType("code", true);
	//TODO 补充高级类型
	
	override fun toString(): String {
		return text
	}
}