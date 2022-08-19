package icu.windea.pls.model

import icu.windea.pls.*
import icu.windea.pls.annotations.*
import java.text.*

/**
 * 值的类型。
 * @see icu.windea.pls.script.psi.ParadoxScriptTypedElement
 */
enum class ParadoxValueType(
	val id: String,
	val text: String
) {
	UnknownType("unknown", "(unknown)"),
	BooleanType("boolean", "boolean"),
	IntType("int", "int"),
	FloatType("float", "float"),
	NumberType("number", "number"), //int | float
	StringType("string", "string"),
	ColorType("color", "color"),
	BlockType("block", "block"),
	
	ParameterType("parameter", "parameter"),
	InlineMathType("inline_math", "inline math");
	
	fun matchesBooleanType() = this == BooleanType
	fun matchesIntType() = this == UnknownType || this == IntType || this == ParameterType || this == InlineMathType
	fun matchesFloatType() = this == UnknownType || this == IntType || this == FloatType || this == ParameterType || this == InlineMathType
	fun matchesStringType() = this == UnknownType || this == StringType || this == ParameterType
	fun matchesColorType() = this == ColorType
	
	fun canBeScriptedVariableValue() = this == BooleanType || this == IntType || this == FloatType || this == NumberType
	
	override fun toString(): String {
		return text
	}
	
	companion object {
		@InferMethod
		@JvmStatic
		fun infer(inputValue: String): ParadoxValueType {
			return when {
				isBooleanYesNo(inputValue) -> BooleanType
				isInt(inputValue) -> IntType
				isFloat(inputValue) -> FloatType
				else -> StringType
			}
		}
		
		fun isBooleanYesNo(string: String): Boolean {
			return string == "yes" || string == "no"
		}
		
		fun isInt(s: String): Boolean {
			var isFirstChar = true
			val chars = s.toCharArray()
			for(char in chars) {
				if(char.isExactDigit()) continue
				if(isFirstChar) {
					isFirstChar = false
					if(char == '+' || char == '-') continue
				}
				return false
			}
			return true
		}
		
		fun isFloat(string: String): Boolean {
			var isFirstChar = true
			var missingDot = true
			val chars = string.toCharArray()
			for(char in chars) {
				if(char.isExactDigit()) continue
				if(isFirstChar) {
					isFirstChar = false
					if(char == '+' || char == '-') continue
				}
				if(missingDot) {
					if(char == '.') {
						missingDot = false
						continue
					}
				}
				return false
			}
			return true
		}
		
		fun isPercentageField(string: String): Boolean {
			val chars = string.toCharArray()
			for(i in string.indices) {
				val char = chars[i]
				if(i == string.lastIndex) {
					if(char != '%') return false
				} else {
					if(!char.isDigit()) return false
				}
			}
			return true
		}
		
		private val isColorRegex = """(?:rgb|rgba|hsb|hsv|hsl)[ \t]*\{[\d. \t]*}""".toRegex()
		
		fun isColorField(string: String): Boolean {
			return string.matches(isColorRegex)
		}
		
		private val threadLocalDateFormat = ThreadLocal.withInitial { SimpleDateFormat("yyyy.MM.dd") }
		
		fun isDateField(string: String): Boolean {
			return try {
				threadLocalDateFormat.get().parse(string)
				true
			} catch(e: Exception) {
				false
			}
		}
	}
}