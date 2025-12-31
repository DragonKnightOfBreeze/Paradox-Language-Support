package icu.windea.pls.model

import icu.windea.pls.core.optimizer.Optimizer
import icu.windea.pls.core.optimizer.OptimizerRegistry

fun OptimizerRegistry.forCwtType() = register(CwtTypeOptimizer)
fun OptimizerRegistry.forCwtSeparatorType() = register(CwtSeparatorTypeOptimizer)
// fun OptimizerRegistry.forCwtMemberType() = register(CwtMemberTypeOptimizer)
fun OptimizerRegistry.forGameType() = register(ParadoxGameTypeOptimizer)
fun OptimizerRegistry.forLocalisationType() = register(ParadoxLocalisationTypeOptimizer)

private object CwtTypeOptimizer : Optimizer<CwtType, Byte> {
    override fun optimize(input: CwtType): Byte {
        return input.ordinal.toByte()
    }

    override fun deoptimize(input: Byte): CwtType {
        return CwtType.entries[input.toInt()]
    }
}

private object CwtSeparatorTypeOptimizer : Optimizer<CwtSeparatorType, Byte> {
    override fun optimize(input: CwtSeparatorType): Byte {
        return input.ordinal.toByte()
    }

    override fun deoptimize(input: Byte): CwtSeparatorType {
        return CwtSeparatorType.entries[input.toInt()]
    }
}

// private object CwtMemberTypeOptimizer : Optimizer<CwtMemberType, Byte> {
//     override fun optimize(input: CwtMemberType): Byte {
//         return input.ordinal.toByte()
//     }
//
//     override fun deoptimize(input: Byte): CwtMemberType {
//         return CwtMemberType.entries[input.toInt()]
//     }
// }

private object ParadoxGameTypeOptimizer : Optimizer<ParadoxGameType, Byte> {
    override fun optimize(input: ParadoxGameType): Byte {
        return (input.ordinal - 1).toByte()
    }

    override fun deoptimize(input: Byte): ParadoxGameType {
        return ParadoxGameType.getAll(withCore = true)[input.toInt() + 1]
    }
}

private object ParadoxLocalisationTypeOptimizer : Optimizer<ParadoxLocalisationType, Byte> {
    override fun optimize(input: ParadoxLocalisationType): Byte {
        return input.ordinal.toByte()
    }

    override fun deoptimize(input: Byte): ParadoxLocalisationType {
        return ParadoxLocalisationType.entries[input.toInt()]
    }
}
