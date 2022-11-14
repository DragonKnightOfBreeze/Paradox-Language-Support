package icu.windea.pls.config.cwt.expression

import com.google.common.cache.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.config.cwt.expression.CwtDataTypes as Types

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
) : AbstractExpression(expressionString), CwtDataExpression {
	companion object Resolver {
		val EmptyStringExpression = CwtKeyExpression("", Types.Constant, "")
		
		val cache by lazy { CacheBuilder.newBuilder().buildCache<String, CwtKeyExpression> { doResolve(it) } }
		
		fun resolve(expressionString: String) = cache.getUnchecked(expressionString)
		
		private fun doResolve(expressionString: String) = when {
			expressionString.isEmpty() -> EmptyStringExpression
			expressionString == "any" -> {
				CwtKeyExpression(expressionString, Types.Any)
			}
			expressionString == "int" -> {
				CwtKeyExpression(expressionString, Types.Int)
			}
			expressionString.surroundsWith("int[", "]") -> {
				val extraValue = expressionString.substring(4, expressionString.length - 1).toIntRangeOrNull()
				CwtKeyExpression(expressionString, Types.Int, null, extraValue)
			}
			expressionString == "float" -> {
				CwtKeyExpression(expressionString, Types.Float)
			}
			expressionString.surroundsWith("float[", "]") -> {
				val extraValue = expressionString.substring(6, expressionString.length - 1).toFloatRangeOrNull()
				CwtKeyExpression(expressionString, Types.Float, null, extraValue)
			}
			expressionString == "scalar" -> {
				CwtKeyExpression(expressionString, Types.Scalar)
			}
			expressionString == "localisation" -> {
				CwtKeyExpression(expressionString, Types.Localisation)
			}
			expressionString == "localisation_synced" -> {
				CwtKeyExpression(expressionString, Types.SyncedLocalisation)
			}
			expressionString == "localisation_inline" -> {
				CwtKeyExpression(expressionString, Types.InlineLocalisation)
			}
			expressionString.surroundsWith('<', '>') -> {
				val value = expressionString.substring(1, expressionString.length - 1)
				CwtKeyExpression(expressionString, Types.TypeExpression, value)
			}
			expressionString.indexOf('<').let { it > 0 && it < expressionString.indexOf('>') } && !expressionString.endsWith("]") -> {
				val value = expressionString.substring(expressionString.indexOf('<'), expressionString.indexOf('>'))
				val extraValue = expressionString.substringBefore('<') to expressionString.substringAfterLast('>')
				CwtKeyExpression(expressionString, Types.TypeExpressionString, value, extraValue)
			}
			expressionString.surroundsWith("enum[", "]") -> {
				val value = expressionString.substring(5, expressionString.length - 1)
				CwtKeyExpression(expressionString, Types.Enum, value)
			}
			expressionString.surroundsWith("value[", "]") -> {
				val value = expressionString.substring(6, expressionString.length - 1)
				CwtKeyExpression(expressionString, Types.Value, value)
			}
			expressionString.surroundsWith("value_set[", "]") -> {
				val value = expressionString.substring(10, expressionString.length - 1)
				CwtKeyExpression(expressionString, Types.ValueSet, value)
			}
			expressionString == "scope_field" -> {
				CwtKeyExpression(expressionString, Types.ScopeField)
			}
			expressionString.surroundsWith("scope[", "]") -> {
				//value需要是有效的scope_type
				val value = expressionString.substring(6, expressionString.length - 1).takeIf { it != "any" }
				CwtKeyExpression(expressionString, Types.Scope, value)
			}
			expressionString.surroundsWith("scope_group[", "]") -> {
				val value = expressionString.substring(12, expressionString.length - 1)
				CwtKeyExpression(expressionString, Types.ScopeGroup, value)
			}
			//EXTENDED BY PLS
			expressionString == "\$modifier" -> {
				CwtKeyExpression(expressionString, Types.Modifier)
			}
			expressionString.surroundsWith("alias_keys_field[", "]") -> {
				val value = expressionString.substring(17, expressionString.length - 1)
				CwtKeyExpression(expressionString, Types.AliasKeysField, value)
			}
			expressionString.surroundsWith("alias_name[", "]") -> {
				val value = expressionString.substring(11, expressionString.length - 1)
				CwtKeyExpression(expressionString, Types.AliasName, value)
			}
			expressionString.endsWith(']') -> {
				CwtKeyExpression(expressionString, Types.Other)
			}
			else -> {
				CwtKeyExpression(expressionString, Types.Constant, expressionString)
			}
		}
	}
	
	operator fun component1() = type
	
	operator fun component2() = value
}