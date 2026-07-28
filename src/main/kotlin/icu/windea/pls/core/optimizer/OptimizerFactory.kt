package icu.windea.pls.core.optimizer

import icu.windea.pls.core.cast

object OptimizerFactory {
    fun <T : Any, R : Any> create(optimizeAction: (input: T) -> R): Optimizer<T, R> {
        return object : Optimizer<T, R> {
            override fun optimize(input: T) = optimizeAction(input)
        }
    }

    fun <T : Any, R : Any> create(optimizeAction: (input: T) -> R, deoptimizeAction: (input: R) -> T): Optimizer<T, R> {
        return object : Optimizer<T, R> {
            override fun optimize(input: T) = optimizeAction(input)
            override fun deoptimize(input: R) = deoptimizeAction(input)
        }
    }

    fun <T : Any, R : Any> get(optimizer: Optimizer<T, R>): Optimizer<T, R> = optimizer

    fun <T : Any> get(optimizer: Optimizer.Unary<T>): Optimizer.Unary<T> = optimizer

    fun <T : Any/*T0*/, T0 : Any> getTyped(optimizer: Optimizer.Unary<T0>): Optimizer.Unary<T> = optimizer.cast()
}
