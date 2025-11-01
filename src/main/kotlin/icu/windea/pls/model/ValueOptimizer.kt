package icu.windea.pls.model

sealed interface ValueOptimizer<T, R> {
    fun optimize(value: T): R

    fun deoptimize(value: R): T
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T, R> T.optimized(optimizer: ValueOptimizer<T, R>): R = optimizer.optimize(this)

@Suppress("NOTHING_TO_INLINE")
inline  fun <T, R> R.deoptimized(optimizer: ValueOptimizer<T, R>): T = optimizer.deoptimize(this)
