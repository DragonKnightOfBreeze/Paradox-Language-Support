package icu.windea.pls.core.expression

import java.text.*

/**
 * @see ParadoxDataExpression
 */
enum class ParadoxDataType(
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
	
	companion object {
		fun resolve(expression: String): ParadoxDataType {
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

fun ParadoxDataType.isBooleanType() = this == ParadoxDataType.BooleanType

fun ParadoxDataType.isIntType() = this == ParadoxDataType.UnknownType || this == ParadoxDataType.IntType || this == ParadoxDataType.ParameterType || this == ParadoxDataType.InlineMathType

fun ParadoxDataType.isFloatType() = this == ParadoxDataType.UnknownType || this == ParadoxDataType.IntType || this == ParadoxDataType.FloatType || this == ParadoxDataType.ParameterType || this == ParadoxDataType.InlineMathType

fun ParadoxDataType.isNumberType() = this == ParadoxDataType.UnknownType || this == ParadoxDataType.IntType || this == ParadoxDataType.FloatType || this == ParadoxDataType.ParameterType || this == ParadoxDataType.InlineMathType

fun ParadoxDataType.isStringType() = this == ParadoxDataType.UnknownType || this == ParadoxDataType.StringType || this == ParadoxDataType.ParameterType

fun ParadoxDataType.isStringLikeType() = this == ParadoxDataType.UnknownType || this == ParadoxDataType.StringType || this == ParadoxDataType.ParameterType
	|| this == ParadoxDataType.IntType || this == ParadoxDataType.FloatType

fun ParadoxDataType.isColorType() = this == ParadoxDataType.ColorType

fun ParadoxDataType.isBlockLikeType() = this == ParadoxDataType.BlockType || this == ParadoxDataType.ColorType || this == ParadoxDataType.InlineMathType

fun ParadoxDataType.canBeScriptedVariableValue() = this == ParadoxDataType.BooleanType || this == ParadoxDataType.IntType || this == ParadoxDataType.FloatType || this == ParadoxDataType.StringType
