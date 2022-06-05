package icu.windea.pls.config.cwt.expression

import icu.windea.pls.*

/**
 * CWT值表达式。
 * @property type 类型
 * @property value 值
 */
class CwtValueExpression private constructor(
	expressionString: String,
	override val type: CwtValueDataType,
	override val value: String? = null,
	override val extraValue: Any? = null
) : AbstractExpression(expressionString), CwtKvExpression {
	companion object Resolver : CachedExpressionResolver<CwtValueExpression>() {
		val EmptyExpression = CwtValueExpression("", CwtDataTypes.Constant, "")
		
		override fun doResolve(expressionString: String): CwtValueExpression {
			return when {
				expressionString.isEmpty() -> EmptyExpression
				expressionString == "any" -> {
					CwtValueExpression(expressionString, CwtDataTypes.Any)
				}
				expressionString == "bool" -> {
					CwtValueExpression(expressionString, CwtDataTypes.Bool)
				}
				expressionString == "int" -> {
					CwtValueExpression(expressionString, CwtDataTypes.Int)
				}
				expressionString.surroundsWith("int[", "]") -> {
					val extraValue = expressionString.substring(4, expressionString.length - 1).toIntRangeOrNull()
					CwtValueExpression(expressionString, CwtDataTypes.Int, null, extraValue)
				}
				expressionString == "float" -> {
					CwtValueExpression(expressionString, CwtDataTypes.Float)
				}
				expressionString.surroundsWith("float[", "]") -> {
					val extraValue = expressionString.substring(6, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtDataTypes.Float, null, extraValue)
				}
				expressionString == "scalar" -> {
					CwtValueExpression(expressionString, CwtDataTypes.Scalar)
				}
				expressionString == "colour_field" -> {
					CwtValueExpression(expressionString, CwtDataTypes.ColorField)
				}
				expressionString.surroundsWith("colour[", "]") -> {
					val value = expressionString.substring(7, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtDataTypes.ColorField, value)
				}
				expressionString == "percentage_field" -> {
					CwtValueExpression(expressionString, CwtDataTypes.PercentageField)
				}
				expressionString == "date_field" -> {
					CwtValueExpression(expressionString, CwtDataTypes.DateField)
				}
				expressionString == "localisation" -> {
					CwtValueExpression(expressionString, CwtDataTypes.Localisation)
				}
				expressionString == "localisation_synced" -> {
					CwtValueExpression(expressionString, CwtDataTypes.SyncedLocalisation)
				}
				expressionString == "localisation_inline" -> {
					CwtValueExpression(expressionString, CwtDataTypes.InlineLocalisation)
				}
				expressionString == "abs_filepath" -> {
					CwtValueExpression(expressionString, CwtDataTypes.AbsoluteFilePath)
				}
				expressionString == "filepath" -> {
					CwtValueExpression(expressionString, CwtDataTypes.FilePath)
				}
				expressionString.surroundsWith("filepath[", "]") -> {
					val value = expressionString.substring(9, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtDataTypes.FilePath, value)
				}
				expressionString.surroundsWith("icon[", "]") -> {
					val value = expressionString.substring(5, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtDataTypes.Icon, value)
				}
				expressionString.surroundsWith('<', '>') -> {
					val value = expressionString.substring(1, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtDataTypes.TypeExpression, value)
				}
				expressionString.indexOf('<').let { it > 0 && it < expressionString.indexOf('>') } -> {
					val value = expressionString.substring(expressionString.indexOf('<'), expressionString.indexOf('>'))
					val extraValue = expressionString.substringBefore('<') to expressionString.substringAfterLast('>')
					CwtValueExpression(expressionString, CwtDataTypes.TypeExpressionString, value, extraValue)
				}
				expressionString.surroundsWith("value[", "]") -> {
					val value = expressionString.substring(6, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtDataTypes.Value, value)
				}
				expressionString.surroundsWith("value_set[", "]") -> {
					val value = expressionString.substring(10, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtDataTypes.ValueSet, value)
				}
				expressionString.surroundsWith("enum[", "]") -> {
					val value = expressionString.substring(5, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtDataTypes.Enum, value)
				}
				expressionString.surroundsWith("complex_enum[", "]") -> {
					val value = expressionString.substring(13, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtDataTypes.ComplexEnum, value)
				}
				expressionString.surroundsWith("scope_group[", "]") -> {
					val value = expressionString.substring(12, expressionString.length -1)
					CwtValueExpression(expressionString, CwtDataTypes.ScopeGroup, value)
				}
				expressionString.surroundsWith("scope[", "]") -> {
					val value = expressionString.substring(6, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtDataTypes.Scope, value)
				}
				expressionString == "scope_field" -> {
					CwtValueExpression(expressionString, CwtDataTypes.ScopeField)
				}
				expressionString == "variable_field" -> {
					val value = when {
						expressionString.surroundsWith("variable_field[", "]") -> expressionString.substring(15, expressionString.length - 1)
						else -> null
					}
					CwtValueExpression(expressionString, CwtDataTypes.VariableField, value)
				}
				expressionString == "int_variable_field" -> {
					val value = when {
						expressionString.surroundsWith("int_variable_field[", "]") -> expressionString.substring(19, expressionString.length - 1)
						else -> null
					}
					CwtValueExpression(expressionString, CwtDataTypes.IntVariableField, value)
				}
				expressionString == "value_field" -> {
					val value = when {
						expressionString.surroundsWith("value_field[", "]") -> expressionString.substring(12, expressionString.length - 1)
						else -> null
					}
					CwtValueExpression(expressionString, CwtDataTypes.ValueField, value)
				}
				expressionString == "int_value_field" -> {
					val value = when {
						expressionString.surroundsWith("int_value_field[", "]") -> expressionString.substring(16, expressionString.length - 1)
						else -> null
					}
					CwtValueExpression(expressionString, CwtDataTypes.IntValueField, value)
				}
				expressionString.surroundsWith("single_alias_right[", "]") -> {
					val value = expressionString.substring(19, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtDataTypes.SingleAliasRight, value)
				}
				expressionString.surroundsWith("alias_keys_field[", "]") -> {
					val value = expressionString.substring(17, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtDataTypes.AliasKeysField, value)
				}
				expressionString.surroundsWith("alias_match_left[", "]") -> {
					val value = expressionString.substring(17, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtDataTypes.AliasMatchLeft, value)
				}
				expressionString.endsWith(']') -> {
					CwtValueExpression(expressionString, CwtDataTypes.Other)
				}
				else -> {
					CwtValueExpression(expressionString, CwtDataTypes.Constant, expressionString)
				}
			}
		}
	}
	
	operator fun component1() = type
	
	operator fun component2() = value
}