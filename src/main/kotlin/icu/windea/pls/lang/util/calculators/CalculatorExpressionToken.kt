package icu.windea.pls.lang.util.calculators

sealed interface CalculatorExpressionToken

data class CalculatorOperandToken<R : CalculatorResult>(val operand: R) : CalculatorExpressionToken

enum class CalculatorOperatorSymbol {
    Plus,
    Minus,
    Times,
    Div,
    Mod,
}

data class CalculatorOperatorSymbolToken(val symbol: CalculatorOperatorSymbol) : CalculatorExpressionToken

data object CalculatorLeftParenToken : CalculatorExpressionToken

data object CalculatorRightParenToken : CalculatorExpressionToken

data object CalculatorLeftAbsToken : CalculatorExpressionToken

data object CalculatorRightAbsToken : CalculatorExpressionToken
