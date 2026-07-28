package icu.windea.pls.model.type

import icu.windea.pls.core.optimizer.Optimizer
import icu.windea.pls.core.optimizer.OptimizerFactory
import icu.windea.pls.cwt.psi.CwtExpressionElement

/**
 * @see CwtExpressionElement
 */
enum class CwtExpressionRole(val text: String) {
    Key("key"),
    Value("value"),
    Other("(other)"),
    ;

    override fun toString() = text

    @Suppress("unused")
    fun isKey() = this == Key

    @Suppress("unused")
    fun isValue() = this == Value

    @Suppress("unused")
    fun toBoolean(): Boolean? = if (this == Key) true else if (this === Value) false else null

    companion object {
        private val optimizer = OptimizerFactory.create<CwtExpressionRole, Byte>({ it.ordinal.toByte() }, { entries[it.toInt()] })

        @JvmStatic
        fun optimizer(): Optimizer<CwtExpressionRole, Byte> = optimizer

        @Suppress("unused")
        @JvmStatic
        fun fromBoolean(value: Boolean?): CwtExpressionRole = if (value == true) Key else if (value == false) Value else Other
    }
}
