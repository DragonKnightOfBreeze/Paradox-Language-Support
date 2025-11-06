@file:Suppress("unused")

package icu.windea.pls.core.optimizer

import it.unimi.dsi.fastutil.Hash
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet
import java.util.*

fun OptimizerRegistry.intern() = register(InternStringOptimizer)
fun <E> OptimizerRegistry.forList() = registerTyped<List<E>, _>(ListOptimizer)
fun <E> OptimizerRegistry.forSet() = registerTyped<Set<E>, _>(SetOptimizer)
fun <K, V> OptimizerRegistry.forMap() = registerTyped<Map<K, V>, _>(MapOptimizer)

private object InternStringOptimizer : Optimizer.Unary<String> {
    override fun optimize(value: String): String {
        return value.intern()
    }
}

private object ListOptimizer : Optimizer.Unary<List<*>> {
    override fun optimize(value: List<*>): List<*> {
        if (value.isEmpty()) return emptyList<Any?>()
        if (ignore(value)) return value
        if (value.size == 1) return applySingleton(value)
        return apply(value)
    }

    private fun ignore(value: List<*>) = false

    /** @see kotlin.collections.toList */
    private fun applySingleton(value: List<*>) = listOf(value.get(0))

    private fun apply(value: List<*>) = value.toImmutableList()
}

private object SetOptimizer : Optimizer.Unary<Set<*>> {
    override fun optimize(value: Set<*>): Set<*> {
        if (value.isEmpty()) return emptySet<Any?>()
        if (ignore(value)) return value
        if (value.size == 1) return applySingleton(value)
        return apply(value)
    }

    private fun ignore(value: Set<*>): Boolean {
        return value is Hash
    }

    /** @see kotlin.collections.toSet */
    private fun applySingleton(value: Set<*>): Set<Any?> = setOf(value.iterator().next())

    private fun apply(value: Set<*>) = value.toImmutableSet()
}

private object MapOptimizer : Optimizer.Unary<Map<*, *>> {
    override fun optimize(value: Map<*, *>): Map<*, *> {
        if (value.isEmpty()) return emptyMap<Any?, Any?>()
        if (ignore(value)) return value
        if (value.size == 1) return applySingleton(value)
        return apply(value)
    }

    private fun ignore(value: Map<*, *>) = value is Hash

    /** @see kotlin.collections.toMap */
    private fun applySingleton(value: Map<*, *>) = with(value.entries.iterator().next()) { Collections.singletonMap(key, value) }

    private fun apply(value: Map<*, *>) = value.toImmutableMap()
}
