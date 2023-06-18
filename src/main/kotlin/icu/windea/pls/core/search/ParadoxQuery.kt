package icu.windea.pls.core.search

import com.intellij.util.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.priority.*
import java.util.concurrent.atomic.*

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
	var parallel = false
	
	override fun processResults(consumer: Processor<in T>): Boolean {
		val processor = CommonProcessors.UniqueProcessor(consumer)
		if(parallel) {
			return delegateProcessResults(original, processor)
		} else {
			val lock = ObjectUtils.sentinel("AbstractQuery lock")
			return delegateProcessResults(original) {
				synchronized(lock) {
					processor.process(it)
				}
			}
		}
	}
	
	fun find(): T? {
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
		return result ?: selector.defaultValue()
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
		return result ?: selector.defaultValue()
	}
	
	override fun findAll(): Set<T> {
		val selector = searchParameters.selector
		val priorityComparator = getPriorityComparator()
		val selectorComparator = selector.comparator()
		val comparator = if(selectorComparator != null) priorityComparator then selectorComparator else priorityComparator
		val result = sortedSetOf(comparator)
		delegateProcessResults(original) {
			if(selector.selectAll(it)) {
				result.add(it)
			}
			true
		}
		return result
	}
	
	override fun forEach(consumer: Processor<in T>): Boolean {
		val selector = searchParameters.selector
		return delegateProcessResults(original) {
			if(selector.selectAll(it)) {
				val r = consumer.process(it)
				if(!r) return@delegateProcessResults false
			}
			true
		}
	}
	
	fun getPriorityComparator(): Comparator<T> {
		return ParadoxPriorityProvider.getComparator(searchParameters)
	}
	
	/**
	 * 是否有多个查询结果。
	 */
	fun hasMultipleResults(): Boolean {
		val selector = searchParameters.selector
		val flag = AtomicBoolean(false)
		delegateProcessResults(original) {
			if(selector.selectAll(it)) {
				if(flag.get()) return@delegateProcessResults false
				flag.set(true)
			}
			true
		}
		return flag.get()
	}
	
	override fun allowParallelProcessing(): Query<T> {
		parallel = true
		return this
	}
	
	override fun toString(): String {
		return "ParadoxQuery: $original"
	}
}

fun <R : Any, P : ParadoxSearchParameters<R>> QueryFactory<R, P>.createParadoxQuery(parameters: P): ParadoxQuery<R, P> {
	return ParadoxQuery(createQuery(parameters), parameters)
}
