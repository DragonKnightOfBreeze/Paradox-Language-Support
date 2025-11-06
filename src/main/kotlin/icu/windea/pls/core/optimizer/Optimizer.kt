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
interface Optimizer<T : Any, R : Any> {
    /**
     * 将输入的对象 [input] 转化为优化后的等价对象。
     */
    fun optimize(input: T): R

    /**
     * 将输入的对象 [input] 转化为反优化后的等价对象。
     */
    fun deoptimize(input: R): T = throw UnsupportedOperationException()

    /**
     * 一元的优化器。
     *
     * 这类优化器不会在处理后更改对象的类型。
     */
    interface Unary<T : Any> : Optimizer<T, T>
}
