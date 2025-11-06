@file:Suppress("NOTHING_TO_INLINE", "unused")

package icu.windea.pls.core.optimizer

import com.github.benmanes.caffeine.cache.Interner
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSet
import it.unimi.dsi.fastutil.Hash
import java.util.*

fun OptimizerRegistry.forString() = register(StringOptimizer)
fun <E> OptimizerRegistry.forList() = registerTyped<List<E>, _>(ListOptimizer)
fun <E> OptimizerRegistry.forSet() = registerTyped<Set<E>, _>(SetOptimizer)
fun <K, V> OptimizerRegistry.forMap() = registerTyped<Map<K, V>, _>(MapOptimizer)

private object StringOptimizer : Optimizer.Unary<String> {
    private val interner = Interner.newWeakInterner<String>()

    override fun optimize(input: String): String {
        return interner.intern(input)
    }
}

private object ListOptimizer : Optimizer.Unary<List<*>> {
    override fun optimize(input: List<*>): List<*> {
        if (input.isEmpty()) return applyForEmpty()
        if (ignore(input)) return input
        return apply(input)
    }

    private inline fun ignore(input: List<*>): Boolean {
        return input is ImmutableList
    }

    private inline fun applyForEmpty(): List<Any?> {
        return emptyList()
    }

    private inline fun apply(input: List<*>): List<Any?> {
        if (input.size == 1) return listOf(input.get(0))
        return ImmutableList.copyOf(input)
    }
}

private object SetOptimizer : Optimizer.Unary<Set<*>> {
    override fun optimize(input: Set<*>): Set<*> {
        if (input.isEmpty()) return applyForEmpty()
        if (ignore(input)) return input
        return apply(input)
    }

    private inline fun ignore(input: Set<*>): Boolean {
        return input is ImmutableSet || input is Hash
    }

    private inline fun applyForEmpty(): Set<Any?> {
        return emptySet()
    }

    private inline fun apply(input: Set<*>): Set<Any?> {
        return ImmutableSet.copyOf(input)
    }
}

private object MapOptimizer : Optimizer.Unary<Map<*, *>> {
    override fun optimize(input: Map<*, *>): Map<*, *> {
        if (input.isEmpty()) return applyForEmpty()
        if (ignore(input)) return input
        return apply(input)
    }

    private inline fun ignore(input: Map<*, *>): Boolean {
        return input is ImmutableMap || input is Hash
    }

    private inline fun applyForEmpty(): Map<Any?, Any?> {
        return emptyMap()
    }

    private inline fun apply(input: Map<*, *>): Map<Any?, Any?> {
        return ImmutableMap.copyOf(input)
    }
}
