package icu.windea.pls.model

import icu.windea.pls.core.optimizer.Optimizer
import icu.windea.pls.core.optimizer.OptimizerFactory

/**
 * 定义的来源。
 */
enum class ParadoxDefinitionSource {
    File,
    Property,
    Inline,
    Injection,
    ;

    companion object {
        private val optimizer = OptimizerFactory.create<ParadoxDefinitionSource, Byte>({ it.ordinal.toByte() }, { entries[it.toInt()] })

        @JvmStatic
        fun optimizer(): Optimizer<ParadoxDefinitionSource, Byte> = optimizer
    }
}
