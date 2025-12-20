package icu.windea.pls.lang.util.calculators

import kotlin.math.absoluteValue

sealed class CalculatorOperator {
    sealed class Unary : CalculatorOperator() {
        abstract fun calculate(expression: () -> CalculatorResult): CalculatorResult

        data object Plus : Unary() {
            override fun calculate(expression: () -> CalculatorResult) = expression()
        }

        data object Minus : Unary() {
            override fun calculate(expression: () -> CalculatorResult) = expression().apply { value = -value }
        }

        data object Abs : Unary() {
            override fun calculate(expression: () -> CalculatorResult) = expression().apply { value = value.absoluteValue }
        }
    }

    sealed class Binary : CalculatorOperator() {
        abstract fun calculate(left: () -> CalculatorResult, right: () -> CalculatorResult): CalculatorResult

        data object Plus : Binary() {
            override fun calculate(left: () -> CalculatorResult, right: () -> CalculatorResult) = left().apply { value += right().value }
        }

        data object Minus : Binary() {
            override fun calculate(left: () -> CalculatorResult, right: () -> CalculatorResult) = left().apply { value -= right().value }
        }

        data object Times : Binary() {
            override fun calculate(left: () -> CalculatorResult, right: () -> CalculatorResult) = left().apply { value *= right().value }
        }

        data object Div : Binary() {
            override fun calculate(left: () -> CalculatorResult, right: () -> CalculatorResult) = left().apply { value /= right().value }
        }

        data object Mod : Binary() {
            override fun calculate(left: () -> CalculatorResult, right: () -> CalculatorResult) = left().apply { value %= right().value }
        }
    }
}
