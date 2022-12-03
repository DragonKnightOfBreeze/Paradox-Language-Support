package icu.windea.pls.config.cwt.expression

import com.google.common.cache.*
import icu.windea.pls.core.*
import icu.windea.pls.config.cwt.expression.CwtDataTypes as Types

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
) : AbstractExpression(expressionString), CwtDataExpression {
	companion object Resolver {
		val EmptyExpression = CwtValueExpression("", Types.Any)
		val EmptyStringExpression = CwtValueExpression("", Types.Constant, "")
		
		val cache by lazy { CacheBuilder.newBuilder().buildCache<String, CwtValueExpression> { doResolve(it) } }
		
		fun resolve(expressionString: String) = cache.getUnchecked(expressionString)
		
		private fun doResolve(expressionString: String) = when {
			expressionString.isEmpty() -> EmptyStringExpression
			expressionString == "any" -> {
				CwtValueExpression(expressionString, Types.Any)
			}
			expressionString == "bool" -> {
				CwtValueExpression(expressionString, Types.Bool)
			}
			expressionString == "int" -> {
				CwtValueExpression(expressionString, Types.Int)
			}
			expressionString.surroundsWith("int[", "]") -> {
				val extraValue = expressionString.substring(4, expressionString.length - 1).toIntRangeOrNull()
				CwtValueExpression(expressionString, Types.Int, null, extraValue)
			}
			expressionString == "float" -> {
				CwtValueExpression(expressionString, Types.Float)
			}
			expressionString.surroundsWith("float[", "]") -> {
				val extraValue = expressionString.substring(6, expressionString.length - 1)
				CwtValueExpression(expressionString, Types.Float, null, extraValue)
			}
			expressionString == "scalar" -> {
				CwtValueExpression(expressionString, Types.Scalar)
			}
			expressionString == "colour_field" -> {
				CwtValueExpression(expressionString, Types.ColorField)
			}
			expressionString.surroundsWith("colour[", "]") -> {
				val value = expressionString.substring(7, expressionString.length - 1)
				CwtValueExpression(expressionString, Types.ColorField, value)
			}
			expressionString == "percentage_field" -> {
				CwtValueExpression(expressionString, Types.PercentageField)
			}
			expressionString == "date_field" -> {
				CwtValueExpression(expressionString, Types.DateField)
			}
			expressionString == "localisation" -> {
				CwtValueExpression(expressionString, Types.Localisation)
			}
			expressionString == "localisation_synced" -> {
				CwtValueExpression(expressionString, Types.SyncedLocalisation)
			}
			expressionString == "localisation_inline" -> {
				CwtValueExpression(expressionString, Types.InlineLocalisation)
			}
			//NOTE: Extended by PLS
			expressionString == "abs_filepath" -> {
				CwtValueExpression(expressionString, Types.AbsoluteFilePath)
			}
			expressionString == "filepath" -> {
				CwtValueExpression(expressionString, Types.FilePath)
			}
			expressionString.surroundsWith("filepath[", "]") -> {
				val value = expressionString.substring(9, expressionString.length - 1)
				CwtValueExpression(expressionString, Types.FilePath, value)
			}
			expressionString.surroundsWith("icon[", "]") -> {
				val value = expressionString.substring(5, expressionString.length - 1)
				CwtValueExpression(expressionString, Types.Icon, value)
			}
			expressionString.surroundsWith('<', '>') -> {
				val value = expressionString.substring(1, expressionString.length - 1)
				CwtValueExpression(expressionString, Types.TypeExpression, value)
			}
			expressionString.indexOf('<').let { it > 0 && it < expressionString.indexOf('>') } -> {
				val value = expressionString.substring(expressionString.indexOf('<'), expressionString.indexOf('>'))
				val extraValue = expressionString.substringBefore('<') to expressionString.substringAfterLast('>')
				CwtValueExpression(expressionString, Types.TypeExpressionString, value, extraValue)
			}
			expressionString.surroundsWith("value[", "]") -> {
				val value = expressionString.substring(6, expressionString.length - 1)
				CwtValueExpression(expressionString, Types.Value, value)
			}
			expressionString.surroundsWith("value_set[", "]") -> {
				val value = expressionString.substring(10, expressionString.length - 1)
				CwtValueExpression(expressionString, Types.ValueSet, value)
			}
			expressionString.surroundsWith("enum[", "]") -> {
				val value = expressionString.substring(5, expressionString.length - 1)
				CwtValueExpression(expressionString, Types.Enum, value)
			}
			expressionString == "scope_field" -> {
				CwtValueExpression(expressionString, Types.ScopeField)
			}
			expressionString.surroundsWith("scope[", "]") -> {
				//value需要是有效的scope_type
				val value = expressionString.substring(6, expressionString.length - 1).takeIf { it != "any" }
				CwtValueExpression(expressionString, Types.Scope, value)
			}
			expressionString.surroundsWith("scope_group[", "]") -> {
				val value = expressionString.substring(12, expressionString.length - 1)
				CwtValueExpression(expressionString, Types.ScopeGroup, value)
			}
			expressionString == "value_field" -> {
				CwtValueExpression(expressionString, Types.ValueField)
			}
			expressionString.surroundsWith("value_field[", "]") -> {
				val value = expressionString.substring(12, expressionString.length - 1)
				CwtValueExpression(expressionString, Types.ValueField, value)
			}
			expressionString == "int_value_field" -> {
				CwtValueExpression(expressionString, Types.IntValueField)
			}
			expressionString.surroundsWith("int_value_field[", "]") -> {
				val value = expressionString.substring(16, expressionString.length - 1)
				CwtValueExpression(expressionString, Types.IntValueField, value)
			}
			expressionString == "variable_field" -> {
				CwtValueExpression(expressionString, Types.VariableField)
			}
			expressionString == "variable_field_32" -> {
				CwtValueExpression(expressionString, Types.VariableField)
			}
			expressionString.surroundsWith("variable_field[", "]") -> {
				val value = expressionString.substring(15, expressionString.length - 1)
				CwtValueExpression(expressionString, Types.VariableField, value)
			}
			expressionString == "int_variable_field" -> {
				CwtValueExpression(expressionString, Types.IntVariableField)
			}
			expressionString == "int_variable_field_32" -> {
				CwtValueExpression(expressionString, Types.IntVariableField)
			}
			expressionString.surroundsWith("int_variable_field[", "]") -> {
				val value = expressionString.substring(19, expressionString.length - 1)
				CwtValueExpression(expressionString, Types.IntVariableField, value)
			}
			expressionString.surroundsWith("single_alias_right[", "]") -> {
				val value = expressionString.substring(19, expressionString.length - 1)
				CwtValueExpression(expressionString, Types.SingleAliasRight, value)
			}
			expressionString.surroundsWith("alias_keys_field[", "]") -> {
				val value = expressionString.substring(17, expressionString.length - 1)
				CwtValueExpression(expressionString, Types.AliasKeysField, value)
			}
			expressionString.surroundsWith("alias_match_left[", "]") -> {
				val value = expressionString.substring(17, expressionString.length - 1)
				CwtValueExpression(expressionString, Types.AliasMatchLeft, value)
			}
			expressionString.endsWith(']') -> {
				CwtValueExpression(expressionString, Types.Other)
			}
			else -> {
				CwtValueExpression(expressionString, Types.Constant, expressionString)
			}
		}
	}
	
	operator fun component1() = type
	
	operator fun component2() = value
}
