package icu.windea.pls.config.cwt.expression

import icu.windea.pls.*
import icu.windea.pls.core.expression.*

/**
 * CWT值表达式。
 * @property type 类型
 * @property value 值
 */
class CwtValueExpression(
	expression: String,
	val type: Type,
	val value: String? = null,
	val extraValue: Any? = null
) : AbstractExpression(expression), CwtExpression {
	companion object Resolver : CachedExpressionResolver<CwtValueExpression>() {
		val EmptyExpression = CwtValueExpression("", Type.Constant, "")
		
		override fun doResolve(expressionString: String): CwtValueExpression {
			return when {
				expressionString.isEmpty() -> {
					EmptyExpression
				}
				expressionString == "any" -> {
					CwtValueExpression(expressionString, Type.Any)
				}
				expressionString == "bool" -> {
					CwtValueExpression(expressionString, Type.Bool)
				}
				expressionString == "int" -> {
					CwtValueExpression(expressionString, Type.Int)
				}
				expressionString.surroundsWith("int[", "]") -> {
					val extraValue = expressionString.substring(4, expressionString.length - 1).toIntRangeOrNull()
					CwtValueExpression(expressionString, Type.IntRange, null, extraValue)
				}
				expressionString == "float" -> {
					CwtValueExpression(expressionString, Type.Float)
				}
				expressionString.surroundsWith("float[", "]") -> {
					val extraValue = expressionString.substring(6, expressionString.length - 1)
					CwtValueExpression(expressionString, Type.FloatRange, null, extraValue)
				}
				expressionString == "scalar" -> {
					CwtValueExpression(expressionString, Type.Scalar)
				}
				expressionString == "percentage_field" -> {
					CwtValueExpression(expressionString, Type.PercentageField)
				}
				expressionString == "color_field" -> {
					CwtValueExpression(expressionString, Type.ColorField)
				}
				expressionString == "localisation" -> {
					CwtValueExpression(expressionString, Type.Localisation)
				}
				expressionString == "localisation_synced" -> {
					CwtValueExpression(expressionString, Type.SyncedLocalisation)
				}
				expressionString == "localisation_inline" -> {
					CwtValueExpression(expressionString, Type.InlineLocalisation)
				}
				expressionString == "filepath" -> {
					val (value, extraValue) = when {
						expressionString.surroundsWith("filepath[", "]") -> {
							val v = expressionString.substring(9, expressionString.length - 1)
							val commaIndex = v.indexOf(',')
							if(commaIndex == -1) v to null else v.substring(0, commaIndex) to v.substring(commaIndex + 1)
						}
						else -> null to null
					}
					CwtValueExpression(expressionString, Type.FilePath, value, extraValue)
				}
				expressionString.surroundsWith("icon[", "]") -> {
					val value = expressionString.substring(5, expressionString.length - 1)
					CwtValueExpression(expressionString, Type.Icon, value)
				}
				expressionString == "date_field" -> {
					CwtValueExpression(expressionString, Type.DateField)
				}
				expressionString.surroundsWith('<', '>') -> {
					val value = expressionString.substring(1, expressionString.length - 1)
					CwtValueExpression(expressionString, Type.TypeExpression, value)
				}
				expressionString.indexOf('<').let { it > 0 && it < expressionString.indexOf('>') } -> {
					val value = expressionString.substring(expressionString.indexOf('<'), expressionString.indexOf('>'))
					val extraValue = expressionString.substringBefore('<') to expressionString.substringAfterLast('>')
					CwtValueExpression(expressionString, Type.TypeExpressionString, value, extraValue)
				}
				expressionString.surroundsWith("value[", "]") -> {
					val value = expressionString.substring(6, expressionString.length - 1)
					CwtValueExpression(expressionString, Type.Value, value)
				}
				expressionString.surroundsWith("value_set[", "]") -> {
					val value = expressionString.substring(10, expressionString.length - 1)
					CwtValueExpression(expressionString, Type.ValueSet, value)
				}
				expressionString.surroundsWith("enum[", "]") -> {
					val value = expressionString.substring(5, expressionString.length - 1)
					CwtValueExpression(expressionString, Type.Enum, value)
				}
				expressionString.surroundsWith("complex_enum[", "]") -> {
					val value = expressionString.substring(13, expressionString.length - 1)
					CwtValueExpression(expressionString, Type.ComplexEnum, value)
				}
				expressionString.surroundsWith("scope[", "]") -> {
					val value = expressionString.substring(6, expressionString.length - 1)
					CwtValueExpression(expressionString, Type.Scope, value)
				}
				expressionString == "scope_field" -> {
					CwtValueExpression(expressionString, Type.ScopeField)
				}
				expressionString == "variable_field" -> {
					val value = when {
						expressionString.surroundsWith("variable_field[", "]") -> expressionString.substring(15, expressionString.length - 1)
						else -> null
					}
					CwtValueExpression(expressionString, Type.VariableField, value)
				}
				expressionString == "int_variable_field" -> {
					val value = when {
						expressionString.surroundsWith("int_variable_field[", "]") -> expressionString.substring(19, expressionString.length - 1)
						else -> null
					}
					CwtValueExpression(expressionString, Type.IntVariableField, value)
				}
				expressionString == "value_field" -> {
					val value = when {
						expressionString.surroundsWith("value_field[", "]") -> expressionString.substring(12, expressionString.length - 1)
						else -> null
					}
					CwtValueExpression(expressionString, Type.ValueField, value)
				}
				expressionString == "int_value_field" -> {
					val value = when {
						expressionString.surroundsWith("int_value_field[", "]") -> expressionString.substring(16, expressionString.length - 1)
						else -> null
					}
					CwtValueExpression(expressionString, Type.IntValueField, value)
				}
				expressionString.surroundsWith("single_alias_right[", "]") -> {
					val value = expressionString.substring(19, expressionString.length - 1)
					CwtValueExpression(expressionString, Type.SingleAliasRight, value)
				}
				expressionString.surroundsWith("alias_keys_field[", "]") -> {
					val value = expressionString.substring(17, expressionString.length - 1)
					CwtValueExpression(expressionString, Type.AliasKeysField, value)
				}
				expressionString.surroundsWith("alias_match_left[", "]") -> {
					val value = expressionString.substring(17, expressionString.length - 1)
					CwtValueExpression(expressionString, Type.AliasMatchLeft, value)
				}
				expressionString.endsWith(']') -> CwtValueExpression(expressionString, Type.Other)
				else -> CwtValueExpression(expressionString, Type.Constant, expressionString)
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
		Constant,
		Other
	}
	
	operator fun component1() = type
	
	operator fun component2() = value
}