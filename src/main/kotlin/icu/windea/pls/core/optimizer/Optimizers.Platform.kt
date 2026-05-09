package icu.windea.pls.core.optimizer

import icu.windea.pls.core.ReadWriteAccess

fun OptimizerFactory.forReadWriteAccess() = get(ReadWriteAccessOptimizer)

private object ReadWriteAccessOptimizer : Optimizer<ReadWriteAccess, Byte> {
    override fun optimize(input: ReadWriteAccess): Byte {
        return input.ordinal.toByte()
    }

    override fun deoptimize(input: Byte): ReadWriteAccess {
        return when (input) {
            0.toByte() -> ReadWriteAccess.Read
            1.toByte() -> ReadWriteAccess.Write
            else -> ReadWriteAccess.ReadWrite
        }
    }
}
