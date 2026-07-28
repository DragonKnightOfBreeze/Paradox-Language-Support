package icu.windea.pls.model.type

import icu.windea.pls.core.optimizer.Optimizer
import icu.windea.pls.core.optimizer.OptimizerFactory
import icu.windea.pls.script.psi.ParadoxScriptMember

/**
 * @see ParadoxScriptMember
 */
enum class ParadoxMemberRole(val text: String) {
    Property("property"),
    DirectValue("direct_value"),
    PropertyValue("property_value"),
    ScriptedVariableValue("scripted_variable_value"),
    Other("(other)"),
    ;

    override fun toString() = text

    companion object {
        private val optimizer = OptimizerFactory.create<ParadoxMemberRole, Byte>({ it.ordinal.toByte() }, { entries[it.toInt()] })

        @JvmStatic
        fun optimizer(): Optimizer<ParadoxMemberRole, Byte> = optimizer
    }
}
