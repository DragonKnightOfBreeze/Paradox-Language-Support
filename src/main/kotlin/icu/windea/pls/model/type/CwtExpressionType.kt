package icu.windea.pls.model.type

import icu.windea.pls.core.optimizer.ByteOptimizer
import icu.windea.pls.core.optimizer.OptimizerFactory
import icu.windea.pls.cwt.psi.CwtExpressionElement

/**
 * @see CwtExpressionElement
 */
enum class CwtExpressionType(val text: String) {
    Unknown("(unknown)"),
    Boolean("boolean"),
    Int("int"),
    Float("float"),
    String("string"),
    Block("block"),
    ;

    override fun toString() = text

    companion object {
        private val optimizer = OptimizerFactory.create({ it.ordinal.toByte() }, { entries[it.toInt()] })

        @JvmStatic
        fun optimizer(): ByteOptimizer<CwtExpressionType> = optimizer
    }
}
