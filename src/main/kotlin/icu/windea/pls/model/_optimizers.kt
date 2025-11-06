package icu.windea.pls.model

import icu.windea.pls.core.optimizer.Optimizer
import icu.windea.pls.core.optimizer.OptimizerRegistry

fun OptimizerRegistry.forCwtType() = register(CwtTypeOptimizer)
fun OptimizerRegistry.forCwtSeparatorType() = register(CwtSeparatorTypeOptimizer)
fun OptimizerRegistry.forGameType() = register(ParadoxGameTypeOptimizer)
fun OptimizerRegistry.forLocalisationType() = register(ParadoxLocalisationTypeOptimizer)

private object CwtTypeOptimizer : Optimizer<CwtType, Byte> {
    override fun optimize(value: CwtType): Byte {
        return value.ordinal.toByte()
    }

    override fun deoptimize(value: Byte): CwtType {
        return CwtType.entries[value.toInt()]
    }
}

private object CwtSeparatorTypeOptimizer : Optimizer<CwtSeparatorType, Byte> {
    override fun optimize(value: CwtSeparatorType): Byte {
        return value.ordinal.toByte()
    }

    override fun deoptimize(value: Byte): CwtSeparatorType {
        return CwtSeparatorType.entries[value.toInt()]
    }
}

private object ParadoxGameTypeOptimizer : Optimizer<ParadoxGameType, Byte> {
    override fun optimize(value: ParadoxGameType): Byte {
        return (value.ordinal - 1).toByte()
    }

    override fun deoptimize(value: Byte): ParadoxGameType {
        return ParadoxGameType.getAll(withCore = true)[value.toInt() + 1]
    }
}

private object ParadoxLocalisationTypeOptimizer : Optimizer<ParadoxLocalisationType, Byte> {
    override fun optimize(value: ParadoxLocalisationType): Byte {
        return value.ordinal.toByte()
    }

    override fun deoptimize(value: Byte): ParadoxLocalisationType {
        return ParadoxLocalisationType.entries[value.toInt()]
    }
}
