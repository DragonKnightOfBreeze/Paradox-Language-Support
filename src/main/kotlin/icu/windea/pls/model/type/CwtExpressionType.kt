package icu.windea.pls.model.type

import icu.windea.pls.core.optimizer.Optimizer
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
        private val optimizer = OptimizerFactory.create<CwtExpressionType, Byte>({ it.ordinal.toByte() }, { entries[it.toInt()] })

        @JvmStatic
        fun optimizer(): Optimizer<CwtExpressionType, Byte> = optimizer
    }
}
