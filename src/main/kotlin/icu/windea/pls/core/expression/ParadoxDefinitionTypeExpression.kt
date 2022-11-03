package icu.windea.pls.core.expression

import com.google.common.cache.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
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
	companion object Resolver : ExpressionResolver<ParadoxDefinitionTypeExpression> {
		val EmptyExpression = ParadoxDefinitionTypeExpression("", "")
		
		val cache by lazy { CacheBuilder.newBuilder().buildCache<String, ParadoxDefinitionTypeExpression> { doResolve(it) } }
		
		fun resolve(expressionString: String) = cache.getUnchecked(expressionString)
		
		private fun doResolve(expressionString: String) = when {
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
						val dotIndex = it.indexOf('.')
						if(dotIndex == -1) {
							val type = it
							type to null
						} else {
							val type = it.substring(0, dotIndex)
							val subtype = it.substring(dotIndex + 1)
							type to subtype
						}
					}
					val (type, subtype) = pairs.first()
					ParadoxDefinitionTypeExpression(expressionString, type, subtype, pairs)
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
	
	inline fun selectAll(action: (type: String, subtype: String?) -> Unit) {
		if(typeAndSubtypePairs == null) {
			action(type, subtype)
		} else {
			for((type, subtype) in typeAndSubtypePairs) {
				action(type, subtype)
			}
		}
	}
	
	fun matches(element: ParadoxDefinitionProperty): Boolean {
		val stub = runCatching { element.getStub() }.getOrNull()
		val definitionInfo = if(stub == null) element.definitionInfo else null
		val targetType = stub?.type ?: definitionInfo?.type ?: return false
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
			val targetSubtypes = stub?.subtypes ?: definitionInfo?.subtypes ?: return false
			if(subtype !in targetSubtypes) return false
		}
		return true
	}
}