package icu.windea.pls.model

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector

object ValueOptimizers {
    object ForCwtType : ValueOptimizer<CwtType, Byte> {
        override fun optimize(value: CwtType): Byte {
            return value.ordinal.toByte()
        }

        override fun deoptimize(value: Byte): CwtType {
            return CwtType.entries[value.toInt()]
        }
    }

    object ForCwtSeparatorType : ValueOptimizer<CwtSeparatorType, Byte> {
        override fun optimize(value: CwtSeparatorType): Byte {
            return value.ordinal.toByte()
        }

        override fun deoptimize(value: Byte): CwtSeparatorType {
            return CwtSeparatorType.entries[value.toInt()]
        }
    }

    object ForParadoxGameType : ValueOptimizer<ParadoxGameType, Byte> {
        override fun optimize(value: ParadoxGameType): Byte {
            return (value.ordinal - 1).toByte()
        }

        override fun deoptimize(value: Byte): ParadoxGameType {
            return ParadoxGameType.getAll(withCore = true)[value.toInt() + 1]
        }
    }

    object ForParadoxLocalisationType : ValueOptimizer<ParadoxLocalisationType, Byte> {
        override fun optimize(value: ParadoxLocalisationType): Byte {
            return value.ordinal.toByte()
        }

        override fun deoptimize(value: Byte): ParadoxLocalisationType {
            return ParadoxLocalisationType.entries[value.toInt()]
        }
    }

    object ForAccess : ValueOptimizer<ReadWriteAccessDetector.Access, Byte> {
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
}
