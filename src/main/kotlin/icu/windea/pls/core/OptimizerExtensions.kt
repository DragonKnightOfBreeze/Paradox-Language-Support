@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core

import icu.windea.pls.core.optimizer.Optimizer
import icu.windea.pls.core.optimizer.OptimizerRegistry
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

/** @see Optimizer.optimize */
inline fun <T : Any, R : Any> T.optimized(optimizerProvider: OptimizerRegistry.() -> Optimizer<T, R>): R {
    return OptimizerRegistry.optimizerProvider().optimize(this)
}

/** @see Optimizer.deoptimize */
inline fun <T : Any, R : Any> R.deoptimized(optimizer: Optimizer<T, R>): T {
    return optimizer.deoptimize(this)
}

/** @see Optimizer.deoptimize */
inline fun <T : Any, R : Any> R.deoptimized(optimizerProvider: OptimizerRegistry.() -> Optimizer<T, R>): T {
    return OptimizerRegistry.optimizerProvider().deoptimize(this)
}

/** @see OptimizerRegistry.forString */
inline fun String.optimized() = optimized(OptimizerRegistry.forString())
/** @see OptimizerRegistry.forStringList */
inline fun List<String>.optimized() = optimized(OptimizerRegistry.forStringList())
/** @see OptimizerRegistry.forStringSet */
inline fun Set<String>.optimized() = optimized(OptimizerRegistry.forStringSet())
/** @see OptimizerRegistry.forList */
inline fun <E : Any> List<E>.optimized() = optimized(OptimizerRegistry.forList())
/** @see OptimizerRegistry.forSet */
inline fun <E : Any> Set<E>.optimized() = optimized(OptimizerRegistry.forSet())
/** @see OptimizerRegistry.forMap */
inline fun <K : Any, V : Any> Map<K, V>.optimized() = optimized(OptimizerRegistry.forMap())
