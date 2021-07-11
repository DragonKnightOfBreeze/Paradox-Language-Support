package icu.windea.pls.cwt.expression

import icu.windea.pls.*

/**
 * 键表达式。
 *
 * @property type 类型
 * @property value 值
 */
class CwtKeyExpression(
	expression: String,
	val type: Type,
	val value: String? = null,
	val extraValue:Any? = null
) : AbstractExpression(expression) {
	companion object Resolver : AbstractExpressionResolver<CwtKeyExpression>() {
		override val emptyExpression = CwtKeyExpression("",Type.Constant,"")
		
		override fun resolve(expression: String): CwtKeyExpression {
			return cache.getOrPut(expression) { doResolve(expression) }
		}
		
		private fun doResolve(expression: String): CwtKeyExpression {
			return when {
				expression.isEmpty() -> {
					emptyExpression
				}
				expression == "any" -> {
					CwtKeyExpression(expression, Type.Any)
				}
				expression == "bool" -> {
					CwtKeyExpression(expression, Type.Bool)
				}
				expression == "int" -> {
					CwtKeyExpression(expression, Type.Int)
				}
				expression.surroundsWith("int[", "]") -> {
					val extraValue = expression.substring(4, expression.length - 1).toIntRangeOrNull()
					CwtKeyExpression(expression, Type.IntRange, null,extraValue)
				}
				expression == "float" -> {
					CwtKeyExpression(expression, Type.Float)
				}
				expression.surroundsWith("float[", "]") -> {
					val extraValue = expression.substring(6, expression.length - 1).toFloatRangeOrNull()
					CwtKeyExpression(expression, Type.FloatRange, null,extraValue)
				}
				expression == "scalar" -> {
					CwtKeyExpression(expression, Type.Scalar)
				}
				expression == "localisation" -> {
					CwtKeyExpression(expression, Type.Localisation)
				}
				expression == "localisation_synced" -> {
					CwtKeyExpression(expression, Type.SyncedLocalisation)
				}
				expression == "localisation_inline" -> {
					CwtKeyExpression(expression, Type.InlineLocalisation)
				}
				expression.surroundsWith('<', '>') -> {
					val value = expression.substring(1, expression.length - 1)
					CwtKeyExpression(expression, Type.TypeExpression, value)
				}
				expression.indexOf('<').let { it > 0 && it < expression.indexOf('>') } -> {
					val value = expression.substring(expression.indexOf('<'), expression.indexOf('>'))
					val extraValue = expression.substringBefore('<') to expression.substringAfterLast('>')
					CwtKeyExpression(expression, Type.TypeExpressionString, value,extraValue)
				}
				expression.surroundsWith("enum[", "]") -> {
					val value = expression.substring(5, expression.length - 1)
					CwtKeyExpression(expression, Type.Enum, value)
				}
				expression.surroundsWith("complex_enum[", "]") -> {
					val value = expression.substring(13, expression.length - 1)
					CwtKeyExpression(expression, Type.ComplexEnum, value)
				}
				expression.surroundsWith("value[", "]") -> {
					val value = expression.substring(6, expression.length - 1)
					CwtKeyExpression(expression, Type.Value, value)
				}
				expression.surroundsWith("value_set[", "]") -> {
					val value = expression.substring(10, expression.length - 1)
					CwtKeyExpression(expression, Type.ValueSet, value)
				}
				expression.surroundsWith("scope[", "]") -> {
					val value = expression.substring(6, expression.length - 1)
					CwtKeyExpression(expression, Type.Scope, value)
				}
				expression == "scope_field" -> {
					CwtKeyExpression(expression, Type.ScopeField)
				}
				expression.surroundsWith("single_alias_right[", "]") -> {
					val value = expression.substring(19, expression.length - 1)
					CwtKeyExpression(expression, Type.SingleAliasRight, value)
				}
				expression.surroundsWith("alias_keys_field[", "]") -> {
					val value = expression.substring(17, expression.length - 1)
					CwtKeyExpression(expression, Type.AliasKeysField, value)
				}
				expression.surroundsWith("alias_name[", "]") -> {
					val value = expression.substring(11, expression.length - 1)
					CwtKeyExpression(expression, Type.AliasName, value)
				}
				else -> {
					val value = expression
					CwtKeyExpression(expression, Type.Constant, value)
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
		SingleAliasRight,
		AliasKeysField,
		AliasName,
		Constant
	}
	
	operator fun component1() = type
	
	operator fun component2() = value
}