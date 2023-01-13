package icu.windea.pls.config.cwt.expression

import com.google.common.cache.*
import icu.windea.pls.core.*

/**
 * CWT键表达式。
 */
class CwtKeyExpression private constructor(
	expressionString: String,
	override val type: CwtDataType,
	override val value: String? = null,
	override val extraValue: Any? = null
) : AbstractExpression(expressionString), CwtDataExpression {
	companion object Resolver {
		val EmptyStringExpression = CwtKeyExpression("", CwtDataType.Constant, "")
		
		val cache by lazy { CacheBuilder.newBuilder().buildCache<String, CwtKeyExpression> { doResolve(it) } }
		
		fun resolve(expressionString: String) = cache.getUnchecked(expressionString)
		
		private fun doResolve(expressionString: String) = when {
			expressionString.isEmpty() -> EmptyStringExpression
			expressionString == "int" -> {
				CwtKeyExpression(expressionString, CwtDataType.Int)
			}
			expressionString.surroundsWith("int[", "]") -> {
				val range = expressionString.substring(4, expressionString.length - 1)
					.split("..", limit = 2)
					.let { tupleOf(it.getOrNull(0)?.toIntOrNull() ?: 0, it.getOrNull(1)?.toIntOrNull()) }
				CwtKeyExpression(expressionString, CwtDataType.Int, null, range)
			}
			expressionString == "float" -> {
				CwtKeyExpression(expressionString, CwtDataType.Float)
			}
			expressionString.surroundsWith("float[", "]") -> {
				val range = expressionString.substring(6, expressionString.length - 1)
					.split("..", limit = 2)
					.let { tupleOf(it.getOrNull(0)?.toIntOrNull() ?: 0, it.getOrNull(1)?.toIntOrNull()) }
				CwtKeyExpression(expressionString, CwtDataType.Float, null, range)
			}
			expressionString == "scalar" -> {
				CwtKeyExpression(expressionString, CwtDataType.Scalar)
			}
			expressionString == "localisation" -> {
				CwtKeyExpression(expressionString, CwtDataType.Localisation)
			}
			expressionString == "localisation_synced" -> {
				CwtKeyExpression(expressionString, CwtDataType.SyncedLocalisation)
			}
			expressionString == "localisation_inline" -> {
				CwtKeyExpression(expressionString, CwtDataType.InlineLocalisation)
			}
			expressionString == "<modifier>" -> {
				CwtKeyExpression(expressionString, CwtDataType.Modifier)
			}
			expressionString.surroundsWith('<', '>') -> {
				val value = expressionString.substring(1, expressionString.length - 1)
				CwtKeyExpression(expressionString, CwtDataType.Definition, value)
			}
			expressionString.surroundsWith("enum[", "]") -> {
				val value = expressionString.substring(5, expressionString.length - 1)
				CwtKeyExpression(expressionString, CwtDataType.Enum, value)
			}
			expressionString.surroundsWith("value[", "]") -> {
				val value = expressionString.substring(6, expressionString.length - 1)
				CwtKeyExpression(expressionString, CwtDataType.Value, value)
			}
			expressionString.surroundsWith("value_set[", "]") -> {
				val value = expressionString.substring(10, expressionString.length - 1)
				CwtKeyExpression(expressionString, CwtDataType.ValueSet, value)
			}
			expressionString == "scope_field" -> {
				CwtKeyExpression(expressionString, CwtDataType.ScopeField)
			}
			expressionString.surroundsWith("scope[", "]") -> {
				//value需要是有效的scope_type
				val value = expressionString.substring(6, expressionString.length - 1).takeIf { it != "any" }
				CwtKeyExpression(expressionString, CwtDataType.Scope, value)
			}
			expressionString.surroundsWith("scope_group[", "]") -> {
				val value = expressionString.substring(12, expressionString.length - 1)
				CwtKeyExpression(expressionString, CwtDataType.ScopeGroup, value)
			}
			expressionString.surroundsWith("alias_keys_field[", "]") -> {
				val value = expressionString.substring(17, expressionString.length - 1)
				CwtKeyExpression(expressionString, CwtDataType.AliasKeysField, value)
			}
			expressionString.surroundsWith("alias_name[", "]") -> {
				val value = expressionString.substring(11, expressionString.length - 1)
				CwtKeyExpression(expressionString, CwtDataType.AliasName, value)
			}
			CwtTemplateExpression.resolve(expressionString).isNotEmpty() -> {
				CwtKeyExpression(expressionString, CwtDataType.TemplateExpression)
			}
			expressionString.endsWith(']') -> {
				CwtKeyExpression(expressionString, CwtDataType.Other)
			}
			else -> {
				CwtKeyExpression(expressionString, CwtDataType.Constant, expressionString)
			}
		}
	}
	
	operator fun component1() = type
	
	operator fun component2() = value
}