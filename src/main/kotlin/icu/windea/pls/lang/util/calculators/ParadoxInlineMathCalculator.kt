package icu.windea.pls.lang.util.calculators

import com.intellij.lang.ASTNode
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import icu.windea.pls.core.orNull
import icu.windea.pls.core.surroundsWith
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptInlineMathNumber
import icu.windea.pls.script.psi.ParadoxScriptInlineMathParameter
import icu.windea.pls.script.psi.ParadoxScriptInlineMathScriptedVariableReference

class ParadoxInlineMathCalculator {
    data class Argument(
        val expression: String,
        val id: String,
        val defaultValue: String,
        val resolvedValueElement: PsiElement? = null,
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
        val result = sortedMapOf<String, Argument>()
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
                        if (id != expression) result[id] = Argument(id, id, defaultValue)
                    }
                    is ParadoxScriptInlineMathScriptedVariableReference -> {
                        val expression = element.text?.trim()?.orNull() ?: return
                        val id = element.name?.trim()?.orNull() ?: return // = expression
                        val resolvedValue = when {
                            DumbService.isDumb(tokenElement.project) -> null
                            else -> element.resolved()?.scriptedVariableValue
                        }
                        val defaultValue = resolvedValue?.text.orEmpty()
                        result[expression] = Argument(expression, id, defaultValue, resolvedValue)
                        // if (id != expression) result[id] = Argument(id, id, defaultValue)
                    }
                }
                super.visitElement(element)
            }
        })
    }

    fun calculate(element: ParadoxScriptInlineMath, args: Map<String, String> = emptyMap()): Result {
        return calculateInternal(element, args)
    }

    private fun calculateInternal(element: ParadoxScriptInlineMath, args: Map<String, String>): Result {
        val arguments = resolveArguments(element)

        // val parameterGroups = arguments.values
        //     .filter { it.expression.surroundsWith('$', '$') }
        //     .groupBy { it.id }

        for (argument in arguments.values) {
            // 不检查歧义参数
            // val isParameter = argument.expression.surroundsWith('$', '$')
            // val group = parameterGroups[argument.id]
            // val isAmbiguousParameter = isParameter && (group?.count { it.expression != argument.id } ?: 0) > 1
            // if (isAmbiguousParameter && args.containsKey(argument.id) && !args.containsKey(argument.expression)) {
            //     throw IllegalArgumentException("Ambiguous argument '${argument.id}': ${group.orEmpty().joinToString(", ") { it.expression }}")
            // }

            // 优先使用带相同默认值的，其次使用不带默认值的
            val value = args[argument.expression]?.trim() ?: args[argument.id]?.trim() ?: ""
            if (value.isNotEmpty()) {
                if (parseNumberOrNull(value) == null) {
                    throw IllegalArgumentException("Invalid argument value for '${argument.expression}': '$value'")
                }
                argument.value = value
                continue
            }

            val defaultValue = argument.defaultValue.trim()
            if (defaultValue.isNotEmpty()) {
                // 这里的默认值用于展示，因此可能是无效值
                if (parseNumberOrNull(defaultValue) != null) {
                    argument.value = defaultValue
                }
            }
        }
        val missingArguments = arguments.values.filter { a ->
            if (a.expression.surroundsWith('$', '$')) {
                a.value.isEmpty()
            } else {
                a.value.isEmpty() && a.resolvedValueElement == null
            }
        }
        if (missingArguments.isNotEmpty()) {
            throw IllegalArgumentException("Missing arguments: ${missingArguments.joinToString(", ") { it.expression }}")
        }
        val tokenElement = element.tokenElement ?: throw IllegalStateException("Cannot calculate inline math: token element is missing.")
        return calculateItems(tokenElement, arguments, args)
    }

    private fun calculateItems(tokenElement: PsiElement, arguments: Map<String, Argument>, args: Map<String, String>): Result {
        PsiTreeUtil.findChildOfType(tokenElement, PsiErrorElement::class.java)?.let { error ->
            val errorText = error.errorDescription.ifEmpty { "Syntax error" }
            throw IllegalStateException("Cannot calculate inline math: $errorText")
        }

        val tokens = mutableListOf<CalculatorExpressionToken>()
        val node = tokenElement.node ?: throw IllegalStateException("Cannot calculate inline math: token element node is missing.")
        collectTokens(node, tokens, arguments, args)
        if (tokens.isEmpty()) throw IllegalStateException("Cannot calculate inline math: empty expression.")
        return evaluateTokens(tokens)
    }

    private fun collectTokens(
        node: ASTNode,
        tokens: MutableList<CalculatorExpressionToken>,
        arguments: Map<String, Argument>,
        args: Map<String, String>,
    ) {
        val element = node.psi

        when (element) {
            is ParadoxScriptInlineMathNumber -> {
                val valueText = element.value
                val number = parseNumberOrNull(valueText)
                    ?: throw IllegalStateException("Cannot calculate inline math: invalid number '$valueText'.")
                tokens.add(CalculatorOperandToken(number))
                return
            }
            is ParadoxScriptInlineMathParameter -> {
                val expression = element.text?.trim()?.orNull()
                    ?: throw IllegalStateException("Cannot calculate inline math: parameter text is missing.")
                val argument = arguments[expression]
                    ?: throw IllegalStateException("Cannot calculate inline math: parameter '$expression' is not resolved.")
                val number = parseNumberOrNull(argument.value)
                    ?: throw IllegalArgumentException("Invalid argument value for '$expression': '${argument.value}'")
                tokens.add(CalculatorOperandToken(number))
                return
            }
            is ParadoxScriptInlineMathScriptedVariableReference -> {
                val expression = element.text?.trim()?.orNull()
                    ?: throw IllegalStateException("Cannot calculate inline math: scripted variable reference text is missing.")
                val argument = arguments[expression]
                    ?: throw IllegalStateException("Cannot calculate inline math: scripted variable reference '$expression' is not resolved.")
                val resolvedNumber = when {
                    argument.value.isNotEmpty() -> {
                        parseNumberOrNull(argument.value)
                            ?: throw IllegalArgumentException("Invalid argument value for '$expression': '${argument.value}'")
                    }
                    else -> {
                        val resolvedValueElement = argument.resolvedValueElement
                            ?: throw IllegalArgumentException("Missing arguments: $expression")
                        when (resolvedValueElement) {
                            is ParadoxScriptInlineMath -> {
                                withRecursionGuard {
                                    withRecursionCheck("sv:${argument.id}") {
                                        calculateInternal(resolvedValueElement, args)
                                    } ?: throw IllegalArgumentException("Cannot calculate inline math: recursive scripted variable reference '$expression'.")
                                } ?: throw IllegalArgumentException("Cannot calculate inline math: recursion detected.")
                            }
                            else -> {
                                val valueText = resolvedValueElement.text?.trim().orEmpty()
                                parseNumberOrNull(valueText)
                                    ?: throw IllegalStateException("Cannot calculate inline math: invalid scripted variable value '$valueText' for '$expression'.")
                            }
                        }
                    }
                }
                tokens.add(CalculatorOperandToken(resolvedNumber))
                return
            }
        }

        when (node.elementType) {
            PLUS_SIGN -> tokens.add(CalculatorOperatorSymbolToken(CalculatorOperatorSymbol.Plus))
            MINUS_SIGN -> tokens.add(CalculatorOperatorSymbolToken(CalculatorOperatorSymbol.Minus))
            TIMES_SIGN -> tokens.add(CalculatorOperatorSymbolToken(CalculatorOperatorSymbol.Times))
            DIV_SIGN -> tokens.add(CalculatorOperatorSymbolToken(CalculatorOperatorSymbol.Div))
            MOD_SIGN -> tokens.add(CalculatorOperatorSymbolToken(CalculatorOperatorSymbol.Mod))
            LP_SIGN -> tokens.add(CalculatorLeftParenToken)
            RP_SIGN -> tokens.add(CalculatorRightParenToken)
            LABS_SIGN -> tokens.add(CalculatorLeftAbsToken)
            RABS_SIGN -> tokens.add(CalculatorRightAbsToken)
        }

        for (child in node.getChildren(null)) {
            collectTokens(child, tokens, arguments, args)
        }
    }

    private fun evaluateTokens(tokens: List<CalculatorExpressionToken>): Result {
        val evaluator = CalculatorInfixExpressionEvaluator<Result>(
            toUnaryOperator = {
                when (it) {
                    CalculatorOperatorSymbol.Plus -> CalculatorOperator.Unary.Plus
                    CalculatorOperatorSymbol.Minus -> CalculatorOperator.Unary.Minus
                    else -> null
                }
            },
            toBinaryOperator = {
                when (it) {
                    CalculatorOperatorSymbol.Plus -> CalculatorOperator.Binary.Plus
                    CalculatorOperatorSymbol.Minus -> CalculatorOperator.Binary.Minus
                    CalculatorOperatorSymbol.Times -> CalculatorOperator.Binary.Times
                    CalculatorOperatorSymbol.Div -> CalculatorOperator.Binary.Div
                    CalculatorOperatorSymbol.Mod -> CalculatorOperator.Binary.Mod
                }
            },
            validateBinary = { operator, right ->
                ensureArithmeticValid(operator, right)
            },
            onUnaryApplied = { _, operand, result ->
                result.isInt = operand.isInt
            },
            onBinaryApplied = { operator, left, right, result ->
                result.isInt = resolveIsIntAfterBinary(operator, left, right, result.value)
            },
            tokenToDebugString = {
                when (it) {
                    is CalculatorOperandToken<*> -> (it.operand as? Result)?.resolveValue()?.toString().orEmpty()
                    is CalculatorOperatorSymbolToken -> when (it.symbol) {
                        CalculatorOperatorSymbol.Plus -> "+"
                        CalculatorOperatorSymbol.Minus -> "-"
                        CalculatorOperatorSymbol.Times -> "*"
                        CalculatorOperatorSymbol.Div -> "/"
                        CalculatorOperatorSymbol.Mod -> "%"
                    }
                    CalculatorLeftParenToken -> "("
                    CalculatorRightParenToken -> ")"
                    CalculatorLeftAbsToken -> "|"
                    CalculatorRightAbsToken -> "|"
                    else -> it::class.simpleName.orEmpty()
                }
            }
        )
        return evaluator.evaluate(tokens)
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
                // 注意：CalculatorOperator.Binary.Div 会原地修改 left.value，因此不能再用 left.value 推导可整除性。
                // 在两个操作数都为整数时，只要结果是整数即可认为 isInt。
                val asInt = computedValue.toInt().toFloat()
                computedValue == asInt
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
