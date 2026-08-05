package icu.windea.pls.model.type

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

    // region Inline Methods

    @Suppress("NOTHING_TO_INLINE", "unused")
    inline fun optimized(): Byte = ordinal.toByte() // 3.0.1 radical optimization

    // endregion

    companion object {
        // region Inline Methods

        @Suppress("NOTHING_TO_INLINE", "unused")
        inline fun deoptimized(value: Byte): ParadoxMemberRole = entries[value.toInt()] // 3.0.1 radical optimization

        // endregion
    }
}
