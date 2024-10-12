package icu.windea.pls.lang.search

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
    override fun processResults(consumer: Processor<in T>): Boolean {
        return delegateProcessResults(original, CommonProcessors.UniqueProcessor(consumer))
    }
    
    fun find(): T? {
        val selector = searchParameters.selector
        var result: T? = null
        delegateProcessResults(original) {
            if(selector.selectOne(it)) {
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
        return result
    }
    
    override fun findAll(): Set<T> {
        //性能优化：
        //* 尽可能少地调用MutableSet.add
        //* 懒加载真正的finalComparator
        //* 尽可能少地调用排序逻辑（当result中的元素小于等于1个时，不需要调用）
        
        val selector = searchParameters.selector
        val finalComparator by lazy { getFinalComparator() }
        val comparator = Comparator<T> c@{ o1, o2 ->
            if(o1 == o2) return@c 0 
            finalComparator.compare(o1, o2)
        }
        val result = sortedSetOf(comparator)
        delegateProcessResults(original) {
            if(selector.select(it)) {
                result += it
            }
            true
        }
        return selector.postHandle(result)
    }
    
    override fun forEach(consumer: Processor<in T>): Boolean {
        //TODO 1.3.8+ 需要检查这里的改动（适用排序）是否会显著影响性能
        val result = findAll()
        return result.process { consumer.process(it) }
    }
    
    fun getFinalComparator(): Comparator<T> {
        //注意：最终使用的排序器需要将比较结果为0的项按照原有顺序进行排序，除非它们值相等
        var comparator = searchParameters.selector.comparator()
        comparator = comparator thenPossible getPriorityComparator()
        comparator = comparator thenPossible Comparator { o1, o2 -> if(o1 == o2) 0 else 1 }
        return comparator!!
    }
    
    fun getPriorityComparator(): Comparator<T> {
        return ParadoxPriorityProvider.getComparator(searchParameters)
    }
    
    override fun toString(): String {
        return "ParadoxQuery: $original"
    }
}
