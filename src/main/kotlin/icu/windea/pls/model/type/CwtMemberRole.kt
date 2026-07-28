package icu.windea.pls.model.type

import icu.windea.pls.core.optimizer.Optimizer
import icu.windea.pls.core.optimizer.OptimizerFactory
import icu.windea.pls.cwt.psi.CwtMember

/**
 * @see CwtMember
 */
enum class CwtMemberRole(val text: String) {
    Property("property"),
    PropertyValue("property_value"),
    DirectValue("direct_value"),
    OptionValue("option_value"),
    OptionDirectValue("option_direct_value"),
    Other("(other)"),
    ;

    override fun toString() = text

    companion object {
        private val optimizer = OptimizerFactory.create<CwtMemberRole, Byte>({ it.ordinal.toByte() }, { entries[it.toInt()] })

        @JvmStatic
        fun optimizer(): Optimizer<CwtMemberRole, Byte> = optimizer
    }
}
