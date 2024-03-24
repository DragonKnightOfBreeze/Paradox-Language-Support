package icu.windea.pls.lang.search

import com.intellij.openapi.progress.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.ep.priority.*
import icu.windea.pls.lang.search.selector.*

/**
 * 可对查询结果进行进一步的处理，包括排序、过滤、去重等。
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
        ProgressManager.checkCanceled()
        val processor = CommonProcessors.UniqueProcessor(consumer)
        if(parallel) {
            return delegateProcessResults(original, processor)
        } else {
            val lock = ObjectUtils.sentinel("ParadoxQuery lock")
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
        val comparator = getFinalComparator()
        val result = MutableSet(comparator)
        delegateProcessResults(original) {
            result.add(it)
            true
        }
        return result.filterTo(mutableSetOf()) { selector.selectAll(it) }
    }
    
    override fun forEach(consumer: Processor<in T>): Boolean {
        //TODO 1.2.5+ 需要验证这里的改动（适用排序）是否会显著影响性能
        val selector = searchParameters.selector
        val comparator = getFinalComparator()
        val result = MutableSet(comparator)
        delegateProcessResults(original) {
            result.add(it)
            true
        }
        return result.process {
            if(selector.selectAll(it)) {
                consumer.process(it)
            } else {
                true
            }
        }
    }
    
    fun getPriorityComparator(): Comparator<T> {
        return ParadoxPriorityProvider.getComparator(searchParameters)
    }
    
    fun getFinalComparator(): Comparator<T>? {
        //最终使用的排序器需要将比较结果为0的项按照原有顺序进行排序，除非它们值相等
        var comparator = searchParameters.selector.comparator()
        comparator = comparator thenPossible getPriorityComparator()
        comparator = comparator thenPossible Comparator { o1, o2 -> if(o1 == o2) 0 else 1 }
        return comparator
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
