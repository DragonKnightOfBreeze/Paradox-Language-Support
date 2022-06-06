package icu.windea.pls.config.cwt.expression

import icu.windea.pls.*

/**
 * CWT键表达式。
 * @property type 类型
 * @property value 值
 */
class CwtKeyExpression private constructor(
	expressionString: String,
	override val type: CwtKeyDataType,
	override val value: String? = null,
	override val extraValue: Any? = null
) : AbstractExpression(expressionString), CwtKvExpression {
	companion object Resolver : CachedExpressionResolver<CwtKeyExpression>() {
		val EmptyExpression = CwtKeyExpression("", CwtDataTypes.Constant, "")
		
		override fun doResolve(expressionString: String): CwtKeyExpression {
			return when {
				expressionString.isEmpty() -> EmptyExpression
				expressionString == "any" -> {
					CwtKeyExpression(expressionString, CwtDataTypes.Any)
				}
				expressionString == "int" -> {
					CwtKeyExpression(expressionString, CwtDataTypes.Int)
				}
				expressionString.surroundsWith("int[", "]") -> {
					val extraValue = expressionString.substring(4, expressionString.length - 1).toIntRangeOrNull()
					CwtKeyExpression(expressionString, CwtDataTypes.Int, null, extraValue)
				}
				expressionString == "float" -> {
					CwtKeyExpression(expressionString, CwtDataTypes.Float)
				}
				expressionString.surroundsWith("float[", "]") -> {
					val extraValue = expressionString.substring(6, expressionString.length - 1).toFloatRangeOrNull()
					CwtKeyExpression(expressionString, CwtDataTypes.Float, null, extraValue)
				}
				expressionString == "scalar" -> {
					CwtKeyExpression(expressionString, CwtDataTypes.Scalar)
				}
				expressionString == "localisation" -> {
					CwtKeyExpression(expressionString, CwtDataTypes.Localisation)
				}
				expressionString == "localisation_synced" -> {
					CwtKeyExpression(expressionString, CwtDataTypes.SyncedLocalisation)
				}
				expressionString == "localisation_inline" -> {
					CwtKeyExpression(expressionString, CwtDataTypes.InlineLocalisation)
				}
				expressionString.surroundsWith('<', '>') -> {
					val value = expressionString.substring(1, expressionString.length - 1)
					CwtKeyExpression(expressionString, CwtDataTypes.TypeExpression, value)
				}
				expressionString.indexOf('<').let { it > 0 && it < expressionString.indexOf('>') } -> {
					val value = expressionString.substring(expressionString.indexOf('<'), expressionString.indexOf('>'))
					val extraValue = expressionString.substringBefore('<') to expressionString.substringAfterLast('>')
					CwtKeyExpression(expressionString, CwtDataTypes.TypeExpressionString, value, extraValue)
				}
				expressionString.surroundsWith("enum[", "]") -> {
					val value = expressionString.substring(5, expressionString.length - 1)
					CwtKeyExpression(expressionString, CwtDataTypes.Enum, value)
				}
				expressionString.surroundsWith("complex_enum[", "]") -> {
					val value = expressionString.substring(13, expressionString.length - 1)
					CwtKeyExpression(expressionString, CwtDataTypes.ComplexEnum, value)
				}
				expressionString.surroundsWith("value[", "]") -> {
					val value = expressionString.substring(6, expressionString.length - 1)
					CwtKeyExpression(expressionString, CwtDataTypes.Value, value)
				}
				expressionString.surroundsWith("value_set[", "]") -> {
					val value = expressionString.substring(10, expressionString.length - 1)
					CwtKeyExpression(expressionString, CwtDataTypes.ValueSet, value)
				}
				expressionString.surroundsWith("scope[", "]") -> {
					//value需要是有效的scope_type
					val value = expressionString.substring(6, expressionString.length - 1).takeIf { it != "any" }
					CwtKeyExpression(expressionString, CwtDataTypes.Scope, value)
				}
				expressionString == "scope_field" -> {
					CwtKeyExpression(expressionString, CwtDataTypes.Scope)
				}
				expressionString.surroundsWith("alias_keys_field[", "]") -> {
					val value = expressionString.substring(17, expressionString.length - 1)
					CwtKeyExpression(expressionString, CwtDataTypes.AliasKeysField, value)
				}
				expressionString.surroundsWith("alias_name[", "]") -> {
					val value = expressionString.substring(11, expressionString.length - 1)
					CwtKeyExpression(expressionString, CwtDataTypes.AliasName, value)
				}
				expressionString.endsWith(']') -> {
					CwtKeyExpression(expressionString, CwtDataTypes.Other)
				}
				else -> {
					CwtKeyExpression(expressionString, CwtDataTypes.Constant, expressionString)
				}
			}
		}
	}
	
	operator fun component1() = type
	
	operator fun component2() = value
}