package icu.windea.pls.core.optimizer

import icu.windea.pls.core.cast

@Suppress("NOTHING_TO_INLINE") // 3.0.1 can be called many many many times
object OptimizerFactory {
    inline fun <T : Any, R : Any> create(noinline optimizeAction: (input: T) -> R): Optimizer<T, R> {
        return object : Optimizer<T, R> {
            override fun optimize(input: T) = optimizeAction(input)
        }
    }

    inline fun <T : Any, R : Any> create(noinline optimizeAction: (input: T) -> R, noinline deoptimizeAction: (input: R) -> T): Optimizer<T, R> {
        return object : Optimizer<T, R> {
            override fun optimize(input: T) = optimizeAction(input)
            override fun deoptimize(input: R) = deoptimizeAction(input)
        }
    }

    @JvmName("createForByte")
    inline fun <T : Any> create(noinline optimizeAction: (input: T) -> Byte, noinline deoptimizeAction: (input: Byte) -> T): ByteOptimizer<T> {
        return object : ByteOptimizer<T> {
            override fun optimizeByte(input: T) = optimizeAction(input)
            override fun deoptimizeByte(input: Byte) = deoptimizeAction(input)
        }
    }

    inline fun <T : Any, R : Any> get(optimizer: Optimizer<T, R>): Optimizer<T, R> = optimizer

    inline fun <T : Any> get(optimizer: Optimizer.Unary<T>): Optimizer.Unary<T> = optimizer

    inline fun <T : Any> get(optimizer: ByteOptimizer<T>): ByteOptimizer<T> = optimizer

    inline fun <T : Any/*T0*/, T0 : Any> getTyped(optimizer: Optimizer.Unary<T0>): Optimizer.Unary<T> = optimizer.cast()
}
