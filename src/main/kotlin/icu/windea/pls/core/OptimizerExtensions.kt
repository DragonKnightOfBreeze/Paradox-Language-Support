@file:Suppress("unused", "NOTHING_TO_INLINE")

package icu.windea.pls.core

import icu.windea.pls.core.optimizer.ByteOptimizer
import icu.windea.pls.core.optimizer.Optimizer
import icu.windea.pls.core.optimizer.OptimizerFactory
import icu.windea.pls.core.optimizer.forList
import icu.windea.pls.core.optimizer.forMap
import icu.windea.pls.core.optimizer.forSet
import icu.windea.pls.core.optimizer.forString
import icu.windea.pls.core.optimizer.forStringList
import icu.windea.pls.core.optimizer.forStringSet

/** @see Optimizer.optimize */
inline fun <T : Any, R : Any> T.optimized(optimizer: Optimizer<T, R>): R {
    return optimizer.optimize(this)
}

/** @see Optimizer.deoptimize */
inline fun <T : Any, R : Any> R.deoptimized(optimizer: Optimizer<T, R>): T {
    return optimizer.deoptimize(this)
}

/** @see ByteOptimizer.optimizeByte */
inline fun <T : Any> T.optimized(optimizer: ByteOptimizer<T>): Byte {
    return optimizer.optimizeByte(this)
}

/** @see ByteOptimizer.deoptimizeByte */
inline fun <T : Any> Byte.deoptimized(optimizer: ByteOptimizer<T>): T {
    return optimizer.deoptimizeByte(this)
}

/** @see OptimizerFactory.forString */
@JvmName("optimizedForString")
inline fun String.optimized(): String = optimized(OptimizerFactory.forString())
/** @see OptimizerFactory.forStringList */
@JvmName("optimizedForStringList")
inline fun List<String>.optimized(): List<String> = optimized(OptimizerFactory.forStringList())
/** @see OptimizerFactory.forStringSet */
@JvmName("optimizedForStringSet")
inline fun Set<String>.optimized(): Set<String> = optimized(OptimizerFactory.forStringSet())
/** @see OptimizerFactory.forList */
@JvmName("optimizedForList")
inline fun <E : Any> List<E>.optimized(): List<E> = optimized(OptimizerFactory.forList())
/** @see OptimizerFactory.forSet */
@JvmName("optimizedForSet")
inline fun <E : Any> Set<E>.optimized(): Set<E> = optimized(OptimizerFactory.forSet())
/** @see OptimizerFactory.forMap */
@JvmName("optimizedForMap")
inline fun <K : Any, V : Any> Map<K, V>.optimized(): Map<K, V> = optimized(OptimizerFactory.forMap())

inline fun <E> List<E>.optimizedIfEmpty() = ifEmpty { emptyList() }

inline fun <E> Set<E>.optimizedIfEmpty() = ifEmpty { emptySet() }

inline fun <K, V> Map<K, V>.optimizedIfEmpty() = ifEmpty { emptyMap() }
