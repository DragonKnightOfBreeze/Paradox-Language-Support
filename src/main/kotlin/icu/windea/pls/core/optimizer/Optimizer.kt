package icu.windea.pls.core.optimizer

/**
 * 优化器。
 *
 * 用于通过使用缓存、转化为单例或者占用更少内存的等价对象等方式，优化各种对象。
 *
 * 说明：
 * - 可能会在处理后返回自身，或者仅修改自身的字段。
 * - 可能会在处理后返回同类型或不同类型的等价对象。
 *
 * @see OptimizerRegistry
 */
interface Optimizer<T, R> {
    /**
     * 将 [value] 转化为优化后的等价对象。
     */
    fun optimize(value: T): R

    /**
     * [value] 转化为反优化后的等价对象。
     */
    fun deoptimize(value: R): T = throw UnsupportedOperationException()

    interface Unary<T> : Optimizer<T, T>
}
