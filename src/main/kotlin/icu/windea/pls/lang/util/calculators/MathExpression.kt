package icu.windea.pls.lang.util.calculators

sealed interface MathExpression {
    val precedence: Int
    val rightAssociative: Boolean

    data class Unary(val operator: MathOperator.Unary) : MathExpression {
        override val precedence get() = operator.precedence
        override val rightAssociative get() = operator.rightAssociative
    }

    data class Binary(val operator: MathOperator.Binary) : MathExpression {
        override val precedence get() = operator.precedence
        override val rightAssociative get() = operator.rightAssociative
    }

    sealed class Dangling : MathExpression {
        override val precedence get() = -1
        override val rightAssociative get() = false

        data object LeftPar : Dangling()
        data object LeftAbs : Dangling()
    }
}
