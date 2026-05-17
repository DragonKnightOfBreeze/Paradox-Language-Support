package icu.windea.pls.lang.search.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.AbstractQuery
import com.intellij.util.Processor
import com.intellij.util.Query
import com.intellij.util.QueryFactory
import icu.windea.pls.core.collections.synced
import icu.windea.pls.core.thenPossible
import icu.windea.pls.lang.overrides.ParadoxOverrideComparator

/**
 * 可对查询结果进行进一步的处理，包括排序、过滤、去重等。
 *
 * @see ParadoxSearchParameters
 * @see ParadoxSearchSelector
 */
interface ParadoxQuery<T, R : Any> : Query<R> {
    val overrideComparator: Comparator<T>
    val comparator: Comparator<T>

    fun find(): R?
    override fun findFirst(): R?
    override fun findAll(): List<R>
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

    override val overrideComparator by lazy {
        ParadoxOverrideComparator(searchParameters)
    }

    override val comparator by lazy {
        var comparator = searchParameters.selector.comparator()
        comparator = comparator thenPossible overrideComparator
        comparator!!
    }

    override fun findAll(): List<T> {
        // 仅查询最相关的项
        if (onlyMostRelevant) {
            return find()?.let { listOf(it) } ?: emptyList()
        }

        val result = mutableListOf<T>()
        val selector = searchParameters.selector
        val keySelector = selector.keySelector()
        val keysToDistinct = if (keySelector == null) null else mutableSetOf<Any?>()
        delegateProcessResults(original) {
            ProgressManager.checkCanceled()
            if (selector.select(it) && (keysToDistinct == null || keysToDistinct.add(keySelector?.apply(it)))) {
                result += it
            }
            true
        }
        return when {
            result.isEmpty() -> emptyList()
            result.size == 1 -> result
            else -> result.sortedWith(comparator)
        }
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

    // 不应当直接重载这个方法，而是应当重载 `processResults()`（否则会破坏调用 `allowParallelProcessing()` 后的行为）
    // override fun forEach(consumer: Processor<in T>): Boolean {
    //     return super<AbstractQuery>.forEach(consumer)
    // }

    override fun processResults(consumer: Processor<in T>): Boolean {
        // 仅查询最相关的项
        if (onlyMostRelevant) {
            return find()?.let { consumer.process(it) } ?: true
        }

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
    override val overrideComparator get() = original.overrideComparator
    override val comparator get() = original.comparator

    override fun find(): R1? {
        return original.find()?.let { transform(it) }
    }

    override fun findFirst(): R1? {
        return original.find()?.let { transform(it) }
    }

    override fun findAll(): List<R1> {
        return original.findAll().mapNotNull { transform(it) }
    }

    override fun forEach(consumer: Processor<in R1>): Boolean {
        return original.forEach(Processor { r -> transform(r)?.let { consumer.process(it) } ?: true })
    }

    override fun onlyMostRelevant(value: Boolean): ParadoxQuery<T, R1> {
        original.onlyMostRelevant(value)
        return this
    }
}

// endregion
