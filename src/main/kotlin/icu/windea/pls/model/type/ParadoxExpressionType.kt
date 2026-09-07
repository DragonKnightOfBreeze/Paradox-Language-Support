package icu.windea.pls.model.type

import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.model.expressions.ParadoxExpression

/**
 * @see ParadoxExpressionElement
 * @see ParadoxExpression
 */
enum class ParadoxExpressionType(val text: String) {
    Unknown("(unknown)"),
    Boolean("boolean"),
    Int("int"),
    Float("float"),
    String("string"),
    Block("block"),
    Color("color"),
    InlineMath("inline_math"),
    ScriptedVariableReference("scripted_variable_reference"),
    ;

    override fun toString() = text

    // region Matchers

    fun isNumberLiteral(): Boolean {
        return this == Int || this == Float
    }

    fun isStringLiteral(): Boolean {
        return this == String
    }

    fun isLenientBooleanLiteral(): Boolean {
        return this == Boolean || this == Unknown
    }

    fun isLenientNumberLiteral(): Boolean {
        return this == Int || this == Float || this == Unknown
    }

    fun isLenientStringLiteral(): Boolean {
        return this == String || this == Unknown
    }

    fun isLenientNumberOrStringLiteral(): Boolean {
        return this == Int || this == Float || this == String || this == Unknown
    }

    fun isLenientLiteral(): Boolean {
        return this == Boolean || this == Int || this == Float || this == String || this == Unknown
    }

    fun isLenientInt(): Boolean {
        return this == Int || this == InlineMath || this == Unknown
    }

    fun isLenientFloat(): Boolean {
        return this == Int || this == Float || this == InlineMath || this == Unknown
    }

    @Suppress("unused")
    fun isBlockLike(): Boolean {
        return this == Block || this == Color || this == InlineMath
    }

    // endregion

    // region Inline Methods

    @Suppress("NOTHING_TO_INLINE", "unused")
    inline fun optimized(): Byte = ordinal.toByte() // 3.0.1 radical optimization

    // endregion

    companion object {
        // region Inline Methods

        @Suppress("NOTHING_TO_INLINE", "unused")
        inline fun deoptimized(value: Byte): ParadoxExpressionType = entries[value.toInt()] // 3.0.1 radical optimization

        // endregion
    }
}
