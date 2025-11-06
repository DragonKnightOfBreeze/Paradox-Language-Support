@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core

import icu.windea.pls.core.optimizer.Optimizer
import icu.windea.pls.core.optimizer.OptimizerRegistry
import icu.windea.pls.core.optimizer.forList
import icu.windea.pls.core.optimizer.forMap
import icu.windea.pls.core.optimizer.forSet
import icu.windea.pls.core.optimizer.forString

/** @see Optimizer.optimize */
inline fun <T, R> T.optimized(optimizer: Optimizer<T, R>): R {
    return optimizer.optimize(this)
}

/** @see Optimizer.optimize */
inline fun <T, R> T.optimized(optimizerProvider: OptimizerRegistry.() -> Optimizer<T, R>): R {
    return OptimizerRegistry.optimizerProvider().optimize(this)
}

/** @see Optimizer.deoptimize */
inline fun <T, R> R.deoptimized(optimizer: Optimizer<T, R>): T {
    return optimizer.deoptimize(this)
}

/** @see Optimizer.deoptimize */
inline fun <T, R> R.deoptimized(optimizerProvider: OptimizerRegistry.() -> Optimizer<T, R>): T {
    return OptimizerRegistry.optimizerProvider().deoptimize(this)
}

/** @see Optimizer.optimize */
inline fun String.optimized() = optimized(OptimizerRegistry.forString())
/** @see Optimizer.optimize */
inline fun <E> List<E>.optimized() = optimized(OptimizerRegistry.forList())
/** @see Optimizer.optimize */
inline fun <E> Set<E>.optimized() = optimized(OptimizerRegistry.forSet())
/** @see Optimizer.optimize */
inline fun <K, V> Map<K, V>.optimized() = optimized(OptimizerRegistry.forMap())
