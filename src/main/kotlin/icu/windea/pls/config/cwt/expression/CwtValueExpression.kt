package icu.windea.pls.config.cwt.expression

import icu.windea.pls.*

/**
 * CWT值表达式。
 * @property type 类型
 * @property value 值
 */
class CwtValueExpression private constructor(
	expressionString: String,
	override val type: CwtValueExpressionType,
	override val value: String? = null,
	override val extraValue: Any? = null
) : AbstractExpression(expressionString), CwtKvExpression {
	companion object Resolver : CachedExpressionResolver<CwtValueExpression>() {
		val EmptyExpression = CwtValueExpression("", CwtKvExpressionTypes.Constant, "")
		
		override fun doResolve(expressionString: String): CwtValueExpression {
			return when {
				expressionString.isEmpty() -> EmptyExpression
				expressionString == "any" -> {
					CwtValueExpression(expressionString, CwtKvExpressionTypes.Any)
				}
				expressionString == "bool" -> {
					CwtValueExpression(expressionString, CwtKvExpressionTypes.Bool)
				}
				expressionString == "int" -> {
					CwtValueExpression(expressionString, CwtKvExpressionTypes.Int)
				}
				expressionString.surroundsWith("int[", "]") -> {
					val extraValue = expressionString.substring(4, expressionString.length - 1).toIntRangeOrNull()
					CwtValueExpression(expressionString, CwtKvExpressionTypes.Int, null, extraValue)
				}
				expressionString == "float" -> {
					CwtValueExpression(expressionString, CwtKvExpressionTypes.Float)
				}
				expressionString.surroundsWith("float[", "]") -> {
					val extraValue = expressionString.substring(6, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtKvExpressionTypes.Float, null, extraValue)
				}
				expressionString == "scalar" -> {
					CwtValueExpression(expressionString, CwtKvExpressionTypes.Scalar)
				}
				expressionString == "percentage_field" -> {
					CwtValueExpression(expressionString, CwtKvExpressionTypes.PercentageField)
				}
				expressionString == "color_field" -> {
					CwtValueExpression(expressionString, CwtKvExpressionTypes.ColorField)
				}
				expressionString == "localisation" -> {
					CwtValueExpression(expressionString, CwtKvExpressionTypes.Localisation)
				}
				expressionString == "localisation_synced" -> {
					CwtValueExpression(expressionString, CwtKvExpressionTypes.SyncedLocalisation)
				}
				expressionString == "localisation_inline" -> {
					CwtValueExpression(expressionString, CwtKvExpressionTypes.InlineLocalisation)
				}
				expressionString == "abs_filepath" -> {
					CwtValueExpression(expressionString, CwtKvExpressionTypes.AbsoluteFilePath)
				}
				expressionString == "filepath" -> {
					CwtValueExpression(expressionString, CwtKvExpressionTypes.FilePath)
				}
				expressionString.surroundsWith("filepath[", "]") -> {
					val value = expressionString.substring(9, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtKvExpressionTypes.FilePath, value)
				}
				expressionString.surroundsWith("icon[", "]") -> {
					val value = expressionString.substring(5, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtKvExpressionTypes.Icon, value)
				}
				expressionString == "date_field" -> {
					CwtValueExpression(expressionString, CwtKvExpressionTypes.DateField)
				}
				expressionString.surroundsWith('<', '>') -> {
					val value = expressionString.substring(1, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtKvExpressionTypes.TypeExpression, value)
				}
				expressionString.indexOf('<').let { it > 0 && it < expressionString.indexOf('>') } -> {
					val value = expressionString.substring(expressionString.indexOf('<'), expressionString.indexOf('>'))
					val extraValue = expressionString.substringBefore('<') to expressionString.substringAfterLast('>')
					CwtValueExpression(expressionString, CwtKvExpressionTypes.TypeExpressionString, value, extraValue)
				}
				expressionString.surroundsWith("value[", "]") -> {
					val value = expressionString.substring(6, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtKvExpressionTypes.Value, value)
				}
				expressionString.surroundsWith("value_set[", "]") -> {
					val value = expressionString.substring(10, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtKvExpressionTypes.ValueSet, value)
				}
				expressionString.surroundsWith("enum[", "]") -> {
					val value = expressionString.substring(5, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtKvExpressionTypes.Enum, value)
				}
				expressionString.surroundsWith("complex_enum[", "]") -> {
					val value = expressionString.substring(13, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtKvExpressionTypes.ComplexEnum, value)
				}
				expressionString.surroundsWith("scope[", "]") -> {
					val value = expressionString.substring(6, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtKvExpressionTypes.Scope, value)
				}
				expressionString.surroundsWith("event_target[", "]") -> {
					val value = expressionString.substring(13, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtKvExpressionTypes.Scope, value)
				}
				expressionString == "scope_field" -> {
					CwtValueExpression(expressionString, CwtKvExpressionTypes.ScopeField)
				}
				expressionString == "variable_field" -> {
					val value = when {
						expressionString.surroundsWith("variable_field[", "]") -> expressionString.substring(15, expressionString.length - 1)
						else -> null
					}
					CwtValueExpression(expressionString, CwtKvExpressionTypes.VariableField, value)
				}
				expressionString == "int_variable_field" -> {
					val value = when {
						expressionString.surroundsWith("int_variable_field[", "]") -> expressionString.substring(19, expressionString.length - 1)
						else -> null
					}
					CwtValueExpression(expressionString, CwtKvExpressionTypes.IntVariableField, value)
				}
				expressionString == "value_field" -> {
					val value = when {
						expressionString.surroundsWith("value_field[", "]") -> expressionString.substring(12, expressionString.length - 1)
						else -> null
					}
					CwtValueExpression(expressionString, CwtKvExpressionTypes.ValueField, value)
				}
				expressionString == "int_value_field" -> {
					val value = when {
						expressionString.surroundsWith("int_value_field[", "]") -> expressionString.substring(16, expressionString.length - 1)
						else -> null
					}
					CwtValueExpression(expressionString, CwtKvExpressionTypes.IntValueField, value)
				}
				expressionString.surroundsWith("single_alias_right[", "]") -> {
					val value = expressionString.substring(19, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtKvExpressionTypes.SingleAliasRight, value)
				}
				expressionString.surroundsWith("alias_keys_field[", "]") -> {
					val value = expressionString.substring(17, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtKvExpressionTypes.AliasKeysField, value)
				}
				expressionString.surroundsWith("alias_match_left[", "]") -> {
					val value = expressionString.substring(17, expressionString.length - 1)
					CwtValueExpression(expressionString, CwtKvExpressionTypes.AliasMatchLeft, value)
				}
				expressionString.endsWith(']') -> {
					CwtValueExpression(expressionString, CwtKvExpressionTypes.Other)
				}
				else -> {
					CwtValueExpression(expressionString, CwtKvExpressionTypes.Constant, expressionString)
				}
			}
		}
	}
	
	operator fun component1() = type
	
	operator fun component2() = value
}