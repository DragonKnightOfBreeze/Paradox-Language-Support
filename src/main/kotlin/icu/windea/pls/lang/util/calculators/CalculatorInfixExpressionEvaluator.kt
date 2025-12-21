package icu.windea.pls.lang.util.calculators

import com.intellij.openapi.progress.ProgressManager

class CalculatorInfixExpressionEvaluator<R : CalculatorResult>(
    private val toUnaryOperator: (CalculatorOperatorSymbol) -> CalculatorOperator.Unary?,
    private val toBinaryOperator: (CalculatorOperatorSymbol) -> CalculatorOperator.Binary?,
    private val validateBinary: (CalculatorOperator.Binary, R) -> Unit = { _, _ -> },
    private val onUnaryApplied: (CalculatorOperator.Unary, R, R) -> Unit = { _, _, _ -> },
    private val onBinaryApplied: (CalculatorOperator.Binary, R, R, R) -> Unit = { _, _, _, _ -> },
    private val tokenToDebugString: (CalculatorExpressionToken) -> String = { it.toString() },
) {
    private sealed interface OperatorToken : CalculatorExpressionToken {
        val precedence: Int
        val rightAssociative: Boolean
    }

    private data class UnaryOperatorToken(
        val operator: CalculatorOperator.Unary,
    ) : OperatorToken {
        override val precedence: Int = 3
        override val rightAssociative: Boolean = true
    }

    private data class BinaryOperatorToken(
        val operator: CalculatorOperator.Binary,
    ) : OperatorToken {
        override val precedence: Int = when (operator) {
            CalculatorOperator.Binary.Times, CalculatorOperator.Binary.Div, CalculatorOperator.Binary.Mod -> 2
            CalculatorOperator.Binary.Plus, CalculatorOperator.Binary.Minus -> 1
        }
        override val rightAssociative: Boolean = false
    }

    private enum class PrevTokenKind { None, Operand, Operator, LeftParen, LeftAbs }

    @Suppress("UNCHECKED_CAST")
    fun evaluate(tokens: List<CalculatorExpressionToken>): R {
        val values = ArrayDeque<R>()
        val operators = ArrayDeque<CalculatorExpressionToken>()

        var prevKind = PrevTokenKind.None

        fun popOperator() {
            val op = operators.removeLast()
            when (op) {
                is UnaryOperatorToken -> {
                    val value = values.removeLastOrNull()
                        ?: throw IllegalStateException("Cannot calculate inline math: missing operand for unary operator.")
                    val result = op.operator.calculate { value } as R
                    onUnaryApplied(op.operator, value, result)
                    values.addLast(result)
                }
                is BinaryOperatorToken -> {
                    val right = values.removeLastOrNull()
                        ?: throw IllegalStateException("Cannot calculate inline math: missing right operand for binary operator.")
                    val left = values.removeLastOrNull()
                        ?: throw IllegalStateException("Cannot calculate inline math: missing left operand for binary operator.")
                    validateBinary(op.operator, right)
                    val result = op.operator.calculate({ left }, { right }) as R
                    onBinaryApplied(op.operator, left, right, result)
                    values.addLast(result)
                }
                CalculatorLeftParenToken, CalculatorLeftAbsToken -> {
                    throw IllegalStateException("Cannot calculate inline math: mismatched parentheses/abs.")
                }
                else -> {
                    throw IllegalStateException("Cannot calculate inline math: unexpected operator token.")
                }
            }
        }

        fun pushOperator(op: OperatorToken) {
            while (true) {
                val top = operators.lastOrNull()
                if (top !is OperatorToken) break
                val shouldPop = if (op.rightAssociative) top.precedence > op.precedence else top.precedence >= op.precedence
                if (!shouldPop) break
                popOperator()
            }
            operators.addLast(op)
        }

        fun toUnaryOperator(token: CalculatorOperatorSymbolToken): CalculatorOperator.Unary? = toUnaryOperator(token.symbol)

        fun toBinaryOperator(token: CalculatorOperatorSymbolToken): CalculatorOperator.Binary? = toBinaryOperator(token.symbol)

        for (token in tokens) {
            ProgressManager.checkCanceled()
            when (token) {
                is CalculatorOperandToken<*> -> {
                    values.addLast(token.operand as R)
                    prevKind = PrevTokenKind.Operand
                }
                CalculatorLeftParenToken -> {
                    operators.addLast(CalculatorLeftParenToken)
                    prevKind = PrevTokenKind.LeftParen
                }
                CalculatorRightParenToken -> {
                    while (true) {
                        val top = operators.lastOrNull() ?: throw IllegalStateException("Cannot calculate inline math: mismatched parentheses.")
                        if (top == CalculatorLeftParenToken) {
                            operators.removeLast()
                            break
                        }
                        popOperator()
                    }
                    prevKind = PrevTokenKind.Operand
                }
                CalculatorLeftAbsToken -> {
                    operators.addLast(CalculatorLeftAbsToken)
                    prevKind = PrevTokenKind.LeftAbs
                }
                CalculatorRightAbsToken -> {
                    while (true) {
                        val top = operators.lastOrNull() ?: throw IllegalStateException("Cannot calculate inline math: mismatched abs operator.")
                        if (top == CalculatorLeftAbsToken) {
                            operators.removeLast()
                            break
                        }
                        popOperator()
                    }

                    val value = values.removeLastOrNull()
                        ?: throw IllegalStateException("Cannot calculate inline math: missing operand for abs operator.")
                    val result = CalculatorOperator.Unary.Abs.calculate { value } as R
                    onUnaryApplied(CalculatorOperator.Unary.Abs, value, result)
                    values.addLast(result)
                    prevKind = PrevTokenKind.Operand
                }
                is CalculatorOperatorSymbolToken -> {
                    val isUnary = prevKind == PrevTokenKind.None || prevKind == PrevTokenKind.Operator || prevKind == PrevTokenKind.LeftParen || prevKind == PrevTokenKind.LeftAbs
                    if (isUnary) {
                        val unary = toUnaryOperator(token) ?: throw IllegalStateException("Cannot calculate inline math: invalid unary operator.")
                        pushOperator(UnaryOperatorToken(unary))
                        prevKind = PrevTokenKind.Operator
                    } else {
                        val binary = toBinaryOperator(token) ?: throw IllegalStateException("Cannot calculate inline math: invalid binary operator.")
                        pushOperator(BinaryOperatorToken(binary))
                        prevKind = PrevTokenKind.Operator
                    }
                }
                else -> {
                    throw IllegalStateException("Cannot calculate inline math: unexpected token.")
                }
            }
        }

        while (operators.isNotEmpty()) {
            val top = operators.last()
            if (top == CalculatorLeftParenToken || top == CalculatorLeftAbsToken) {
                throw IllegalStateException("Cannot calculate inline math: mismatched parentheses/abs.")
            }
            popOperator()
        }

        if (values.size != 1) {
            throw IllegalStateException(
                "Cannot calculate inline math: invalid expression. " +
                    "tokens=[${tokens.joinToString(" ") { tokenToDebugString(it) }}], " +
                    "values=${values.size}, operators=${operators.size}"
            )
        }

        return values.last()
    }
}
