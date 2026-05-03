package icu.windea.pls.core.math

import com.intellij.openapi.progress.ProgressManager
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.pow

/**
 * 默认的数学表达式的评估器。
 */
class DefaultMathExpressionEvaluator(
    override var precision: Int? = null,
    override var isFloatingPoint: Boolean? = null,
) : MathExpressionEvaluator {
    private enum class State { None, Operand, Operator, LeftPar, LeftAbs }

    /**
     * @throws ArithmeticException 如果在评估过程中发生任何数学异常。
     * @throws IllegalStateException 如果在评估过程中发生任何导致无法评估的异常。
     */
    override fun evaluate(tokens: List<MathToken>): MathResult {
        val values = ArrayDeque<MathResult>()
        val expressions = ArrayDeque<MathExpression>()
        var state = State.None

        fun popExpression() {
            val expression = expressions.removeLast()
            when (expression) {
                is MathExpression.Unary -> {
                    val value = values.removeLastOrNull()
                        ?: throw IllegalStateException("Cannot evaluate: missing operand for unary operator.")
                    val result = evaluateUnaryOperator(expression.operator, value)
                    values.addLast(result)
                }
                is MathExpression.Binary -> {
                    val right = values.removeLastOrNull()
                        ?: throw IllegalStateException("Cannot evaluate: missing right operand for binary operator.")
                    val left = values.removeLastOrNull()
                        ?: throw IllegalStateException("Cannot evaluate: missing left operand for binary operator.")
                    val result = evaluateBinaryOperator(expression.operator, left, right)
                    values.addLast(result)
                }
                is MathExpression.Dangling -> {
                    throw IllegalStateException("Cannot evaluate: mismatched parentheses or absolute sign.")
                }
            }
        }

        fun pushExpression(expression: MathExpression) {
            while (true) {
                val top = expressions.lastOrNull()
                if (top !is MathExpression) break
                val shouldPop = if (expression.rightAssociative) top.precedence > expression.precedence else top.precedence >= expression.precedence
                if (!shouldPop) break
                popExpression()
            }
            expressions.addLast(expression)
        }

        for (token in tokens) {
            ProgressManager.checkCanceled()
            when (token) {
                is MathToken.Operand -> {
                    if (!token.operand.value.isFinite()) {
                        throw ArithmeticException("NaN operand")
                    }
                    values.addLast(token.operand)
                    state = State.Operand
                }
                is MathToken.Operator.LeftAbs -> {
                    expressions.addLast(MathExpression.Dangling.LeftAbs)
                    state = State.LeftAbs
                }
                is MathToken.Operator.RightAbs -> {
                    while (true) {
                        val top = expressions.lastOrNull()
                            ?: throw IllegalStateException("Cannot evaluate: mismatched absolute operator.")
                        if (top is MathExpression.Dangling.LeftAbs) {
                            expressions.removeLast()
                            break
                        }
                        popExpression()
                    }

                    val value = values.removeLastOrNull()
                        ?: throw IllegalStateException("Cannot evaluate: missing operand for absolute operator.")
                    val result = evaluateUnaryOperator(MathOperator.Unary.Abs, value)
                    values.addLast(result)
                    state = State.Operand
                }
                is MathToken.Operator.LeftPar -> {
                    expressions.addLast(MathExpression.Dangling.LeftPar)
                    state = State.LeftPar
                }
                is MathToken.Operator.RightPar -> {
                    while (true) {
                        val top = expressions.lastOrNull()
                            ?: throw IllegalStateException("Cannot evaluate: mismatched parentheses.")
                        if (top is MathExpression.Dangling.LeftPar) {
                            expressions.removeLast()
                            break
                        }
                        popExpression()
                    }
                    state = State.Operand
                }
                is MathToken.Operator -> {
                    val isUnary = state == State.None || state == State.Operator || state == State.LeftPar || state == State.LeftAbs
                    if (isUnary) {
                        val unary = toUnaryOperator(token)
                            ?: throw IllegalStateException("Cannot evaluate: invalid unary operator.")
                        pushExpression(MathExpression.Unary(unary))
                        state = State.Operator
                    } else {
                        val binary = toBinaryOperator(token)
                            ?: throw IllegalStateException("Cannot evaluate: invalid binary operator.")
                        pushExpression(MathExpression.Binary(binary))
                        state = State.Operator
                    }
                }
            }
        }

        while (expressions.isNotEmpty()) {
            val top = expressions.last()
            if (top is MathExpression.Dangling) {
                throw IllegalStateException("Cannot evaluate: mismatched parentheses or absolute sign.")
            }
            popExpression()
        }

        if (values.size != 1) {
            throw IllegalStateException("Cannot evaluate: invalid expression. " +
                "tokens=[${tokens.joinToString(" ") { it.render() }}], " +
                "values=${values.size}, operators=${expressions.size}"
            )
        }

        return values.last()
    }

    override fun toUnaryOperator(token: MathToken.Operator): MathOperator.Unary? {
        return when (token) {
            MathToken.Operator.Plus -> MathOperator.Unary.Plus
            MathToken.Operator.Minus -> MathOperator.Unary.Minus
            else -> null
        }
    }

    override fun toBinaryOperator(token: MathToken.Operator): MathOperator.Binary? {
        return when (token) {
            MathToken.Operator.Plus -> MathOperator.Binary.Plus
            MathToken.Operator.Minus -> MathOperator.Binary.Minus
            MathToken.Operator.Times -> MathOperator.Binary.Times
            MathToken.Operator.Div -> MathOperator.Binary.Div
            MathToken.Operator.Mod -> MathOperator.Binary.Mod
            MathToken.Operator.Pow -> MathOperator.Binary.Pow
            else -> null
        }
    }

    override fun evaluateUnaryOperator(operator: MathOperator.Unary, input: MathResult): MathResult {
        val value = when (operator) {
            MathOperator.Unary.Plus -> input.value
            MathOperator.Unary.Minus -> -input.value
            MathOperator.Unary.Abs -> input.value.absoluteValue
        }
        val precision = this.precision ?: input.precision
        val isFloatingPoint = this.isFloatingPoint ?: input.isFloatingPoint
        val result = MathResult(value, precision, isFloatingPoint)
        return result
    }

    override fun evaluateBinaryOperator(operator: MathOperator.Binary, left: MathResult, right: MathResult): MathResult {
        if (operator == MathOperator.Binary.Div || operator == MathOperator.Binary.Mod) {
            // 数学检查
            if (right.value == 0.0) throw ArithmeticException("Divided by zero")
        }
        val value = when (operator) {
            MathOperator.Binary.Plus -> left.value + right.value
            MathOperator.Binary.Minus -> left.value - right.value
            MathOperator.Binary.Times -> left.value * right.value
            MathOperator.Binary.Div -> left.value / right.value
            MathOperator.Binary.Mod -> left.value % right.value
            MathOperator.Binary.Pow -> left.value.pow(right.value)
        }
        val precision = this.precision ?: min(left.precision, right.precision)
        val isFloatingPoint = this.isFloatingPoint ?: left.isFloatingPoint || right.isFloatingPoint
        val result = MathResult(value, precision, isFloatingPoint)
        if (operator == MathOperator.Binary.Div) {
            // 如果是除法运算，且结果的值被判定为浮点数，则将结果标记为浮点数
            if (!result.isFloatingPoint && result.isFloatingPointValue()) result.isFloatingPoint = true
        }
        return result
    }
}
