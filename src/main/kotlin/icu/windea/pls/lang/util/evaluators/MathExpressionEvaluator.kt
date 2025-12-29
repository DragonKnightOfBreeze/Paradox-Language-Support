package icu.windea.pls.lang.util.evaluators

import com.intellij.openapi.progress.ProgressManager

open class MathExpressionEvaluator: MathExpressionEvaluatorBase() {
    private enum class ContextType { None, Operand, Operator, LeftPar, LeftAbs }

    override fun evaluate(tokens: List<MathToken>): MathResult {
        val values = ArrayDeque<MathResult>()
        val expressions = ArrayDeque<MathExpression>()
        var contextType = ContextType.None

        fun popExpression() {
            val expression = expressions.removeLast()
            when (expression) {
                is MathExpression.Unary -> {
                    val value = values.removeLastOrNull()
                        ?: throw IllegalStateException("Cannot evaluate: missing operand for unary operator.")
                    validateUnary(expression.operator, value)
                    val result = expression.operator.evaluate(value)
                    onUnaryApplied(expression.operator, value, result)
                    values.addLast(result)
                }
                is MathExpression.Binary -> {
                    val right = values.removeLastOrNull()
                        ?: throw IllegalStateException("Cannot evaluate: missing right operand for binary operator.")
                    val left = values.removeLastOrNull()
                        ?: throw IllegalStateException("Cannot evaluate: missing left operand for binary operator.")
                    validateBinary(expression.operator, left, right)
                    val result = expression.operator.evaluate(left, right)
                    onBinaryApplied(expression.operator, left, right, result)
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
                    values.addLast(token.operand)
                    contextType = ContextType.Operand
                }
                is MathToken.Operator.LeftAbs -> {
                    expressions.addLast(MathExpression.Dangling.LeftAbs)
                    contextType = ContextType.LeftAbs
                }
                is MathToken.Operator.RightAbs -> {
                    while (true) {
                        val top = expressions.lastOrNull() ?: throw IllegalStateException("Cannot evaluate: mismatched absolute operator.")
                        if (top is MathExpression.Dangling.LeftAbs) {
                            expressions.removeLast()
                            break
                        }
                        popExpression()
                    }

                    val value = values.removeLastOrNull()
                        ?: throw IllegalStateException("Cannot evaluate: missing operand for absolute operator.")
                    val result = MathOperator.Unary.Abs.evaluate(value)
                    onUnaryApplied(MathOperator.Unary.Abs, value, result)
                    values.addLast(result)
                    contextType = ContextType.Operand
                }
                is MathToken.Operator.LeftPar -> {
                    expressions.addLast(MathExpression.Dangling.LeftPar)
                    contextType = ContextType.LeftPar
                }
                is MathToken.Operator.RightPar -> {
                    while (true) {
                        val top = expressions.lastOrNull() ?: throw IllegalStateException("Cannot evaluate: mismatched parentheses.")
                        if (top is MathExpression.Dangling.LeftPar) {
                            expressions.removeLast()
                            break
                        }
                        popExpression()
                    }
                    contextType = ContextType.Operand
                }
                is MathToken.Operator -> {
                    val isUnary = contextType == ContextType.None || contextType == ContextType.Operator || contextType == ContextType.LeftPar || contextType == ContextType.LeftAbs
                    if (isUnary) {
                        val unary = toUnaryOperator(token) ?: throw IllegalStateException("Cannot evaluate: invalid unary operator.")
                        pushExpression(MathExpression.Unary(unary))
                        contextType = ContextType.Operator
                    } else {
                        val binary = toBinaryOperator(token) ?: throw IllegalStateException("Cannot evaluate: invalid binary operator.")
                        pushExpression(MathExpression.Binary(binary))
                        contextType = ContextType.Operator
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
            throw IllegalStateException(
                "Cannot evaluate: invalid expression. " +
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

    override fun validateBinary(operator: MathOperator.Binary, left: MathResult, right: MathResult) {
        ensureArithmeticValid(operator, right)
    }

    override fun onUnaryApplied(operator: MathOperator.Unary, input: MathResult, result: MathResult) {
        handleNumberType(result, input)
    }

    override fun onBinaryApplied(operator: MathOperator.Binary, left: MathResult, right: MathResult, result: MathResult) {
        handleNumberType(operator, left, right, result)
    }

    private fun ensureArithmeticValid(operator: MathOperator.Binary, right: MathResult) {
        if (operator is MathOperator.Binary.Div || operator is MathOperator.Binary.Mod) {
            if (right.value == 0f) throw ArithmeticException("/ by zero")
        }
    }

    private fun handleNumberType(result: MathResult, input: MathResult) {
        result.isInt = input.isInt
    }

    private fun handleNumberType(operator: MathOperator.Binary, left: MathResult, right: MathResult, result: MathResult) {
        if (!left.isInt || !right.isInt) {
            result.isInt = false
            return
        }
        return when (operator) {
            MathOperator.Binary.Div -> {
                // 注意：MathOperator.Binary.Div 会原地修改 left.value，因此不能再用 left.value 推导可整除性。
                // 在两个操作数都为整数时，只要结果是整数即可认为 isInt。
                val computedValue = result.value
                val asInt = computedValue.toInt().toFloat()
                result.isInt = computedValue == asInt
            }
            else -> result.isInt = true
        }
    }
}
