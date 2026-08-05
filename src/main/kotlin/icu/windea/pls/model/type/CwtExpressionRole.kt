package icu.windea.pls.model.type

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

    // region Inline Methods

    @Suppress("NOTHING_TO_INLINE", "unused")
    inline fun optimized(): Byte = ordinal.toByte() // 3.0.1 radical optimization

    @Suppress("NOTHING_TO_INLINE", "unused")
    inline fun toBoolean(): Boolean? = if (this == Key) true else if (this === Value) false else null

    @Suppress("NOTHING_TO_INLINE", "unused")
    inline fun isKey(): Boolean = this == Key

    @Suppress("NOTHING_TO_INLINE", "unused")
    inline fun isValue(): Boolean = this == Value

    // endregion

    companion object {
        // region Inline Methods

        @Suppress("NOTHING_TO_INLINE", "unused")
        inline fun deoptimized(value: Byte): CwtExpressionRole = entries[value.toInt()] // 3.0.1 radical optimization

        @Suppress("NOTHING_TO_INLINE", "unused")
        fun fromBoolean(value: Boolean?): CwtExpressionRole = if (value == true) Key else if (value == false) Value else Other

        // endregion
    }
}
