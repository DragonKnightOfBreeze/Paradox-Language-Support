package icu.windea.pls.core

import com.intellij.util.SmartList
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

/**
 * 定义类型表达式。
 *
 * 示例：`origin_or_civic`, `origin_or_civic.origin`, `sprite|spriteType`
 */
class ParadoxDefinitionTypeExpression private constructor(
	expressionString: String,
	val type: String,
	val subtype: String? = null,
	val typeAndSubtypePairs: List<Pair<String, String?>>? = null
) : AbstractExpression(expressionString) {
	companion object Resolver : CachedExpressionResolver<ParadoxDefinitionTypeExpression>() {
		val EmptyExpression = ParadoxDefinitionTypeExpression("", "")
		
		override fun doResolve(expressionString: String): ParadoxDefinitionTypeExpression {
			return when {
				expressionString.isEmpty() -> EmptyExpression
				else -> {
					val pipeIndex = expressionString.indexOf('|')
					if(pipeIndex == -1) {
						val dotIndex = expressionString.indexOf('.')
						if(dotIndex == -1) {
							val type = expressionString
							ParadoxDefinitionTypeExpression(expressionString, type, null, null)
						} else {
							val type = expressionString.substring(0, dotIndex)
							val subtype = expressionString.substring(dotIndex + 1)
							ParadoxDefinitionTypeExpression(expressionString, type, subtype, null)
						}
					} else {
						val pairs = SmartList<Pair<String, String?>>()
						expressionString.split('|').mapTo(pairs) {
							val dotIndex = expressionString.indexOf('.')
							if(dotIndex == -1) {
								val type = expressionString
								type to null
							} else {
								val type = expressionString.substring(0, dotIndex)
								val subtype = expressionString.substring(dotIndex + 1)
								type to subtype
							}
						}
						val (type, subtype) = pairs.first()
						ParadoxDefinitionTypeExpression(expressionString, type, subtype, pairs)
					}
				}
			}
		}
	}
	
	operator fun component1() = type
	
	operator fun component2() = subtype
	
	operator fun component3() = typeAndSubtypePairs
	
	inline fun any(action: (type: String, subtype: String?) -> Boolean): Boolean {
		if(typeAndSubtypePairs == null) {
			return action(type, subtype)
		} else {
			return typeAndSubtypePairs.any { (type, subtype) -> action(type, subtype) }
		}
	}
	
	inline fun <T> select(action: (type: String, subtype: String?) -> T?): T? {
		if(typeAndSubtypePairs == null) {
			return action(type, subtype)
		} else {
			for((type, subtype) in typeAndSubtypePairs) {
				val result = action(type, subtype)
				if(result != null) return result
			}
			return null
		}
	}
	
	inline fun <T> collect(action: (type: String, subtype: String?) -> List<T>): List<T> {
		if(typeAndSubtypePairs == null) {
			return action(type, subtype)
		} else {
			val result = SmartList<T>()
			for((type, subtype) in typeAndSubtypePairs) {
				val r = action(type, subtype)
				result.addAll(r)
			}
			return result
		}
	}
	
	fun matches(element: ParadoxDefinitionProperty): Boolean {
		val stub = element.getStub()
		val definitionInfo = if(stub == null) element.definitionInfo else null
		val targetType = runCatching { stub?.type }.getOrNull() ?: definitionInfo?.type ?: return false
		if(typeAndSubtypePairs == null) {
			return matchesByTypeAndSubtype(targetType, stub, definitionInfo, type, subtype)
		} else {
			return typeAndSubtypePairs.any { (type, subtype) ->
				matchesByTypeAndSubtype(targetType, stub, definitionInfo, type, subtype)
			}
		}
	}
	
	private fun matchesByTypeAndSubtype(targetType: String, stub: ParadoxDefinitionPropertyStub<*>?, definitionInfo: ParadoxDefinitionInfo?, type: String, subtype: String?): Boolean {
		if(type != targetType) return false
		if(subtype != null) {
			val targetSubtypes = runCatching { stub?.subtypes }.getOrNull() ?: definitionInfo?.subtypes ?: return false
			if(subtype !in targetSubtypes) return false
		}
		return true
	}
}