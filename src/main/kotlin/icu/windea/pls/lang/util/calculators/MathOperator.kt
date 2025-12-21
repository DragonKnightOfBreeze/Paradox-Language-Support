package icu.windea.pls.lang.util.calculators

import kotlin.math.absoluteValue

sealed interface MathOperator {
    sealed interface Unary : MathOperator {
         fun calculate(input: MathCalculationResult): MathCalculationResult

        data object Plus : Unary {
            override fun calculate(input: MathCalculationResult) = input
        }

        data object Minus : Unary {
            override fun calculate(input: MathCalculationResult) = input.apply { value = -value }
        }

        data object Abs : Unary {
            override fun calculate(input: MathCalculationResult) = input.apply { value = value.absoluteValue }
        }
    }

    sealed interface Binary : MathOperator {
         fun calculate(left: MathCalculationResult, right: MathCalculationResult): MathCalculationResult

        data object Plus : Binary {
            override fun calculate(left: MathCalculationResult, right: MathCalculationResult) = left.apply { value += right.value }
        }

        data object Minus : Binary {
            override fun calculate(left: MathCalculationResult, right: MathCalculationResult) = left.apply { value -= right.value }
        }

        data object Times : Binary {
            override fun calculate(left: MathCalculationResult, right: MathCalculationResult) = left.apply { value *= right.value }
        }

        data object Div : Binary {
            override fun calculate(left: MathCalculationResult, right: MathCalculationResult) = left.apply { value /= right.value }
        }

        data object Mod : Binary {
            override fun calculate(left: MathCalculationResult, right: MathCalculationResult) = left.apply { value %= right.value }
        }
    }
}
