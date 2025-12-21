package icu.windea.pls.lang.util.calculators

import kotlin.math.absoluteValue
import kotlin.math.pow

sealed interface MathOperator {
    val precedence: Int
    val rightAssociative: Boolean

    sealed interface Unary : MathOperator {
        override val rightAssociative get() = true

        fun calculate(input: MathCalculationResult): MathCalculationResult

        data object Plus : Unary {
            override val precedence get() = 3
            override fun calculate(input: MathCalculationResult) = input
        }

        data object Minus : Unary {
            override val precedence get() = 3
            override fun calculate(input: MathCalculationResult) = input.apply { value = -value }
        }

        data object Abs : Unary {
            override val precedence get() = 3
            override fun calculate(input: MathCalculationResult) = input.apply { value = value.absoluteValue }
        }
    }

    sealed interface Binary : MathOperator {
        override val rightAssociative get() = false

        fun calculate(left: MathCalculationResult, right: MathCalculationResult): MathCalculationResult

        data object Plus : Binary {
            override val precedence get() = 1
            override fun calculate(left: MathCalculationResult, right: MathCalculationResult) = left.apply { value += right.value }
        }

        data object Minus : Binary {
            override val precedence get() = 1
            override fun calculate(left: MathCalculationResult, right: MathCalculationResult) = left.apply { value -= right.value }
        }

        data object Times : Binary {
            override val precedence get() = 2
            override fun calculate(left: MathCalculationResult, right: MathCalculationResult) = left.apply { value *= right.value }
        }

        data object Div : Binary {
            override val precedence get() = 2
            override fun calculate(left: MathCalculationResult, right: MathCalculationResult) = left.apply { value /= right.value }
        }

        data object Mod : Binary {
            override val precedence get() = 2
            override fun calculate(left: MathCalculationResult, right: MathCalculationResult) = left.apply { value %= right.value }
        }

        data object Pow: Binary {
            override val precedence get() = 2
            override fun calculate(left: MathCalculationResult, right: MathCalculationResult) = left.apply { value = value.pow(right.value) }
        }
    }
}
