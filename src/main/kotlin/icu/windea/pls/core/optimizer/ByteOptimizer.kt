package icu.windea.pls.core.optimizer

// 3.0.1 optimize: to avoid boxing

/**
 *使用 [Byte] 作为目标类型的优化器。
 *
 * 用于避免自动装箱带来的额外性能开销。
 */
interface ByteOptimizer<T : Any> : Optimizer<T, Byte> {
    override fun optimize(input: T): Byte = optimizeByte(input)

    fun optimizeByte(input: T): Byte

    override fun deoptimize(input: Byte): T = deoptimizeByte(input)

    fun deoptimizeByte(input: Byte): T = throw UnsupportedOperationException()
}
