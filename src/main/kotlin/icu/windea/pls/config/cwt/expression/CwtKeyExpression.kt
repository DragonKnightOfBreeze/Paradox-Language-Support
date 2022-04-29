package icu.windea.pls.config.cwt.expression

import icu.windea.pls.*

/**
 * CWT键表达式。
 * @property type 类型
 * @property value 值
 */
class CwtKeyExpression private constructor(
	expressionString: String,
	val type: Type,
	val value: String? = null,
	val extraValue: Any? = null
) : AbstractExpression(expressionString), CwtExpression {
	companion object Resolver : CachedExpressionResolver<CwtKeyExpression>() {
		val EmptyExpression = CwtKeyExpression("", Type.Constant, "")
		
		override fun doResolve(expressionString: String): CwtKeyExpression {
			return when {
				expressionString.isEmpty() -> EmptyExpression
				expressionString == "any" -> {
					CwtKeyExpression(expressionString, Type.Any)
				}
				expressionString == "bool" -> {
					CwtKeyExpression(expressionString, Type.Bool)
				}
				expressionString == "int" -> {
					CwtKeyExpression(expressionString, Type.Int)
				}
				expressionString.surroundsWith("int[", "]") -> {
					val extraValue = expressionString.substring(4, expressionString.length - 1).toIntRangeOrNull()
					CwtKeyExpression(expressionString, Type.IntRange, null, extraValue)
				}
				expressionString == "float" -> {
					CwtKeyExpression(expressionString, Type.Float)
				}
				expressionString.surroundsWith("float[", "]") -> {
					val extraValue = expressionString.substring(6, expressionString.length - 1).toFloatRangeOrNull()
					CwtKeyExpression(expressionString, Type.FloatRange, null, extraValue)
				}
				expressionString == "scalar" -> {
					CwtKeyExpression(expressionString, Type.Scalar)
				}
				expressionString == "localisation" -> {
					CwtKeyExpression(expressionString, Type.Localisation)
				}
				expressionString == "localisation_synced" -> {
					CwtKeyExpression(expressionString, Type.SyncedLocalisation)
				}
				expressionString == "localisation_inline" -> {
					CwtKeyExpression(expressionString, Type.InlineLocalisation)
				}
				expressionString.surroundsWith('<', '>') -> {
					val value = expressionString.substring(1, expressionString.length - 1)
					CwtKeyExpression(expressionString, Type.TypeExpression, value)
				}
				expressionString.indexOf('<').let { it > 0 && it < expressionString.indexOf('>') } -> {
					val value = expressionString.substring(expressionString.indexOf('<'), expressionString.indexOf('>'))
					val extraValue = expressionString.substringBefore('<') to expressionString.substringAfterLast('>')
					CwtKeyExpression(expressionString, Type.TypeExpressionString, value, extraValue)
				}
				expressionString.surroundsWith("enum[", "]") -> {
					val value = expressionString.substring(5, expressionString.length - 1)
					CwtKeyExpression(expressionString, Type.Enum, value)
				}
				expressionString.surroundsWith("complex_enum[", "]") -> {
					val value = expressionString.substring(13, expressionString.length - 1)
					CwtKeyExpression(expressionString, Type.ComplexEnum, value)
				}
				expressionString.surroundsWith("value[", "]") -> {
					val value = expressionString.substring(6, expressionString.length - 1)
					CwtKeyExpression(expressionString, Type.Value, value)
				}
				expressionString.surroundsWith("value_set[", "]") -> {
					val value = expressionString.substring(10, expressionString.length - 1)
					CwtKeyExpression(expressionString, Type.ValueSet, value)
				}
				expressionString.surroundsWith("scope[", "]") -> {
					val value = expressionString.substring(6, expressionString.length - 1)
					CwtKeyExpression(expressionString, Type.Scope, value)
				}
				expressionString == "scope_field" -> {
					CwtKeyExpression(expressionString, Type.ScopeField)
				}
				expressionString.surroundsWith("alias_keys_field[", "]") -> {
					val value = expressionString.substring(17, expressionString.length - 1)
					CwtKeyExpression(expressionString, Type.AliasKeysField, value)
				}
				expressionString.surroundsWith("alias_name[", "]") -> {
					val value = expressionString.substring(11, expressionString.length - 1)
					CwtKeyExpression(expressionString, Type.AliasName, value)
				}
				expressionString.endsWith(']') -> {
					CwtKeyExpression(expressionString, Type.Other)
				}
				else -> {
					CwtKeyExpression(expressionString, Type.Constant, expressionString)
				}
			}
		}
	}
	
	enum class Type {
		Any,
		Bool,
		Int,
		IntRange,
		Float,
		FloatRange,
		Scalar,
		Localisation,
		SyncedLocalisation,
		InlineLocalisation,
		TypeExpression,
		TypeExpressionString,
		Enum,
		ComplexEnum,
		Value,
		ValueSet,
		Scope,
		ScopeField,
		AliasKeysField,
		AliasName,
		Constant,
		Other
	}
	
	operator fun component1() = type
	
	operator fun component2() = value
}