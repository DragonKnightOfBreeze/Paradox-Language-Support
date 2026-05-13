package icu.windea.pls.model

import icu.windea.pls.core.optimizer.Optimizer
import icu.windea.pls.core.optimizer.OptimizerFactory
import icu.windea.pls.model.type.CwtSeparatorType
import icu.windea.pls.model.type.CwtExpressionType

fun OptimizerFactory.forCwtType() = get(CwtTypeOptimizer)
fun OptimizerFactory.forCwtSeparatorType() = get(CwtSeparatorTypeOptimizer)
fun OptimizerFactory.forParadoxGameType() = get(ParadoxGameTypeOptimizer)
fun OptimizerFactory.forParadoxDefinitionSource() = get(ParadoxDefinitionSourceOptimizer)
fun OptimizerFactory.forParadoxLocalisationType() = get(ParadoxLocalisationTypeOptimizer)

private object CwtTypeOptimizer : Optimizer<CwtExpressionType, Byte> {
    override fun optimize(input: CwtExpressionType): Byte {
        return input.ordinal.toByte()
    }

    override fun deoptimize(input: Byte): CwtExpressionType {
        return CwtExpressionType.entries[input.toInt()]
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

private object ParadoxGameTypeOptimizer : Optimizer<ParadoxGameType, Byte> {
    override fun optimize(input: ParadoxGameType): Byte {
        return (input.ordinal - 1).toByte()
    }

    override fun deoptimize(input: Byte): ParadoxGameType {
        return ParadoxGameType.getAll(withCore = true)[input.toInt() + 1]
    }
}

private object ParadoxDefinitionSourceOptimizer : Optimizer<ParadoxDefinitionSource, Byte> {
    override fun optimize(input: ParadoxDefinitionSource): Byte {
        return input.ordinal.toByte()
    }

    override fun deoptimize(input: Byte): ParadoxDefinitionSource {
        return ParadoxDefinitionSource.entries[input.toInt()]
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
