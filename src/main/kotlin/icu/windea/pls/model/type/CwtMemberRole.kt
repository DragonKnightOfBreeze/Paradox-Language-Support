package icu.windea.pls.model.type

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

    // region Inline Methods

    @Suppress("NOTHING_TO_INLINE", "unused")
    inline fun optimized(): Byte = ordinal.toByte() // 3.0.1 radical optimization

    // endregion

    companion object {
        // region Inline Methods

        @Suppress("NOTHING_TO_INLINE", "unused")
        inline fun deoptimized(value: Byte): CwtMemberRole = entries[value.toInt()] // 3.0.1 radical optimization

        // endregion
    }
}
