package icu.windea.pls.config.cwt.expression

import icu.windea.pls.*

/**
 * CWT键表达式。
 * @property type 类型
 * @property value 值
 */
class CwtKeyExpression private constructor(
	expressionString: String,
	override val type: CwtKeyExpressionType,
	override val value: String? = null,
	override val extraValue: Any? = null
) : AbstractExpression(expressionString), CwtKvExpression {
	companion object Resolver : CachedExpressionResolver<CwtKeyExpression>() {
		val EmptyExpression = CwtKeyExpression("", CwtKvExpressionTypes.Constant, "")
		
		override fun doResolve(expressionString: String): CwtKeyExpression {
			return when {
				expressionString.isEmpty() -> EmptyExpression
				expressionString == "any" -> {
					CwtKeyExpression(expressionString, CwtKvExpressionTypes.Any)
				}
				expressionString == "int" -> {
					CwtKeyExpression(expressionString, CwtKvExpressionTypes.Int)
				}
				expressionString.surroundsWith("int[", "]") -> {
					val extraValue = expressionString.substring(4, expressionString.length - 1).toIntRangeOrNull()
					CwtKeyExpression(expressionString, CwtKvExpressionTypes.Int, null, extraValue)
				}
				expressionString == "float" -> {
					CwtKeyExpression(expressionString, CwtKvExpressionTypes.Float)
				}
				expressionString.surroundsWith("float[", "]") -> {
					val extraValue = expressionString.substring(6, expressionString.length - 1).toFloatRangeOrNull()
					CwtKeyExpression(expressionString, CwtKvExpressionTypes.Float, null, extraValue)
				}
				expressionString == "scalar" -> {
					CwtKeyExpression(expressionString, CwtKvExpressionTypes.Scalar)
				}
				expressionString == "localisation" -> {
					CwtKeyExpression(expressionString, CwtKvExpressionTypes.Localisation)
				}
				expressionString == "localisation_synced" -> {
					CwtKeyExpression(expressionString, CwtKvExpressionTypes.SyncedLocalisation)
				}
				expressionString == "localisation_inline" -> {
					CwtKeyExpression(expressionString, CwtKvExpressionTypes.InlineLocalisation)
				}
				expressionString.surroundsWith('<', '>') -> {
					val value = expressionString.substring(1, expressionString.length - 1)
					CwtKeyExpression(expressionString, CwtKvExpressionTypes.TypeExpression, value)
				}
				expressionString.indexOf('<').let { it > 0 && it < expressionString.indexOf('>') } -> {
					val value = expressionString.substring(expressionString.indexOf('<'), expressionString.indexOf('>'))
					val extraValue = expressionString.substringBefore('<') to expressionString.substringAfterLast('>')
					CwtKeyExpression(expressionString, CwtKvExpressionTypes.TypeExpressionString, value, extraValue)
				}
				expressionString.surroundsWith("enum[", "]") -> {
					val value = expressionString.substring(5, expressionString.length - 1)
					CwtKeyExpression(expressionString, CwtKvExpressionTypes.Enum, value)
				}
				expressionString.surroundsWith("complex_enum[", "]") -> {
					val value = expressionString.substring(13, expressionString.length - 1)
					CwtKeyExpression(expressionString, CwtKvExpressionTypes.ComplexEnum, value)
				}
				expressionString.surroundsWith("value[", "]") -> {
					val value = expressionString.substring(6, expressionString.length - 1)
					CwtKeyExpression(expressionString, CwtKvExpressionTypes.Value, value)
				}
				expressionString.surroundsWith("value_set[", "]") -> {
					val value = expressionString.substring(10, expressionString.length - 1)
					CwtKeyExpression(expressionString, CwtKvExpressionTypes.ValueSet, value)
				}
				expressionString.surroundsWith("scope[", "]") -> {
					val value = expressionString.substring(6, expressionString.length - 1)
					CwtKeyExpression(expressionString, CwtKvExpressionTypes.Scope, value)
				}
				expressionString.surroundsWith("event_target[", "]") -> {
					val value = expressionString.substring(13, expressionString.length - 1)
					CwtKeyExpression(expressionString, CwtKvExpressionTypes.Scope, value)
				}
				expressionString == "scope_field" -> {
					CwtKeyExpression(expressionString, CwtKvExpressionTypes.ScopeField)
				}
				expressionString.surroundsWith("alias_keys_field[", "]") -> {
					val value = expressionString.substring(17, expressionString.length - 1)
					CwtKeyExpression(expressionString, CwtKvExpressionTypes.AliasKeysField, value)
				}
				expressionString.surroundsWith("alias_name[", "]") -> {
					val value = expressionString.substring(11, expressionString.length - 1)
					CwtKeyExpression(expressionString, CwtKvExpressionTypes.AliasName, value)
				}
				expressionString.endsWith(']') -> {
					CwtKeyExpression(expressionString, CwtKvExpressionTypes.Other)
				}
				else -> {
					CwtKeyExpression(expressionString, CwtKvExpressionTypes.Constant, expressionString)
				}
			}
		}
	}
	
	operator fun component1() = type
	
	operator fun component2() = value
}