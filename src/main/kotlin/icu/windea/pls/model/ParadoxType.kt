package icu.windea.pls.model

import icu.windea.pls.lang.util.*

enum class ParadoxType(
	val id: kotlin.String,
	val text: kotlin.String
) {
	Unknown("unknown", "(unknown)"),
	Boolean("boolean", "boolean"),
	Int("int", "int"),
	Float("float", "float"),
	String("string", "string"),
	Color("color", "color"),
	Block("block", "block"),
	Parameter("parameter", "parameter"),
	InlineMath("inline_math", "inline math");
	
	override fun toString(): kotlin.String {
		return text
	}
	
	fun isBooleanType(): kotlin.Boolean {
		return this == Boolean
	}
	
	fun isIntType(): kotlin.Boolean {
		return this == Unknown || this == Int || this == Parameter || this == InlineMath
	}
	
	fun isFloatType(): kotlin.Boolean {
		return this == Unknown || this == Int || this == Float || this == Parameter || this == InlineMath
	}
	
	fun isNumberType(): kotlin.Boolean {
		return this == Unknown || this == Int || this == Float || this == Parameter || this == InlineMath
	}
	
	fun isStringType(): kotlin.Boolean {
		return this == Unknown || this == String || this == Parameter
	}
	
	fun isColorType(): kotlin.Boolean {
		return this == Color
	}
	
	fun isStringLikeType(): kotlin.Boolean {
		return this == Unknown || this == String || this == Parameter || this == Int || this == Float
	}
	
	fun isBlockLikeType(): kotlin.Boolean {
		return this == Block || this == Color || this == InlineMath
	}
	
	fun canBeScriptedVariableValue(): kotlin.Boolean {
		return this == Boolean || this == Int || this == Float || this == String
	}
	
	companion object {
        @JvmStatic
		fun resolve(expression: kotlin.String): ParadoxType {
			return when {
                ParadoxTypeHandler.isBoolean(expression) -> Boolean
                ParadoxTypeHandler.isInt(expression) -> Int
                ParadoxTypeHandler.isFloat(expression) -> Float
                else -> String
            }
		}
    }
}
