@file:Suppress("unused")

package icu.windea.pls.core.util.stream

import com.intellij.util.Processor
import java.util.stream.Stream

/**
 * 轻量级的推流，每次接收一个元素。
 *
 * ## API 笔记
 *
 * 此类推流（push stream）是一种基于 Push 模型的零成本抽象（zero-cost abstraction）。
 * 它们不需要额外的中间操作，也不会引入状态机与堆内存分配，因此不存在额外的性能开销。
 *
 * 相比常见的拉流（pull stream），例如 [Stream] 和 [Sequence]，
 * 由于可以直接依赖原生调用栈，而不存在状态持久化的核心开销，
 * 在有着极高的时间复杂度或者极高的元素数量的场景下，它们通常能取得更好的性能表现。
 *
 * 相比平台代码中常见的处理器模式（processor pattern），即使用 [Processor] 作为参数以进行可中断迭代的方式，
 * 由于可以直接进行链式调用，而不需要编写违反直觉且不够直观的回调代码，
 * 在能够保持性能的同时，它们也能取得更好的可读性。
 *
 * ## 实现笔记
 *
 * 使用内联类（inline class）实现，从而避免额外的包装对象开销，同时也避免引入过多的扩展函数。
 *
 * @see Iterable
 * @see Iterator
 * @see Stream
 * @see Sequence
 * @see Processor
 */
@JvmInline
value class PushStream<out T>(val dispatch: ((T) -> Boolean) -> Unit) {
    // Termination operation

    inline fun forEach(crossinline action: (T) -> Unit) {
        dispatch { e ->
            action(e)
            true
        }
    }

    inline fun process(crossinline processor: (T) -> Boolean): Boolean {
        var result = true
        dispatch { e ->
            if (!processor(e)) {
                result = false
                false
            } else {
                true
            }
        }
        return result
    }

    inline fun any(crossinline predicate: (T) -> Boolean): Boolean {
        var result = false
        dispatch { e ->
            if (predicate(e)) {
                result = true
                false
            } else {
                true
            }
        }
        return result
    }

    inline fun all(crossinline predicate: (T) -> Boolean): Boolean {
        var result = true
        dispatch { e ->
            if (!predicate(e)) {
                result = false
                false
            } else {
                true
            }
        }
        return result
    }

    inline fun none(crossinline predicate: (T) -> Boolean): Boolean {
        return !any(predicate)
    }

    inline fun <R> firstNotNullOrOrNull(crossinline transform: (T) -> R): R? {
        var result: R? = null
        dispatch { e ->
            val r = transform(e)
            if (r != null) {
                result = r
                false
            } else {
                true
            }
        }
        return result
    }

    fun firstOrNull(): T? {
        var result: T? = null
        dispatch { e ->
            result = e
            false
        }
        return result
    }

    inline fun firstOrNull(crossinline predicate: (T) -> Boolean): T? {
        var result: T? = null
        dispatch { e ->
            if (predicate(e)) {
                result = e
                false
            } else {
                true
            }
        }
        return result
    }

    inline fun find(crossinline predicate: (T) -> Boolean): T? {
        return firstOrNull(predicate)
    }

    // Intermediate operations

    inline fun <R> map(crossinline transform: (T) -> R): PushStream<R> {
        return PushStream { downstream ->
            dispatch { e ->
                downstream(transform(e))
            }
        }
    }

    inline fun filter(crossinline predicate: (T) -> Boolean): PushStream<T> {
        return PushStream { downstream ->
            dispatch { e ->
                if (predicate(e)) downstream(e) else true
            }
        }
    }

    fun take(count: Int): PushStream<T> {
        if (count <= 0) return of()
        return PushStream { downstream ->
            var remaining = count
            dispatch { e ->
                val continueNext = downstream(e)
                remaining--
                continueNext && remaining > 0
            }
        }
    }

    // Conversion operations

    fun toOne(): T? {
        return firstOrNull()
    }

    fun toList(): List<T> {
        val s = this
        return buildList { s.forEach { e -> add(e) } }
    }

    fun toSet(): Set<T> {
        val s = this
        return buildSet { s.forEach { e -> add(e) } }
    }

    companion object {
        private val EMPTY = PushStream<Any?> { }

        @JvmStatic
        fun <T> of(): PushStream<T> {
            @Suppress("UNCHECKED_CAST")
            return EMPTY as PushStream<T>
        }

        @JvmStatic
        fun <T> from(producer: ((T) -> Boolean).() -> Unit): PushStream<T> {
            return PushStream(producer)
        }
    }
}
