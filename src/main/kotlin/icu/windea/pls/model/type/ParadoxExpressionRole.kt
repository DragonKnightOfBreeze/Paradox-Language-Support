package icu.windea.pls.model.type

import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.model.expressions.ParadoxExpression

/**
 * @see ParadoxExpressionElement
 * @see ParadoxExpression
 */
enum class ParadoxExpressionRole(val text: String) {
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
        inline fun deoptimized(value: Byte): ParadoxExpressionRole = entries[value.toInt()] // 3.0.1 radical optimization

        @Suppress("NOTHING_TO_INLINE", "unused")
        fun fromBoolean(value: Boolean?): ParadoxExpressionRole = if (value == true) Key else if (value == false) Value else Other

        // endregion
    }
}
