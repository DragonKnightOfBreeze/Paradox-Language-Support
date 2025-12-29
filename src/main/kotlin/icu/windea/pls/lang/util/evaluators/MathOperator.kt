package icu.windea.pls.lang.util.evaluators

import kotlin.math.absoluteValue
import kotlin.math.pow

sealed interface MathOperator {
    val precedence: Int
    val rightAssociative: Boolean

    sealed interface Unary : MathOperator {
        override val rightAssociative get() = true

        fun evaluate(input: MathResult): MathResult

        data object Plus : Unary {
            override val precedence get() = 3
            override fun evaluate(input: MathResult) = input
        }

        data object Minus : Unary {
            override val precedence get() = 3
            override fun evaluate(input: MathResult) = input.apply { value = -value }
        }

        data object Abs : Unary {
            override val precedence get() = 3
            override fun evaluate(input: MathResult) = input.apply { value = value.absoluteValue }
        }
    }

    sealed interface Binary : MathOperator {
        override val rightAssociative get() = false

        fun evaluate(left: MathResult, right: MathResult): MathResult

        data object Plus : Binary {
            override val precedence get() = 1
            override fun evaluate(left: MathResult, right: MathResult) = left.apply { value += right.value }
        }

        data object Minus : Binary {
            override val precedence get() = 1
            override fun evaluate(left: MathResult, right: MathResult) = left.apply { value -= right.value }
        }

        data object Times : Binary {
            override val precedence get() = 2
            override fun evaluate(left: MathResult, right: MathResult) = left.apply { value *= right.value }
        }

        data object Div : Binary {
            override val precedence get() = 2
            override fun evaluate(left: MathResult, right: MathResult) = left.apply { value /= right.value }
        }

        data object Mod : Binary {
            override val precedence get() = 2
            override fun evaluate(left: MathResult, right: MathResult) = left.apply { value %= right.value }
        }

        data object Pow: Binary {
            override val precedence get() = 2
            override fun evaluate(left: MathResult, right: MathResult) = left.apply { value = value.pow(right.value) }
        }
    }
}
