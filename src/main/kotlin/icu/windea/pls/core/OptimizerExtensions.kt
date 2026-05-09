@file:Suppress("NOTHING_TO_INLINE")

package icu.windea.pls.core

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

/** @see Optimizer.optimize */
inline fun <T : Any, R : Any> T.optimized(optimizerProvider: OptimizerFactory.() -> Optimizer<T, R>): R {
    return OptimizerFactory.optimizerProvider().optimize(this)
}

/** @see Optimizer.deoptimize */
inline fun <T : Any, R : Any> R.deoptimized(optimizer: Optimizer<T, R>): T {
    return optimizer.deoptimize(this)
}

/** @see Optimizer.deoptimize */
inline fun <T : Any, R : Any> R.deoptimized(optimizerProvider: OptimizerFactory.() -> Optimizer<T, R>): T {
    return OptimizerFactory.optimizerProvider().deoptimize(this)
}

/** @see OptimizerFactory.forString */
@JvmName("optimizedForString")
inline fun String.optimized() = optimized(OptimizerFactory.forString())
/** @see OptimizerFactory.forStringList */
@JvmName("optimizedForStringList")
inline fun List<String>.optimized() = optimized(OptimizerFactory.forStringList())
/** @see OptimizerFactory.forStringSet */
@JvmName("optimizedForStringSet")
inline fun Set<String>.optimized() = optimized(OptimizerFactory.forStringSet())
/** @see OptimizerFactory.forList */
@JvmName("optimizedForList")
inline fun <E : Any> List<E>.optimized() = optimized(OptimizerFactory.forList())
/** @see OptimizerFactory.forSet */
@JvmName("optimizedForSet")
inline fun <E : Any> Set<E>.optimized() = optimized(OptimizerFactory.forSet())
/** @see OptimizerFactory.forMap */
@JvmName("optimizedForMap")
inline fun <K : Any, V : Any> Map<K, V>.optimized() = optimized(OptimizerFactory.forMap())
