package icu.windea.pls.lang.util.calculators

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import icu.windea.pls.core.orNull
import icu.windea.pls.core.surroundsWith
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptInlineMathNumber
import icu.windea.pls.script.psi.ParadoxScriptInlineMathParameter
import icu.windea.pls.script.psi.ParadoxScriptInlineMathScriptedVariableReference
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class ParadoxInlineMathCalculator {
    data class Argument(
        val expression: String,
        val id: String,
        val defaultValue: String,
        var value: String = "",
    )

    data class Result(
        override var value: Float,
        var isInt: Boolean = true,
    ) : CalculatorResult {
        override fun resolveValue(): Number {
            return if (isInt) value.toInt() else value
        }
    }

    fun resolveArguments(element: ParadoxScriptInlineMath): Map<String, Argument> {
        val tokenElement = element.tokenElement ?: return emptyMap()
        val result = mutableMapOf<String, Argument>()
        buildArgumentMap(tokenElement, result)
        return result
    }

    private fun buildArgumentMap(tokenElement: PsiElement, result: MutableMap<String, Argument>) {
        tokenElement.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                when (element) {
                    is ParadoxScriptInlineMathParameter -> {
                        val expression = element.text?.trim()?.orNull() ?: return
                        val id = element.name?.trim()?.orNull()?.let { "$$it$" } ?: return
                        val defaultValue = element.defaultValue.orEmpty()
                        result[expression] = Argument(expression, id, defaultValue)
                    }
                    is ParadoxScriptInlineMathScriptedVariableReference -> {
                        val expression = element.text?.trim()?.orNull() ?: return
                        val id = element.name?.trim()?.orNull() ?: return
                        val resolvedValue = element.resolved()?.scriptedVariableValue
                        val defaultValue = run {
                            val v1 = resolvedValue?.value
                            if (!v1.isNullOrBlank()) return@run v1
                            if (!id.all { it.isLetterOrDigit() || it == '_' }) return@run ""
                            val file = element.containingFile ?: return@run ""
                            val local = PsiTreeUtil.findChildrenOfType(file, ParadoxScriptScriptedVariable::class.java).firstOrNull { it.name == id }
                            local?.value.orEmpty()
                        }
                        result[expression] = Argument(expression, id, defaultValue)
                    }
                }
                super.visitElement(element)
            }
        })
    }

    fun calculate(element: ParadoxScriptInlineMath, args: Map<String, String> = emptyMap()): Result {
        for ((k, v) in args) {
            val value = v.trim()
            if (value.isEmpty()) continue
            if (parseNumberOrNull(value) == null) {
                throw IllegalArgumentException("Invalid argument value for '$k': '$v'")
            }
        }

        val arguments = resolveArguments(element)

        val parameterGroups = arguments.values
            .filter { it.expression.surroundsWith('$', '$') }
            .groupBy { it.id }

        for (argument in arguments.values) {
            val rawArg = args[argument.expression] ?: run {
                val id = argument.id
                val isAmbiguousParameter = argument.expression.surroundsWith('$', '$') && (parameterGroups[id]?.size ?: 0) > 1
                if (isAmbiguousParameter && args.containsKey(id)) {
                    throw IllegalArgumentException("Ambiguous argument '$id': ${parameterGroups.getValue(id).joinToString(", ") { it.expression }}")
                }
                args[id]
            } ?: ""
            val value = rawArg.trim()
            if (value.isNotEmpty()) {
                if (parseNumberOrNull(value) == null) {
                    throw IllegalArgumentException("Invalid argument value for '${argument.expression}': '$rawArg'")
                }
                argument.value = value
                continue
            }

            val defaultValue = argument.defaultValue.trim()
            if (defaultValue.isNotEmpty()) {
                if (parseNumberOrNull(defaultValue) == null) {
                    throw IllegalStateException("Invalid default value for '${argument.expression}': '${argument.defaultValue}'")
                }
                argument.value = defaultValue
            }
        }
        val missingArguments = arguments.values.filter { it.value.isEmpty() }
        if (missingArguments.isNotEmpty()) {
            throw IllegalArgumentException("Missing arguments: ${missingArguments.joinToString(", ") { it.expression }}")
        }
        val tokenElement = element.tokenElement ?: throw IllegalStateException("Cannot calculate inline math: token element is missing.")
        return calculateItems(tokenElement, arguments)
    }

    private fun calculateItems(tokenElement: PsiElement, arguments: Map<String, Argument>): Result {
        PsiTreeUtil.findChildOfType(tokenElement, PsiErrorElement::class.java)?.let { error ->
            val errorText = error.errorDescription.ifEmpty { "Syntax error" }
            throw IllegalStateException("Cannot calculate inline math: $errorText")
        }

        val tokens = mutableListOf<Token>()
        val node = tokenElement.node ?: throw IllegalStateException("Cannot calculate inline math: token element node is missing.")
        collectTokens(node, tokens, arguments)
        if (tokens.isEmpty()) throw IllegalStateException("Cannot calculate inline math: empty expression.")
        return evaluateTokens(tokens)
    }

    private interface Token

    private data class OperandToken(val operand: Result) : Token

    private sealed interface OperatorToken : Token {
        val precedence: Int
        val rightAssociative: Boolean
    }

    private data class UnaryOperatorToken(val operator: CalculatorOperator.Unary) : OperatorToken {
        override val precedence: Int = 3
        override val rightAssociative: Boolean = true
    }

    private data class BinaryOperatorToken(val operator: CalculatorOperator.Binary) : OperatorToken {
        override val precedence: Int = when (operator) {
            CalculatorOperator.Binary.Times, CalculatorOperator.Binary.Div, CalculatorOperator.Binary.Mod -> 2
            CalculatorOperator.Binary.Plus, CalculatorOperator.Binary.Minus -> 1
        }
        override val rightAssociative: Boolean = false
    }

    private data object LeftParenToken : Token
    private data object RightParenToken : Token
    private data object LeftAbsToken : Token
    private data object RightAbsToken : Token

    private fun collectTokens(node: ASTNode, tokens: MutableList<Token>, arguments: Map<String, Argument>) {
        val element = node.psi

        when (element) {
            is ParadoxScriptInlineMathNumber -> {
                val valueText = element.value
                val number = parseNumberOrNull(valueText) ?: throw IllegalStateException(
                    "Cannot calculate inline math: invalid number '$valueText'."
                )
                tokens.add(OperandToken(number))
                return
            }
            is ParadoxScriptInlineMathParameter -> {
                val expression = element.text.orNull() ?: throw IllegalStateException(
                    "Cannot calculate inline math: parameter text is missing."
                )
                val argument = arguments[expression] ?: throw IllegalStateException(
                    "Cannot calculate inline math: parameter '$expression' is not resolved."
                )
                val number = parseNumberOrNull(argument.value) ?: throw IllegalArgumentException(
                    "Invalid argument value for '$expression': '${argument.value}'"
                )
                tokens.add(OperandToken(number))
                return
            }
            is ParadoxScriptInlineMathScriptedVariableReference -> {
                val expression = element.text.orNull() ?: throw IllegalStateException(
                    "Cannot calculate inline math: scripted variable reference text is missing."
                )
                val argument = arguments[expression] ?: throw IllegalStateException(
                    "Cannot calculate inline math: scripted variable reference '$expression' is not resolved."
                )
                val number = parseNumberOrNull(argument.value) ?: throw IllegalArgumentException(
                    "Invalid argument value for '$expression': '${argument.value}'"
                )
                tokens.add(OperandToken(number))
                return
            }
        }

        when (node.elementType) {
            PLUS_SIGN -> tokens.add(PlusOpToken)
            MINUS_SIGN -> tokens.add(MinusOpToken)
            TIMES_SIGN -> tokens.add(TimesOpToken)
            DIV_SIGN -> tokens.add(DivOpToken)
            MOD_SIGN -> tokens.add(ModOpToken)
            LP_SIGN -> tokens.add(LeftParenToken)
            RP_SIGN -> tokens.add(RightParenToken)
            LABS_SIGN -> tokens.add(LeftAbsToken)
            RABS_SIGN -> tokens.add(RightAbsToken)
        }

        for (child in node.getChildren(null)) {
            collectTokens(child, tokens, arguments)
        }
    }

    private data object PlusOpToken : Token
    private data object MinusOpToken : Token
    private data object TimesOpToken : Token
    private data object DivOpToken : Token
    private data object ModOpToken : Token

    private enum class PrevTokenKind { None, Operand, Operator, LeftParen, LeftAbs }

    private fun evaluateTokens(tokens: List<Token>): Result {
        val values = ArrayDeque<Result>()
        val operators = ArrayDeque<Token>()

        var prevKind = PrevTokenKind.None

        fun popOperator() {
            val op = operators.removeLast()
            when (op) {
                is UnaryOperatorToken -> {
                    val value = values.removeLastOrNull()
                        ?: throw IllegalStateException("Cannot calculate inline math: missing operand for unary operator.")
                    val result = op.operator.calculate { value } as Result
                    result.isInt = value.isInt
                    values.addLast(result)
                }
                is BinaryOperatorToken -> {
                    val right = values.removeLastOrNull()
                        ?: throw IllegalStateException("Cannot calculate inline math: missing right operand for binary operator.")
                    val left = values.removeLastOrNull()
                        ?: throw IllegalStateException("Cannot calculate inline math: missing left operand for binary operator.")
                    ensureArithmeticValid(op.operator, right)
                    val result = op.operator.calculate({ left }, { right }) as Result
                    result.isInt = resolveIsIntAfterBinary(op.operator, left, right, result.value)
                    values.addLast(result)
                }
                LeftParenToken, LeftAbsToken -> {
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

        fun toBinaryOperator(token: Token): CalculatorOperator.Binary? = when (token) {
            PlusOpToken -> CalculatorOperator.Binary.Plus
            MinusOpToken -> CalculatorOperator.Binary.Minus
            TimesOpToken -> CalculatorOperator.Binary.Times
            DivOpToken -> CalculatorOperator.Binary.Div
            ModOpToken -> CalculatorOperator.Binary.Mod
            else -> null
        }

        fun toUnaryOperator(token: Token): CalculatorOperator.Unary? = when (token) {
            PlusOpToken -> CalculatorOperator.Unary.Plus
            MinusOpToken -> CalculatorOperator.Unary.Minus
            else -> null
        }

        for (token in tokens) {
            when (token) {
                is OperandToken -> {
                    values.addLast(token.operand)
                    prevKind = PrevTokenKind.Operand
                }
                LeftParenToken -> {
                    operators.addLast(LeftParenToken)
                    prevKind = PrevTokenKind.LeftParen
                }
                RightParenToken -> {
                    while (true) {
                        val top = operators.lastOrNull() ?: throw IllegalStateException("Cannot calculate inline math: mismatched parentheses.")
                        if (top == LeftParenToken) {
                            operators.removeLast()
                            break
                        }
                        popOperator()
                    }
                    prevKind = PrevTokenKind.Operand
                }
                LeftAbsToken -> {
                    operators.addLast(LeftAbsToken)
                    prevKind = PrevTokenKind.LeftAbs
                }
                RightAbsToken -> {
                    while (true) {
                        val top = operators.lastOrNull() ?: throw IllegalStateException("Cannot calculate inline math: mismatched abs operator.")
                        if (top == LeftAbsToken) {
                            operators.removeLast()
                            break
                        }
                        popOperator()
                    }

                    val value = values.removeLastOrNull()
                        ?: throw IllegalStateException("Cannot calculate inline math: missing operand for abs operator.")
                    val result = CalculatorOperator.Unary.Abs.calculate { value } as Result
                    result.isInt = value.isInt
                    values.addLast(result)
                    prevKind = PrevTokenKind.Operand
                }
                else -> {
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
            }
        }

        while (operators.isNotEmpty()) {
            val top = operators.last()
            if (top == LeftParenToken || top == LeftAbsToken) {
                throw IllegalStateException("Cannot calculate inline math: mismatched parentheses/abs.")
            }
            popOperator()
        }

        if (values.size != 1) {
            throw IllegalStateException(
                "Cannot calculate inline math: invalid expression. " +
                    "tokens=[${tokens.joinToString(" ") { it.toDebugString() }}], " +
                    "values=${values.size}, operators=${operators.size}"
            )
        }
        return values.last()
    }

    private fun Token.toDebugString(): String {
        return when (this) {
            is OperandToken -> operand.resolveValue().toString()
            is UnaryOperatorToken -> when (operator) {
                CalculatorOperator.Unary.Plus -> "u+"
                CalculatorOperator.Unary.Minus -> "u-"
                CalculatorOperator.Unary.Abs -> "abs"
            }
            is BinaryOperatorToken -> when (operator) {
                CalculatorOperator.Binary.Plus -> "+"
                CalculatorOperator.Binary.Minus -> "-"
                CalculatorOperator.Binary.Times -> "*"
                CalculatorOperator.Binary.Div -> "/"
                CalculatorOperator.Binary.Mod -> "%"
            }
            LeftParenToken -> "("
            RightParenToken -> ")"
            LeftAbsToken -> "|"
            RightAbsToken -> "|"
            PlusOpToken -> "+"
            MinusOpToken -> "-"
            TimesOpToken -> "*"
            DivOpToken -> "/"
            ModOpToken -> "%"
            else -> this::class.simpleName.orEmpty()
        }
    }

    private fun resolveIsIntAfterBinary(
        operator: CalculatorOperator.Binary,
        left: Result,
        right: Result,
        computedValue: Float,
    ): Boolean {
        val bothInt = left.isInt && right.isInt
        if (!bothInt) return false
        return when (operator) {
            CalculatorOperator.Binary.Plus, CalculatorOperator.Binary.Minus, CalculatorOperator.Binary.Times, CalculatorOperator.Binary.Mod -> true
            CalculatorOperator.Binary.Div -> {
                val leftInt = left.value.toInt()
                val rightInt = right.value.toInt()
                rightInt != 0 && leftInt % rightInt == 0 && computedValue == leftInt / rightInt.toFloat()
            }
        }
    }

    private fun ensureArithmeticValid(operator: CalculatorOperator.Binary, right: Result) {
        if (operator == CalculatorOperator.Binary.Div || operator == CalculatorOperator.Binary.Mod) {
            if (right.value == 0f) throw ArithmeticException("/ by zero")
        }
    }

    private fun parseNumberOrNull(text: String): Result? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null
        val intValue = trimmed.toIntOrNull()
        if (intValue != null) return Result(intValue.toFloat(), isInt = true)
        val floatValue = trimmed.toFloatOrNull() ?: return null
        if (!floatValue.isFinite()) return null
        return Result(floatValue, isInt = false)
    }
}
