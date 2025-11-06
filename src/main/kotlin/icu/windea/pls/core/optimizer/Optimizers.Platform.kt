@file:Suppress("UnusedReceiverParameter")

package icu.windea.pls.core.optimizer

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector

fun OptimizerRegistry.forAccess() = register(ReadWriteAccessOptimizer)

private object ReadWriteAccessOptimizer : Optimizer<ReadWriteAccessDetector.Access, Byte> {
    override fun optimize(value: ReadWriteAccessDetector.Access): Byte {
        return value.ordinal.toByte()
    }

    override fun deoptimize(value: Byte): ReadWriteAccessDetector.Access {
        return when (value) {
            0.toByte() -> ReadWriteAccessDetector.Access.Read
            1.toByte() -> ReadWriteAccessDetector.Access.Write
            else -> ReadWriteAccessDetector.Access.ReadWrite
        }
    }
}
