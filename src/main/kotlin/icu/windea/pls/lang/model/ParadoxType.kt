package icu.windea.pls.lang.model

import java.text.*

enum class ParadoxType(
	val id: String,
	val text: String
) {
	UnknownType("unknown", "(unknown)"),
	BooleanType("boolean", "boolean"),
	IntType("int", "int"),
	FloatType("float", "float"),
	StringType("string", "string"),
	ColorType("color", "color"),
	BlockType("block", "block"),
	ParameterType("parameter", "parameter"),
	InlineMathType("inline_math", "inline math");
	
	override fun toString(): String {
		return text
	}
	
	fun isBooleanType() = this == BooleanType
	
	fun isIntType() = this == UnknownType || this == IntType || this == ParameterType || this == InlineMathType
	
	fun isFloatType() = this == UnknownType || this == IntType || this == FloatType || this == ParameterType || this == InlineMathType
	
	fun isNumberType() = this == UnknownType || this == IntType || this == FloatType || this == ParameterType || this == InlineMathType
	
	fun isStringType() = this == UnknownType || this == StringType || this == ParameterType
	
	fun isStringLikeType() = this == UnknownType || this == StringType || this == ParameterType
		|| this == IntType || this == FloatType
	
	fun isColorType() = this == ColorType
	
	fun isBlockLikeType() = this == BlockType || this == ColorType || this == InlineMathType
	
	fun canBeScriptedVariableValue() = this == BooleanType || this == IntType || this == FloatType || this == StringType
	
	companion object {
		fun resolve(expression: String): ParadoxType {
			return when {
				isBooleanYesNo(expression) -> BooleanType
				isInt(expression) -> IntType
				isFloat(expression) -> FloatType
				else -> StringType
			}
		}
		
		fun isBooleanYesNo(expression: String): Boolean {
			return expression == "yes" || expression == "no"
		}
		
		fun isInt(expression: String): Boolean {
			return expression.toIntOrNull() != null
		}
		
		fun isFloat(expression: String): Boolean {
			return expression.toFloatOrNull() != null
		}
		
		private val isPercentageFieldRegex = """[1-9]?[0-9]+%""".toRegex()
		
		fun isPercentageField(expression: String): Boolean {
			return expression.matches(isPercentageFieldRegex)
		}
		
		private val isColorFieldRegex = """(?:rgb|rgba|hsb|hsv|hsl)[ \t]*\{[\d. \t]*}""".toRegex()
		
		fun isColorField(expression: String): Boolean {
			return expression.matches(isColorFieldRegex)
		}
		
		private val threadLocalDateFormat by lazy { ThreadLocal.withInitial { SimpleDateFormat("yyyy.MM.dd") } }
		
		fun isDateField(expression: String): Boolean {
			return try {
				threadLocalDateFormat.get().parse(expression)
				true
			} catch(e: Exception) {
				false
			}
		}
	}
}
