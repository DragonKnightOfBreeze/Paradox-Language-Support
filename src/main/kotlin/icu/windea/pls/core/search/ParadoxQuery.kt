package icu.windea.pls.core.search

import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.lang.priority.*

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
        //这里得到的结果会经过过滤和排序
        //最终使用的排序器需要将比较结果为0的项按照原有顺序进行排序，除非它们值相等
        val selector = searchParameters.selector
        val selectorComparator = selector.comparator()
        val priorityComparator = getPriorityComparator()
        var comparator = selectorComparator
        comparator = comparator thenPossible priorityComparator
        comparator = comparator thenPossible Comparator { o1, o2 -> if(o1 == o2) 0 else 1 }
        val result =  MutableSet(comparator)
        delegateProcessResults(original) {
            result.add(it)
            true
        }
        val finalResult = result.filterTo(mutableSetOf()) { selector.selectAll(it) }
        return finalResult
    }
    
    override fun forEach(consumer: Processor<in T>): Boolean {
        //这里得到的结果仅会经过过滤
        //因此，对于某些要求进行排序的地方（如引用解析），尽量避免使用此方法以及延伸方法（如processQueryAsync）
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
