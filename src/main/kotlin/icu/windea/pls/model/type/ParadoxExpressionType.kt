package icu.windea.pls.model.type

import icu.windea.pls.core.optimizer.ByteOptimizer
import icu.windea.pls.core.optimizer.OptimizerFactory
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

    @Suppress("unused")
    fun isBooleanLiteral(): Boolean {
        return this == Boolean
    }

    fun isNumberLiteral(): Boolean {
        return this == Int || this == Float
    }

    fun isStringLiteral(): Boolean {
        return this == String
    }

    fun isLenientInt(): Boolean {
        return this == Int || this == InlineMath || this == Unknown
    }

    fun isLenientFloat(): Boolean {
        return this == Int || this == Float || this == InlineMath || this == Unknown
    }

    fun isLenientString(): Boolean {
        return this == String || this == Unknown
    }

    fun isNumberOrLenientString(): Boolean {
        return this == Int || this == Float || this == String || this == Unknown
    }

    fun isBlockLike(): Boolean {
        return this == Block || this == Color || this == InlineMath
    }

    // endregion

    companion object {
        private val optimizer = OptimizerFactory.create({ it.ordinal.toByte() }, { entries[it.toInt()] })

        @JvmStatic
        fun optimizer(): ByteOptimizer<ParadoxExpressionType> = optimizer
    }
}
