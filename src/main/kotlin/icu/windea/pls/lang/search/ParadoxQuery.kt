package icu.windea.pls.lang.search

import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.AbstractQuery
import com.intellij.util.CommonProcessors
import com.intellij.util.Processor
import com.intellij.util.Query
import icu.windea.pls.core.collections.synced
import icu.windea.pls.core.thenPossible
import icu.windea.pls.ep.priority.ParadoxPriorityProvider
import icu.windea.pls.lang.search.selector.ChainedParadoxSelector
import icu.windea.pls.lang.search.selector.ParadoxSelector

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
            ProgressManager.checkCanceled()
            if (selector.selectOne(it)) {
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
            ProgressManager.checkCanceled()
            if (selector.select(it)) {
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

        //首先遍历并进行必要的过滤和排序，得到排序结果后再进行最后的去重

        val selector = searchParameters.selector
        val finalComparator by lazy { getFinalComparator() }
        val comparator = Comparator<T> { o1, o2 ->
            finalComparator.compare(o1, o2)
        }
        val result = sortedSetOf(comparator)
        delegateProcessResults(original) {
            ProgressManager.checkCanceled()
            if (selector.select(it)) {
                result += it
            }
            true
        }
        if (result.size <= 1) return result
        val keySelector = selector.keySelector()
        if (keySelector == null) return result
        val keysToDistinct = mutableSetOf<Any?>().synced()
        return result.filterTo(mutableSetOf()) { keysToDistinct.add(keySelector.apply(it)) }
    }

    override fun forEach(consumer: Processor<in T>): Boolean {
        //为了优化性能，目前不再先得到处理后的最终结果再遍历，而是直接遍历并进行必要的过滤与去重（不进行排序）

        val selector = searchParameters.selector
        val keySelector = selector.keySelector()
        val keysToDistinct = mutableSetOf<Any?>().synced()
        return delegateProcessResults(original) {
            ProgressManager.checkCanceled()
            if (selector.select(it) && (keySelector == null || keysToDistinct.add(keySelector.apply(it)))) {
                consumer.process(it)
            }
            true
        }

        //val result = findAll()
        //return result.process { consumer.process(it) }
    }

    fun getPriorityComparator(): Comparator<T> {
        return ParadoxPriorityProvider.getComparator(searchParameters)
    }

    fun getFinalComparator(): Comparator<T> {
        //注意：最终使用的排序器需要将比较结果为0的项按照原有顺序进行排序，除非它们值相等

        var comparator = searchParameters.selector.comparator()
        comparator = comparator thenPossible getPriorityComparator()
        comparator = comparator thenPossible Comparator { o1, o2 -> if (o1 == o2) 0 else 1 }
        return comparator!!
    }

    override fun toString(): String {
        return "ParadoxQuery: $original"
    }
}
