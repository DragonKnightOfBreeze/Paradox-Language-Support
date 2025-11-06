@file:Suppress("UnusedReceiverParameter")

package icu.windea.pls.core.optimizer

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector

fun OptimizerRegistry.forAccess() = register(ReadWriteAccessOptimizer)

private object ReadWriteAccessOptimizer : Optimizer<ReadWriteAccessDetector.Access, Byte> {
    override fun optimize(input: ReadWriteAccessDetector.Access): Byte {
        return input.ordinal.toByte()
    }

    override fun deoptimize(input: Byte): ReadWriteAccessDetector.Access {
        return when (input) {
            0.toByte() -> ReadWriteAccessDetector.Access.Read
            1.toByte() -> ReadWriteAccessDetector.Access.Write
            else -> ReadWriteAccessDetector.Access.ReadWrite
        }
    }
}
