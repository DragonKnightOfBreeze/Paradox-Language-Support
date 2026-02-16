package icu.windea.pls.lang.search

import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.AbstractQuery
import com.intellij.util.Processor
import com.intellij.util.Query
import com.intellij.util.QueryFactory
import icu.windea.pls.core.collections.synced
import icu.windea.pls.core.thenPossible
import icu.windea.pls.lang.overrides.ParadoxOverrideService
import icu.windea.pls.lang.search.selector.ParadoxSearchSelector

/**
 * 可对查询结果进行进一步的处理，包括排序、过滤、去重等。
 *
 * @see ParadoxSearchParameters
 * @see ParadoxSearchSelector
 */
interface ParadoxQuery<T, R : Any> : Query<R> {
    val selector: ParadoxSearchSelector<T>
    val overrideComparator: Comparator<T>
    val finalComparator: Comparator<T>

    fun find(): R?
    override fun findFirst(): R?
    override fun findAll(): Set<R>
    override fun forEach(consumer: Processor<in R>): Boolean

    fun onlyMostRelevant(value: Boolean): ParadoxQuery<T, R> = this
}

typealias ParadoxUnaryQuery<T> = ParadoxQuery<T, T>

fun <R : Any, P : ParadoxSearchParameters<R>> QueryFactory<R, P>.createParadoxQuery(parameters: P): ParadoxQuery<R, R> {
    return ParadoxQueryImpl(createQuery(parameters), parameters)
}

fun <T, R : Any, R1 : Any> ParadoxQuery<T, R>.withTransform(transform: (R) -> R1?): ParadoxQuery<T, R1> {
    return ParadoxTransformingQuery(this, transform)
}

// region Implementations

private class ParadoxQueryImpl<T : Any, P : ParadoxSearchParameters<T>>(
    private val original: Query<T>,
    private val searchParameters: P
) : AbstractQuery<T>(), ParadoxUnaryQuery<T> {
    private var onlyMostRelevant = false

    override val selector get() = searchParameters.selector
    override val overrideComparator by lazy { ParadoxOverrideService.getOverrideComparator(searchParameters) }
    override val finalComparator by lazy { computeFinalComparator() }

    private fun computeFinalComparator(): Comparator<T> {
        // 注意：最终使用的排序器需要将比较结果为0的项按照原有顺序进行排序，除非它们值相等
        var comparator = searchParameters.selector.comparator()
        comparator = comparator thenPossible overrideComparator
        comparator = comparator thenPossible Comparator { o1, o2 -> if (o1 == o2) 0 else 1 }
        return comparator!!
    }

    override fun find(): T? {
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
        return result ?: selector.getDefaultValue()
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
        // 性能优化：
        // - 尽可能少地调用 `MutableSet.add`
        // - 懒加载真正的 `finalComparator`
        // - 尽可能少地调用排序逻辑（当 `result` 中的元素小于等于1个时，不需要调用）

        // 首先遍历并进行必要的过滤和排序，得到排序结果后再进行最后的去重

        val selector = searchParameters.selector
        val comparator = Comparator<T> { o1, o2 -> finalComparator.compare(o1, o2) }
        val result = sortedSetOf(comparator).synced()
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
        // 不应当直接重载这个方法，而是应当重载 `processResults()`（否则会破坏调用 `allowParallelProcessing()` 后的行为）

        return super<AbstractQuery>.forEach(consumer)
    }

    override fun processResults(consumer: Processor<in T>): Boolean {
        if (onlyMostRelevant) {
            // 仅查询最相关的项
            find()?.let { consumer.process(it) }
            return true
        }

        // 为了优化性能，目前不再先得到处理后的最终结果再遍历，而是直接遍历并进行必要的过滤和去重（不进行排序）
        // 注意最终的 `forEach()` 的行为和 `find()` `findFirst()` `findAll()` 并不相同
        // 注意如果 `keySelector` 为 null，去重逻辑应直接跳过，`keysToDistinct` 应直接为 null

        val selector = searchParameters.selector
        val keySelector = selector.keySelector()
        val keysToDistinct = if (keySelector == null) null else mutableSetOf<Any?>().synced()
        return delegateProcessResults(original) {
            ProgressManager.checkCanceled()
            if (selector.select(it) && (keysToDistinct == null || keysToDistinct.add(keySelector?.apply(it)))) {
                consumer.process(it)
            } else {
                true
            }
        }
    }

    override fun onlyMostRelevant(value: Boolean): ParadoxUnaryQuery<T> {
        onlyMostRelevant = value
        return this
    }
}

private class ParadoxTransformingQuery<T, R : Any, R1 : Any>(
    private val original: ParadoxQuery<T, R>,
    private val transform: (R) -> R1?
) : ParadoxQuery<T, R1> {
    override val selector get() = original.selector
    override val overrideComparator get() = original.overrideComparator
    override val finalComparator get() = original.finalComparator

    override fun find(): R1? {
        return original.find()?.let { transform(it) }
    }

    override fun findFirst(): R1? {
        return original.find()?.let { transform(it) }
    }

    override fun findAll(): Set<R1> {
        return original.findAll().mapNotNullTo(mutableSetOf()) { transform(it) }
    }

    override fun forEach(consumer: Processor<in R1>): Boolean {
        return original.forEach(Processor {
            val r = transform(it)
            if (r != null) consumer.process(r) else true
        })
    }

    override fun onlyMostRelevant(value: Boolean): ParadoxQuery<T, R1> {
        original.onlyMostRelevant(value)
        return this
    }
}

// endregion
