package icu.windea.pls.config.cwt.expression

import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.CwtDataTypes as Types
import icu.windea.pls.config.cwt.expression.CwtKvExpressionPriorities as Priorities

/**
 * CWT键表达式。
 * @property type 类型
 * @property value 值
 */
class CwtKeyExpression private constructor(
	expressionString: String,
	override val type: CwtKeyDataType,
	override val priority: Int,
	override val value: String? = null,
	override val extraValue: Any? = null
) : AbstractExpression(expressionString), CwtKvExpression {
	companion object Resolver : CachedExpressionResolver<CwtKeyExpression>() {
		val EmptyStringExpression = CwtKeyExpression("", Types.Constant,Priorities.constantPriority, "")
		
		override fun doResolve(expressionString: String): CwtKeyExpression {
			return when {
				expressionString.isEmpty() -> EmptyStringExpression
				expressionString == "any" -> {
					CwtKeyExpression(expressionString, Types.Any, Priorities.fallbackPriority)
				}
				expressionString == "int" -> {
					CwtKeyExpression(expressionString, Types.Int, Priorities.constantPriority)
				}
				expressionString.surroundsWith("int[", "]") -> {
					val extraValue = expressionString.substring(4, expressionString.length - 1).toIntRangeOrNull()
					CwtKeyExpression(expressionString, Types.Int, Priorities.rangedConstantPriority, null, extraValue)
				}
				expressionString == "float" -> {
					CwtKeyExpression(expressionString, Types.Float, Priorities.constantPriority)
				}
				expressionString.surroundsWith("float[", "]") -> {
					val extraValue = expressionString.substring(6, expressionString.length - 1).toFloatRangeOrNull()
					CwtKeyExpression(expressionString, Types.Float, Priorities.rangedConstantPriority, null, extraValue)
				}
				expressionString == "scalar" -> {
					CwtKeyExpression(expressionString, Types.Scalar, Priorities.scalarPriority)
				}
				expressionString == "localisation" -> {
					CwtKeyExpression(expressionString, Types.Localisation, Priorities.localisationReferencePriority)
				}
				expressionString == "localisation_synced" -> {
					CwtKeyExpression(expressionString, Types.SyncedLocalisation, Priorities.syncedLocalisationReferencePriority)
				}
				expressionString == "localisation_inline" -> {
					CwtKeyExpression(expressionString, Types.InlineLocalisation, Priorities.localisationReferencePriority)
				}
				expressionString.surroundsWith('<', '>') -> {
					val value = expressionString.substring(1, expressionString.length - 1)
					CwtKeyExpression(expressionString, Types.TypeExpression, Priorities.definitionReferencePriority, value)
				}
				expressionString.indexOf('<').let { it > 0 && it < expressionString.indexOf('>') } && !expressionString.endsWith("]") -> {
					val value = expressionString.substring(expressionString.indexOf('<'), expressionString.indexOf('>'))
					val extraValue = expressionString.substringBefore('<') to expressionString.substringAfterLast('>')
					CwtKeyExpression(expressionString, Types.TypeExpressionString, Priorities.definitionReferencePriority, value, extraValue)
				}
				expressionString.surroundsWith("enum[", "]") -> {
					val value = expressionString.substring(5, expressionString.length - 1)
					val priority = if(value == CwtConfigHandler.paramsEnumName) Priorities.parametersPriority else Priorities.enumPriority
					CwtKeyExpression(expressionString, Types.Enum, priority, value)
				}
				expressionString.surroundsWith("complex_enum[", "]") -> {
					val value = expressionString.substring(13, expressionString.length - 1)
					CwtKeyExpression(expressionString, Types.ComplexEnum, Priorities.complexEnumPriority, value)
				}
				expressionString.surroundsWith("value[", "]") -> {
					val value = expressionString.substring(6, expressionString.length - 1)
					CwtKeyExpression(expressionString, Types.Value, Priorities.valuePriority, value)
				}
				expressionString.surroundsWith("value_set[", "]") -> {
					val value = expressionString.substring(10, expressionString.length - 1)
					CwtKeyExpression(expressionString, Types.ValueSet, Priorities.valuePriority, value)
				}
				expressionString == "scope_field" -> {
					CwtKeyExpression(expressionString, Types.ScopeField, Priorities.scopePriority)
				}
				expressionString.surroundsWith("scope[", "]") -> {
					//value需要是有效的scope_type
					val value = expressionString.substring(6, expressionString.length - 1).takeIf { it != "any" }
					CwtKeyExpression(expressionString, Types.Scope, Priorities.rangedScopePriority, value)
				}
				expressionString.surroundsWith("scope_group[", "]") -> {
					val value = expressionString.substring(12, expressionString.length - 1)
					CwtKeyExpression(expressionString, Types.ScopeGroup, Priorities.rangedScopePriority, value)
				}
				//EXTENDED BY PLS
				expressionString == "\$modifier" -> {
					CwtKeyExpression(expressionString, Types.Modifier, Priorities.modifierPriority)
				}
				expressionString.surroundsWith("alias_keys_field[", "]") -> {
					val value = expressionString.substring(17, expressionString.length - 1)
					CwtKeyExpression(expressionString, Types.AliasKeysField, Priorities.aliasPriority, value)
				}
				expressionString.surroundsWith("alias_name[", "]") -> {
					val value = expressionString.substring(11, expressionString.length - 1)
					CwtKeyExpression(expressionString, Types.AliasName, Priorities.aliasPriority, value)
				}
				expressionString.endsWith(']') -> {
					CwtKeyExpression(expressionString, Types.Other, Priorities.fallbackPriority)
				}
				else -> {
					CwtKeyExpression(expressionString, Types.Constant, Priorities.constantPriority, expressionString)
				}
			}
		}
	}
	
	operator fun component1() = type
	
	operator fun component2() = value
}