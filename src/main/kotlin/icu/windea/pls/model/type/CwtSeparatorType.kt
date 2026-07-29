package icu.windea.pls.model.type

import icu.windea.pls.core.optimizer.ByteOptimizer
import icu.windea.pls.core.optimizer.OptimizerFactory

enum class CwtSeparatorType(val text: String) {
    Equal("="), // logic equal
    NotEqual("!="), // logic not equal
    DoubleEqual("=="), // matches comparison operators
    ;

    override fun toString() = text

    companion object {
        private val optimizer = OptimizerFactory.create({ it.ordinal.toByte() }, { entries[it.toInt()] })

        @JvmStatic
        fun optimizer(): ByteOptimizer<CwtSeparatorType> = optimizer
    }
}
