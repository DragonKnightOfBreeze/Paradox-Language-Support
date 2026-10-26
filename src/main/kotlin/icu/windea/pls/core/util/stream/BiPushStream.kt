@file:Suppress("unused")

package icu.windea.pls.core.util.stream

import com.intellij.util.Processor
import java.util.stream.Stream

/**
 * 轻量级的推流，每次接收两个元素。
 *
 * ## API 笔记
 *
 * 此类推流（push stream）是一种基于 Push 模型的（近似的）零成本抽象（zero-cost abstraction）。
 * 它们不需要额外的中间操作，也不会引入状态机与堆内存分配，因此不存在额外的性能开销。
 *
 * 相比常见的拉流（pull stream），例如 [Iterator] 和 [Sequence]，
 * 由于可以直接依赖原生调用栈，且不存在状态持久化的核心开销，
 * 在有着极高的时间复杂度或者极高的元素数量的场景下，它们通常能取得更好的性能表现。
 *
 * 相比平台代码中常见的处理器模式（processor pattern），即使用 [Processor] 作为参数以进行可中断迭代的方式，
 * 由于可以直接进行链式调用，且不需要编写违反直觉且不够直观的回调代码，
 * 在能够保持性能的同时，它们也能取得更好的可读性。
 *
 * ## 实现笔记
 *
 * 使用内联类（inline class）实现，从而避免额外的包装对象开销，同时也避免引入过多的扩展函数。
 * 尽管仍然存在闭包对象创建和虚函数调用带来的开销，在大多数情况下，预期可以在运行时被 JIT 优化。
 *
 * @see Stream
 * @see Iterable
 * @see Iterator
 * @see Sequence
 * @see Processor
 */
@JvmInline
value class BiPushStream<out T1, out T2>(val dispatch: ((T1, T2) -> Boolean) -> Unit) {
    // Termination operation

    inline fun forEach(crossinline action: (T1, T2) -> Unit) {
        dispatch { e1, e2 ->
            action(e1, e2)
            true
        }
    }

    inline fun process(crossinline processor: (T1, T2) -> Boolean): Boolean {
        var result = true
        dispatch { e1, e2 ->
            if (!processor(e1, e2)) {
                result = false
                false
            } else {
                true
            }
        }
        return result
    }

    inline fun any(crossinline predicate: (T1, T2) -> Boolean): Boolean {
        var result = false
        dispatch { e1, e2 ->
            if (predicate(e1, e2)) {
                result = true
                false
            } else {
                true
            }
        }
        return result
    }

    inline fun all(crossinline predicate: (T1, T2) -> Boolean): Boolean {
        var result = true
        dispatch { e1, e2 ->
            if (!predicate(e1, e2)) {
                result = false
                false
            } else {
                true
            }
        }
        return result
    }

    inline fun none(crossinline predicate: (T1, T2) -> Boolean): Boolean {
        return !any(predicate)
    }

    inline fun <R> firstNotNullOrOrNull(crossinline transform: (T1, T2) -> R): R? {
        var result: R? = null
        dispatch { e1, e2 ->
            val r = transform(e1, e2)
            if (r != null) {
                result = r
                false
            } else {
                true
            }
        }
        return result
    }

    fun firstOrNull(): Pair<T1, T2>? {
        var result: Pair<T1, T2>? = null
        dispatch { e1, e2 ->
            result = e1 to e2
            false
        }
        return result
    }

    inline fun firstOrNull(crossinline predicate: (T1, T2) -> Boolean): Pair<T1, T2>? {
        var result: Pair<T1, T2>? = null
        dispatch { e1, e2 ->
            if (predicate(e1, e2)) {
                result = e1 to e2
                false
            } else {
                true
            }
        }
        return result
    }

    inline fun find(crossinline predicate: (T1, T2) -> Boolean): Pair<T1, T2>? {
        return firstOrNull(predicate)
    }

    // Intermediate operations

    inline fun <R> map(crossinline transform: (T1, T2) -> R): PushStream<R> {
        return PushStream { downstream ->
            dispatch { e1, e2 ->
                downstream(transform(e1, e2))
            }
        }
    }

    inline fun <R : Any> mapNotNull(crossinline transform: (T1, T2) -> R?): PushStream<R> {
        return PushStream { downstream ->
            dispatch { e1, e2 ->
                val r = transform(e1, e2)
                if (r != null) downstream(r) else true
            }
        }
    }

    inline fun filter(crossinline predicate: (T1, T2) -> Boolean): BiPushStream<T1, T2> {
        return BiPushStream { downstream ->
            dispatch { e1, e2 ->
                if (predicate(e1, e2)) downstream(e1, e2) else true
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun filterNotNull(): BiPushStream<T1 & Any, T2 & Any> {
        return filter { e1, e2 -> e1 != null && e2 != null } as BiPushStream<T1 & Any, T2 & Any>
    }

    fun take(count: Int): BiPushStream<T1, T2> {
        if (count <= 0) return of()
        return BiPushStream { downstream ->
            var remaining = count
            dispatch { e1, e2 ->
                val continueNext = downstream(e1, e2)
                remaining--
                continueNext && remaining > 0
            }
        }
    }

    // Conversion operations

    fun toOne(): Pair<T1, T2>? {
        return firstOrNull()
    }

    fun toList(): List<Pair<T1, T2>> {
        val s = this
        return buildList { s.forEach { e1, e2 -> add(e1 to e2) } }
    }

    fun toSet(): Set<Pair<T1, T2>> {
        val s = this
        return buildSet { s.forEach { e1, e2 -> add(e1 to e2) } }
    }

    companion object {
        private val EMPTY = BiPushStream<Any?, Any?> { }

        @JvmStatic
        fun <T1, T2> of(): BiPushStream<T1, T2> {
            @Suppress("UNCHECKED_CAST")
            return EMPTY as BiPushStream<T1, T2>
        }

        @JvmStatic
        fun <T1, T2> of(vararg elements: Pair<T1, T2>): BiPushStream<T1, T2> {
            if (elements.isEmpty()) {
                @Suppress("UNCHECKED_CAST")
                return EMPTY as BiPushStream<T1, T2>
            }
            return from { for ((e1, e2) in elements) if (!invoke(e1, e2)) break }
        }

        @JvmStatic
        fun <T1, T2> of(source: Iterable<Pair<T1, T2>>): BiPushStream<T1, T2> {
            return from { for ((e1, e2) in source) if (!invoke(e1, e2)) break }
        }

        @JvmStatic
        fun <T1, T2> of(source: Iterator<Pair<T1, T2>>): BiPushStream<T1, T2> {
            return from {
                while (source.hasNext()) {
                    val (e1, e2) = source.next()
                    if (!invoke(e1, e2)) break
                }
            }
        }

        @JvmStatic
        fun <T1, T2> of(source: Sequence<Pair<T1, T2>>): BiPushStream<T1, T2> {
            return from { for ((e1, e2) in source) if (!invoke(e1, e2)) break }
        }

        @JvmStatic
        fun <T1, T2> from(producer: ((T1, T2) -> Boolean).() -> Unit): BiPushStream<T1, T2> {
            return BiPushStream(producer)
        }
    }
}
