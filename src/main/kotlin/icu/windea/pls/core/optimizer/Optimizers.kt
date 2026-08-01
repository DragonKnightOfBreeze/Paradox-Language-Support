@file:Suppress("unused", "NOTHING_TO_INLINE")

package icu.windea.pls.core.optimizer

import com.github.benmanes.caffeine.cache.Interner
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import it.unimi.dsi.fastutil.Hash

fun OptimizerFactory.forString(): Optimizer.Unary<String> = get(StringOptimizer)
fun OptimizerFactory.forStringList(): Optimizer.Unary<List<String>> = get(StringListOptimizer)
fun OptimizerFactory.forStringSet(): Optimizer.Unary<Set<String>> = get(StringSetOptimizer)

fun <E : Any> OptimizerFactory.forList(): Optimizer.Unary<List<E>> = getTyped(ObjectListOptimizer)
fun <E : Any> OptimizerFactory.forSet(): Optimizer.Unary<Set<E>> = getTyped(ObjectSetOptimizer)
fun <K : Any, V : Any> OptimizerFactory.forMap(): Optimizer.Unary<Map<K, V>> = getTyped(ObjectMapOptimizer)

private object StringOptimizer : Optimizer.Unary<String> {
    private val interner = Interner.newWeakInterner<String>()

    override fun optimize(input: String): String {
        if (input.isEmpty()) return ""
        return interner.intern(input)
    }
}

// private val classNameCache = CacheBuilder().build<Class<*>, Boolean> { isOptimizedByClassName(it) }
// private inline fun isOptimizedByClass(input: Any) = classNameCache.get(input.javaClass)
// private inline fun isOptimizedByClassName(c: Class<*>): Boolean {
//     val className = c.name
//     // Java immutable collections
//     if (className.startsWith("java.util.ImmutableCollections$")) return true
//     // Kotlin standard collections may return the JDK's singleton implementation in some cases (e.g., listOf("a") -> java.util.Collections$SingletonList）
//     if (className.startsWith("java.util.Collections$")) return true
//     // Kotlin collections which are immutable
//     if (className.startsWith("kotlin.collections.")) return true
//     // Kotlinx immutable collections, but bad memory
//     // if (className.startsWith("kotlinx.collections.immutable.")) return true
//     return false
// }

private sealed class ListOptimizer<E : Any> : Optimizer.Unary<List<E>> {
    override fun optimize(input: List<E>): List<E> {
        if (input.isEmpty()) return applyForEmpty()
        if (ignore(input)) return input
        return apply(input)
    }

    protected inline fun ignore(input: List<E>): Boolean {
        if (input is ImmutableList) return true // immutable collection (guava) -> skip
        // if (isOptimizedByClass(input)) return true // immutable collection (checked by class name) -> do not skip atm
        return false
    }

    protected inline fun applyForEmpty(): List<E> {
        return ImmutableList.of()
    }

    protected inline fun apply(input: List<E>): List<E> {
        if (input.size == 1) {
            val e = input.get(0)
            return ImmutableList.of(e)
        }
        return ImmutableList.copyOf(input)
    }
}

private sealed class SetOptimizer<E : Any> : Optimizer.Unary<Set<E>> {
    override fun optimize(input: Set<E>): Set<E> {
        if (input.isEmpty()) return applyForEmpty()
        if (ignore(input)) return input
        return apply(input)
    }

    protected inline fun ignore(input: Set<E>): Boolean {
        if (input is ImmutableSet) return true // immutable collection (guava) -> skip
        // if (isOptimizedByClass(input)) return true // immutable collection (checked by class name) -> do not skip atm
        if (input is Hash) return true // may be case-insensitive or using custom hash -> skip
        return false
    }

    protected inline fun applyForEmpty(): Set<E> {
        return ImmutableSet.of()
    }

    protected inline fun apply(input: Set<E>): Set<E> {
        if (input.size == 1) {
            val E = input.iterator().next()
            return ImmutableSet.of(E)
        }
        return ImmutableSet.copyOf(input)
    }
}

private sealed class MapOptimizer<K : Any, V : Any> : Optimizer.Unary<Map<K, V>> {
    override fun optimize(input: Map<K, V>): Map<K, V> {
        if (input.isEmpty()) return applyForEmpty()
        if (ignore(input)) return input
        return apply(input)
    }

    protected inline fun ignore(input: Map<K, V>): Boolean {
        if (input is ImmutableMap) return true // immutable collection (guava) -> skip
        // if (isOptimizedByClass(input)) return true // immutable collection (checked by class name) -> do not skip atm
        if (input is Hash) return true // may be case-insensitive or using custom hash -> skip
        return false
    }

    protected inline fun applyForEmpty(): Map<K, V> {
        return ImmutableMap.of()
    }

    protected inline fun apply(input: Map<K, V>): Map<K, V> {
        if (input.size == 1) {
            val e = input.iterator().next()
            return ImmutableMap.of(e.key, e.value)
        }
        return ImmutableMap.copyOf(input)
    }
}

private object StringListOptimizer : ListOptimizer<String>() {
    private const val threshold = 8
    private val interner = Interner.newWeakInterner<List<String>>()

    override fun optimize(input: List<String>): List<String> {
        if (input.isEmpty()) return applyForEmpty()
        if (ignore(input)) return intern(input)
        return intern(apply(input))
    }

    private inline fun intern(input: List<String>): List<String> {
        if (input.size > threshold) return input
        return interner.intern(input)
    }
}

private object StringSetOptimizer : SetOptimizer<String>() {
    private const val threshold = 8
    private val interner = Interner.newWeakInterner<Set<String>>()

    override fun optimize(input: Set<String>): Set<String> {
        if (input.isEmpty()) return applyForEmpty()
        if (ignore(input)) return intern(input)
        return intern(apply(input))
    }

    private inline fun intern(input: Set<String>): Set<String> {
        if (input.size > threshold) return input
        return interner.intern(input)
    }
}

private object ObjectListOptimizer : ListOptimizer<Any>()

private object ObjectSetOptimizer : SetOptimizer<Any>()

private object ObjectMapOptimizer : MapOptimizer<Any, Any>()
