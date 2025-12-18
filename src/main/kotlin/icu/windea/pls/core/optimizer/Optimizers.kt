@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.optimizer

import com.github.benmanes.caffeine.cache.Interner
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.cache.CacheBuilder
import it.unimi.dsi.fastutil.Hash
import java.util.*

fun OptimizerRegistry.forString() = register(StringOptimizer)
fun OptimizerRegistry.forStringList() = register(StringListOptimizer)
fun OptimizerRegistry.forStringSet() = register(StringSetOptimizer)
fun <E : Any> OptimizerRegistry.forList() = registerTyped<List<E>, _>(ListOptimizer)
fun <E : Any> OptimizerRegistry.forSet() = registerTyped<Set<E>, _>(SetOptimizer)
fun <K : Any, V : Any> OptimizerRegistry.forMap() = registerTyped<Map<K, V>, _>(MapOptimizer)

private val stringInterner = Interner.newWeakInterner<String>()
private inline fun String.internString() = stringInterner.intern(this)

private const val SMALL_INTERN_THRESHOLD = 8

private inline fun isOptimizedByClass(input: Any) = classNameCache.get(input.javaClass)
private val classNameCache = CacheBuilder().build<Class<*>, Boolean> { isOptimizedByClassName(it) }
private inline fun isOptimizedByClassName(c: Class<*>): Boolean {
    val className = c.name
    if (className.startsWith("java.util.ImmutableCollections$")) return true
    // Kotlin 标准集合在某些情况下会返回 JDK 的单例实现（例如 listOf("a") -> java.util.Collections$SingletonList）
    if (className.startsWith("java.util.Collections$")) return true
    if (className.startsWith("kotlin.collections.")) return true
    // if(className.startsWith("kotlinx.collections.immutable.")) return true // bad memory
    return false
}

private object StringOptimizer : Optimizer.Unary<String> {
    override fun optimize(input: String): String {
        if (input.isEmpty()) return ""
        return input.internString()
    }
}

private object StringListOptimizer : Optimizer.Unary<List<String>> {
    private val interner = Interner.newWeakInterner<List<String>>()

    override fun optimize(input: List<String>): List<String> {
        if (input.isEmpty()) return emptyList()
        if (input.size > SMALL_INTERN_THRESHOLD) return input
        return interner.intern(input)
    }
}

private object StringSetOptimizer : Optimizer.Unary<Set<String>> {
    private val interner = Interner.newWeakInterner<Set<String>>()

    override fun optimize(input: Set<String>): Set<String> {
        if (input.isEmpty()) return emptySet()
        if (input.size > SMALL_INTERN_THRESHOLD) return input
        return interner.intern(input)
    }
}

private object ListOptimizer : Optimizer.Unary<List<Any>> {
    override fun optimize(input: List<Any>): List<Any> {
        if (input.isEmpty()) return applyForEmpty()
        if (ignore(input)) return input
        return apply(input)
    }

    private inline fun ignore(input: List<Any>): Boolean {
        if (input is ImmutableList) return true
        if (PlsFacade.Capacities.relaxOptimize()) {
            if (isOptimizedByClass(input)) return true
        }
        return false
    }

    private inline fun applyForEmpty(): List<Any> {
        return emptyList()
    }

    private inline fun apply(input: List<Any>): List<Any> {
        if (input.size == 1) return ImmutableList.of(input.get(0))
        return ImmutableList.copyOf(input)
    }
}

private object SetOptimizer : Optimizer.Unary<Set<Any>> {
    override fun optimize(input: Set<Any>): Set<Any> {
        if (input.isEmpty()) return applyForEmpty()
        if (ignore(input)) return input
        return apply(input)
    }

    private inline fun ignore(input: Set<Any>): Boolean {
        if (input is ImmutableSet) return true
        if (PlsFacade.Capacities.relaxOptimize()) {
            if (isOptimizedByClass(input)) return true
        }
        if (input is Hash) return true // may be case-insensitive or custom hash
        return false
    }

    private inline fun applyForEmpty(): Set<Any> {
        return emptySet()
    }

    private inline fun apply(input: Set<Any>): Set<Any> {
        return ImmutableSet.copyOf(input)
    }
}

private object MapOptimizer : Optimizer.Unary<Map<Any, Any>> {
    override fun optimize(input: Map<Any, Any>): Map<Any, Any> {
        if (input.isEmpty()) return applyForEmpty()
        if (ignore(input)) return input
        return apply(input)
    }

    private inline fun ignore(input: Map<*, Any>): Boolean {
        if (input is ImmutableMap) return true
        if (PlsFacade.Capacities.relaxOptimize()) {
            if (isOptimizedByClass(input)) return true
        }
        if (input is Hash) return true // may be case-insensitive or custom hash
        return false
    }

    private inline fun applyForEmpty(): Map<Any, Any> {
        return emptyMap()
    }

    private inline fun apply(input: Map<Any, Any>): Map<Any, Any> {
        return ImmutableMap.copyOf(input)
    }
}
