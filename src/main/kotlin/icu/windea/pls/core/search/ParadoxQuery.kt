package icu.windea.pls.core.search

import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.selector.*
import java.util.function.*

/**
 * 可对查询结果进行进一步的处理。
 * * 进一步的过滤、排序和去重。
 * * 可处理查找单个和查找所有的路基有所不同的情况。
 * * 可处理存在默认值的情况。
 * @see ParadoxSearchParameters
 * @see ParadoxSelector
 * @see ChainedParadoxSelector
 */
class ParadoxQuery<T, P : ParadoxSearchParameters<T>>(
	private val original: Query<T>,
	private val searchParameters: P
) : AbstractQuery<T>() {
	override fun processResults(consumer: Processor<in T>): Boolean {
		return delegateProcessResults(original, CommonProcessors.UniqueProcessor(consumer))
	}
	
	fun find(exact: Boolean): T? {
		return if(exact) find() else findFirst()
	}
	
	fun find(): T? {
		val preferOverridden = getSettings().preferOverridden
		val selector = searchParameters.selector
		var result: T? = null
		delegateProcessResults(original) {
			if(selector.select(it)) {
				result = it
				preferOverridden
			} else {
				true
			}
		}
		return result ?: selector.defaultValue
	}
	
	override fun findFirst(): T? {
		val selector = searchParameters.selector
		var result: T? = null
		delegateProcessResults(original) {
			if(selector.select(it)) {
				result = it
				false
			} else {
				true
			}
		}
		return result ?: selector.defaultValue
	}
	
	override fun findAll(): Set<T> {
		val selector = searchParameters.selector
		val result = MutableSet(selector.comparator())
		delegateProcessResults(original) {
			if(selector.selectAll(it)) {
				result.add(it)
			}
			true
		}
		return result
	}
	
	override fun forEach(consumer: Processor<in T>): Boolean {
		//这里不进行排序
		val selector = searchParameters.selector
		return delegateProcessResults(original) {
			if(selector.selectAll(it)) {
				val r = consumer.process(it)
				if(!r) return@delegateProcessResults false
			}
			true
		}
	}
	
	override fun toString(): String {
		return "ParadoxQuery: $original"
	}
}

fun <R : Any, P : ParadoxSearchParameters<R>> QueryFactory<R, P>.createParadoxQuery(parameters: P): ParadoxQuery<R, P> {
	return ParadoxQuery(createQuery(parameters), parameters)
}
