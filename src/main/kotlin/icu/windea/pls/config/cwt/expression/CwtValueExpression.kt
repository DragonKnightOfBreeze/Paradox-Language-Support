package icu.windea.pls.config.cwt.expression

import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.CwtDataTypes as Types
import icu.windea.pls.config.cwt.expression.CwtKvExpressionPriorities as Priorities

/**
 * CWT值表达式。
 * @property type 类型
 * @property value 值
 */
class CwtValueExpression private constructor(
	expressionString: String,
	override val type: CwtValueDataType,
	override val priority: Int,
	override val value: String? = null,
	override val extraValue: Any? = null
) : AbstractExpression(expressionString), CwtKvExpression {
	companion object Resolver : CachedExpressionResolver<CwtValueExpression>() {
		val EmptyExpression = CwtValueExpression("", Types.Any, Priorities.fallbackPriority)
		val EmptyStringExpression = CwtValueExpression("", Types.Constant,Priorities.constantPriority, "")
		
		override fun doResolve(expressionString: String): CwtValueExpression {
			return when {
				expressionString.isEmpty() -> EmptyStringExpression
				expressionString == "any" -> {
					CwtValueExpression(expressionString, Types.Any, Priorities.fallbackPriority)
				}
				expressionString == "bool" -> {
					CwtValueExpression(expressionString, Types.Bool, Priorities.constantPriority)
				}
				expressionString == "int" -> {
					CwtValueExpression(expressionString, Types.Int, Priorities.constantPriority)
				}
				expressionString.surroundsWith("int[", "]") -> {
					val extraValue = expressionString.substring(4, expressionString.length - 1).toIntRangeOrNull()
					CwtValueExpression(expressionString, Types.Int, Priorities.rangedConstantPriority, null, extraValue)
				}
				expressionString == "float" -> {
					CwtValueExpression(expressionString, Types.Float, Priorities.constantPriority)
				}
				expressionString.surroundsWith("float[", "]") -> {
					val extraValue = expressionString.substring(6, expressionString.length - 1)
					CwtValueExpression(expressionString, Types.Float, Priorities.rangedConstantPriority, null, extraValue)
				}
				expressionString == "scalar" -> {
					CwtValueExpression(expressionString, Types.Scalar, Priorities.scalarPriority)
				}
				expressionString == "colour_field" -> {
					CwtValueExpression(expressionString, Types.ColorField, Priorities.constantPriority)
				}
				expressionString.surroundsWith("colour[", "]") -> {
					val value = expressionString.substring(7, expressionString.length - 1)
					CwtValueExpression(expressionString, Types.ColorField, Priorities.rangedConstantPriority, value)
				}
				expressionString == "percentage_field" -> {
					CwtValueExpression(expressionString, Types.PercentageField, Priorities.constantPriority)
				}
				expressionString == "date_field" -> {
					CwtValueExpression(expressionString, Types.DateField, Priorities.constantPriority)
				}
				expressionString == "localisation" -> {
					CwtValueExpression(expressionString, Types.Localisation, Priorities.localisationReferencePriority)
				}
				expressionString == "localisation_synced" -> {
					CwtValueExpression(expressionString, Types.SyncedLocalisation, Priorities.syncedLocalisationReferencePriority)
				}
				expressionString == "localisation_inline" -> {
					CwtValueExpression(expressionString, Types.InlineLocalisation, Priorities.localisationReferencePriority)
				}
				//EXTENDED BY PLS
				expressionString == "abs_filepath" -> {
					CwtValueExpression(expressionString, Types.AbsoluteFilePath, Priorities.fileReferencePriority)
				}
				expressionString == "filepath" -> {
					CwtValueExpression(expressionString, Types.FilePath, Priorities.fileReferencePriority)
				}
				expressionString.surroundsWith("filepath[", "]") -> {
					val value = expressionString.substring(9, expressionString.length - 1)
					CwtValueExpression(expressionString, Types.FilePath, Priorities.rangedFileReferencePriority, value)
				}
				expressionString.surroundsWith("icon[", "]") -> {
					val value = expressionString.substring(5, expressionString.length - 1)
					CwtValueExpression(expressionString, Types.Icon, Priorities.rangedFileReferencePriority , value)
				}
				expressionString.surroundsWith('<', '>') -> {
					val value = expressionString.substring(1, expressionString.length - 1)
					CwtValueExpression(expressionString, Types.TypeExpression, Priorities.definitionReferencePriority, value)
				}
				expressionString.indexOf('<').let { it > 0 && it < expressionString.indexOf('>') } -> {
					val value = expressionString.substring(expressionString.indexOf('<'), expressionString.indexOf('>'))
					val extraValue = expressionString.substringBefore('<') to expressionString.substringAfterLast('>')
					CwtValueExpression(expressionString, Types.TypeExpressionString, Priorities.definitionReferencePriority,value, extraValue)
				}
				expressionString.surroundsWith("value[", "]") -> {
					val value = expressionString.substring(6, expressionString.length - 1)
					CwtValueExpression(expressionString, Types.Value, Priorities.valuePriority, value)
				}
				expressionString.surroundsWith("value_set[", "]") -> {
					val value = expressionString.substring(10, expressionString.length - 1)
					CwtValueExpression(expressionString, Types.ValueSet,Priorities.valuePriority, value)
				}
				expressionString.surroundsWith("enum[", "]") -> {
					val value = expressionString.substring(5, expressionString.length - 1)
					val priority = if(value == CwtConfigHandler.paramsEnumName) Priorities.parametersPriority else Priorities.enumPriority
					CwtValueExpression(expressionString, Types.Enum, priority, value)
				}
				expressionString.surroundsWith("complex_enum[", "]") -> {
					val value = expressionString.substring(13, expressionString.length - 1)
					CwtValueExpression(expressionString, Types.ComplexEnum, Priorities.complexEnumPriority,value)
				}
				expressionString == "scope_field" -> {
					CwtValueExpression(expressionString, Types.ScopeField, Priorities.scopeFieldPriority)
				}
				expressionString.surroundsWith("scope[", "]") -> {
					//value需要是有效的scope_type
					val value = expressionString.substring(6, expressionString.length - 1).takeIf { it != "any" }
					CwtValueExpression(expressionString, Types.Scope, Priorities.rangedScopeFieldPriority, value)
				}
				expressionString.surroundsWith("scope_group[", "]") -> {
					val value = expressionString.substring(12, expressionString.length - 1)
					CwtValueExpression(expressionString, Types.ScopeGroup, Priorities.rangedScopeFieldPriority, value)
				}
				expressionString == "value_field" -> {
					CwtValueExpression(expressionString, Types.ValueField, Priorities.valueFieldPriority)
				}
				expressionString.surroundsWith("value_field[", "]") -> {
					val value = expressionString.substring(12, expressionString.length - 1)
					CwtValueExpression(expressionString, Types.ValueField, Priorities.rangedValueFieldPriority, value)
				}
				expressionString == "int_value_field" -> {
					CwtValueExpression(expressionString, Types.IntValueField, Priorities.valueFieldPriority)
				}
				expressionString.surroundsWith("int_value_field[", "]") -> {
					val value = expressionString.substring(16, expressionString.length - 1)
					CwtValueExpression(expressionString, Types.IntValueField, Priorities.rangedValueFieldPriority, value)
				}
				expressionString == "variable_field" -> {
					CwtValueExpression(expressionString, Types.VariableField, Priorities.variableFieldPriority)
				}
				expressionString.surroundsWith("variable_field[", "]") -> {
					val value = expressionString.substring(15, expressionString.length - 1)
					CwtValueExpression(expressionString, Types.VariableField, Priorities.rangedVariableFieldPriority, value)
				}
				expressionString == "int_variable_field" -> {
					CwtValueExpression(expressionString, Types.IntVariableField, Priorities.variableFieldPriority)
				}
				expressionString.surroundsWith("int_variable_field[", "]") -> {
					val value = expressionString.substring(19, expressionString.length - 1)
					CwtValueExpression(expressionString, Types.IntVariableField, Priorities.rangedVariableFieldPriority, value)
				}
				expressionString.surroundsWith("single_alias_right[", "]") -> {
					val value = expressionString.substring(19, expressionString.length - 1)
					CwtValueExpression(expressionString, Types.SingleAliasRight, Priorities.aliasPriority,value)
				}
				expressionString.surroundsWith("alias_keys_field[", "]") -> {
					val value = expressionString.substring(17, expressionString.length - 1)
					CwtValueExpression(expressionString, Types.AliasKeysField,Priorities.aliasPriority, value)
				}
				expressionString.surroundsWith("alias_match_left[", "]") -> {
					val value = expressionString.substring(17, expressionString.length - 1)
					CwtValueExpression(expressionString, Types.AliasMatchLeft, Priorities.aliasPriority, value)
				}
				expressionString.endsWith(']') -> {
					CwtValueExpression(expressionString, Types.Other,Priorities.fallbackPriority)
				}
				else -> {
					CwtValueExpression(expressionString, Types.Constant,Priorities.constantPriority, expressionString)
				}
			}
		}
	}
	
	operator fun component1() = type
	
	operator fun component2() = value
}