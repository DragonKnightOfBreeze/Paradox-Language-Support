package icu.windea.pls.core.optimizer

import icu.windea.pls.core.cast

object OptimizerFactory {
    @Suppress("NOTHING_TO_INLINE")
    inline fun <T : Any, R : Any> get(optimizer: Optimizer<T, R>): Optimizer<T, R> = optimizer

    @Suppress("NOTHING_TO_INLINE")
    inline fun <T : Any> get(optimizer: Optimizer.Unary<T>): Optimizer.Unary<T> = optimizer

    @Suppress("NOTHING_TO_INLINE")
    inline fun <T : Any/*T0*/, T0 : Any> getTyped(optimizer: Optimizer.Unary<T0>): Optimizer.Unary<T> = optimizer.cast()
}
