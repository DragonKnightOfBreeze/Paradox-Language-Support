package icu.windea.pls.lang.util.calculators

sealed interface MathExpression {
    val precedence: Int
    val rightAssociative: Boolean

    data class Unary(val operator: MathOperator.Unary) : MathExpression {
        override val precedence = 3
        override val rightAssociative = true
    }

    data class Binary(val operator: MathOperator.Binary) : MathExpression {
        override val precedence: Int = when (operator) {
            MathOperator.Binary.Times, MathOperator.Binary.Div, MathOperator.Binary.Mod -> 2
            MathOperator.Binary.Plus, MathOperator.Binary.Minus -> 1
        }
        override val rightAssociative = false
    }

    sealed class Dangling: MathExpression {
        override val precedence get() = -1
        override val rightAssociative get() = false

        data object LeftPar : Dangling()
        data object LeftAbs : Dangling()
    }
}
