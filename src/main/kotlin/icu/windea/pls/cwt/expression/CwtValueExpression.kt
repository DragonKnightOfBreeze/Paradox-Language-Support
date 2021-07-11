package icu.windea.pls.cwt.expression

import icu.windea.pls.*

class CwtValueExpression(
	expression: String,
	val type: Type,
	val value: String? = null,
	val extraValue:Any? = null
) : AbstractExpression(expression) {
	companion object Resolver : AbstractExpressionResolver<CwtValueExpression>() {
		override val emptyExpression = CwtValueExpression("",Type.Constant,"")
		
		override fun resolve(expression: String): CwtValueExpression {
			return cache.getOrPut(expression) { doResolve(expression) }
		}
		
		private fun doResolve(expression: String): CwtValueExpression {
			return when {
				expression.isEmpty() -> {
					emptyExpression
				}
				expression == "any" -> {
					CwtValueExpression(expression, Type.Any)
				}
				expression == "bool" -> {
					CwtValueExpression(expression, Type.Bool)
				}
				expression == "int" -> {
					CwtValueExpression(expression, Type.Int)
				}
				expression.surroundsWith("int[", "]") -> {
					val extraValue = expression.substring(4, expression.length - 1).toIntRangeOrNull()
					CwtValueExpression(expression, Type.IntRange, null,extraValue)
				}
				expression == "float" -> {
					CwtValueExpression(expression, Type.Float)
				}
				expression.surroundsWith("float[", "]") -> {
					val extraValue = expression.substring(6, expression.length - 1)
					CwtValueExpression(expression, Type.FloatRange, null,extraValue)
				}
				expression == "scalar" -> {
					CwtValueExpression(expression, Type.Scalar)
				}
				expression == "percentage_field" -> {
					CwtValueExpression(expression, Type.PercentageField)
				}
				expression == "color_field" -> {
					CwtValueExpression(expression, Type.ColorField)
				}
				expression == "localisation" -> {
					CwtValueExpression(expression, Type.Localisation)
				}
				expression == "localisation_synced" -> {
					CwtValueExpression(expression, Type.SyncedLocalisation)
				}
				expression == "localisation_inline" -> {
					CwtValueExpression(expression, Type.InlineLocalisation)
				}
				expression == "filepath" -> {
					val (value,extraValue) = when{
						expression.surroundsWith("filepath[", "]") -> {
							val v = expression.substring(9, expression.length - 1)
							val commaIndex = v.indexOf(',')
							if(commaIndex == -1) v to null else v.substring(0,commaIndex) to v.substring(commaIndex+1)
						}
						else -> null to null
					}
					CwtValueExpression(expression, Type.FilePath,value,extraValue)
				}
				expression.surroundsWith("icon[", "]") -> {
					val value = expression.substring(5, expression.length - 1)
					CwtValueExpression(expression, Type.Icon, value)
				}
				expression == "date_field" -> {
					CwtValueExpression(expression, Type.DateField)
				}
				expression.surroundsWith('<', '>') -> {
					val value = expression.substring(1, expression.length - 1)
					CwtValueExpression(expression, Type.TypeExpression, value)
				}
				expression.indexOf('<').let { it > 0 && it < expression.indexOf('>') } -> {
					val value = expression.substring(expression.indexOf('<'), expression.indexOf('>'))
					val extraValue = expression.substringBefore('<') to expression.substringAfterLast('>')
					CwtValueExpression(expression, Type.TypeExpressionString, value,extraValue)
				}
				expression.surroundsWith("value[", "]") -> {
					val value = expression.substring(6, expression.length - 1)
					CwtValueExpression(expression, Type.Value, value)
				}
				expression.surroundsWith("value_set[", "]") -> {
					val value = expression.substring(10, expression.length - 1)
					CwtValueExpression(expression, Type.ValueSet, value)
				}
				expression.surroundsWith("enum[", "]") -> {
					val value = expression.substring(5, expression.length - 1)
					CwtValueExpression(expression, Type.Enum, value)
				}
				expression.surroundsWith("complex_enum[", "]") -> {
					val value = expression.substring(13, expression.length - 1)
					CwtValueExpression(expression, Type.ComplexEnum, value)
				}
				expression.surroundsWith("scope[", "]") -> {
					val value = expression.substring(6, expression.length - 1)
					CwtValueExpression(expression, Type.Scope, value)
				}
				expression == "scope_field" -> {
					CwtValueExpression(expression, Type.ScopeField)
				}
				expression == "variable_field" -> {
					val value = when{
						expression.surroundsWith("variable_field[", "]") ->  expression.substring(15, expression.length - 1)
						else -> null
					}
					CwtValueExpression(expression, Type.VariableField,value)
				}
				expression == "int_variable_field" -> {
					val value = when{
						expression.surroundsWith("int_variable_field[", "]") ->  expression.substring(19, expression.length - 1)
						else -> null
					}
					CwtValueExpression(expression, Type.IntVariableField,value)
				}
				expression == "value_field" -> {
					val value = when{
						expression.surroundsWith("value_field[", "]") ->  expression.substring(12, expression.length - 1)
						else -> null
					}
					CwtValueExpression(expression, Type.ValueField,value)
				}
				expression == "int_value_field" -> {
					val value = when {
						expression.surroundsWith("int_value_field[", "]") -> expression.substring(16, expression.length - 1)
						else -> null
					}
					CwtValueExpression(expression, Type.IntValueField,value)
				}
				expression.surroundsWith("single_alias_right[", "]") -> {
					val value = expression.substring(19, expression.length - 1)
					CwtValueExpression(expression, Type.SingleAliasRight, value)
				}
				expression.surroundsWith("alias_keys_field[", "]") -> {
					val value = expression.substring(17, expression.length - 1)
					CwtValueExpression(expression, Type.AliasKeysField, value)
				}
				expression.surroundsWith("alias_match_left[", "]") -> {
					val value = expression.substring(17, expression.length - 1)
					CwtValueExpression(expression, Type.AliasMatchLeft, value)
				}
				else -> {
					val value = expression
					CwtValueExpression(expression, Type.Constant, value)
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
		PercentageField,
		ColorField,
		Localisation,
		SyncedLocalisation,
		InlineLocalisation,
		FilePath,
		FilePathExpression,
		Icon,
		DateField,
		TypeExpression,
		TypeExpressionString,
		Value,
		ValueSet,
		Enum,
		ComplexEnum,
		Scope,
		ScopeField,
		VariableField,
		IntVariableField,
		ValueField,
		IntValueField,
		SingleAliasRight,
		AliasKeysField,
		AliasMatchLeft,
		Constant
	}
	
	operator fun component1() = type
	
	operator fun component2() = value
}