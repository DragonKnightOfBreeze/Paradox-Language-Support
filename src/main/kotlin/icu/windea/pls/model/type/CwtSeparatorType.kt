package icu.windea.pls.model.type

import icu.windea.pls.core.optimizer.Optimizer
import icu.windea.pls.core.optimizer.OptimizerFactory

enum class CwtSeparatorType(val text: String) {
    Equal("="), // logic equal
    NotEqual("!="), // logic not equal
    DoubleEqual("=="), // matches comparison operators
    ;

    override fun toString() = text

    companion object {
        private val optimizer = OptimizerFactory.create<CwtSeparatorType, Byte>({ it.ordinal.toByte() }, { entries[it.toInt()] })

        @JvmStatic
        fun optimizer(): Optimizer<CwtSeparatorType, Byte> = optimizer
    }
}
