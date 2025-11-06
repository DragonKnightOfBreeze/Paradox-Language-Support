package icu.windea.pls.core.optimizer

import icu.windea.pls.core.cast

/**
 * 优化器的注册表。
 */
object OptimizerRegistry {
    @Suppress("NOTHING_TO_INLINE")
    inline fun <T : Any, R : Any> register(optimizer: Optimizer<T, R>): Optimizer<T, R> = optimizer

    @Suppress("NOTHING_TO_INLINE")
    inline fun <T : Any> register(optimizer: Optimizer.Unary<T>): Optimizer.Unary<T> = optimizer

    @Suppress("NOTHING_TO_INLINE")
    inline fun <T : T0, T0 : Any> registerTyped(optimizer: Optimizer.Unary<T0>): Optimizer.Unary<T> = optimizer.cast()
}
