package icu.windea.pls.lang.util.evaluators

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import icu.windea.pls.core.children
import icu.windea.pls.core.orNull
import icu.windea.pls.core.surroundsWith
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptInlineMathNumber
import icu.windea.pls.script.psi.ParadoxScriptInlineMathParameter
import icu.windea.pls.script.psi.ParadoxScriptInlineMathScriptedVariableReference

class ParadoxInlineMathEvaluator {
    data class Argument(
        val expression: String,
        val id: String,
        val defaultValue: String,
        val resolvedValueElement: PsiElement? = null,
        var value: String = "",
    )

    fun resolveArguments(element: ParadoxScriptInlineMath): Map<String, Argument> {
        val tokenElement = element.tokenElement ?: return emptyMap()
        val result = sortedMapOf<String, Argument>()
        buildArguments(tokenElement, result)
        return result
    }

    private fun buildArguments(tokenElement: PsiElement, result: MutableMap<String, Argument>) {
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

    fun evaluate(element: ParadoxScriptInlineMath, args: Map<String, String> = emptyMap()): MathResult {
        return evaluateInternal(element, args)
    }

    private fun evaluateInternal(element: ParadoxScriptInlineMath, args: Map<String, String>): MathResult {
        val arguments = resolveArguments(element)
        prepareArguments(arguments, args)
        return evaluateItems(element, arguments, args)
    }

    private fun prepareArguments(arguments: Map<String, Argument>, args: Map<String, String>) {
        if (arguments.isEmpty()) return
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
                // 这里的默认值是用于展示的，因此可以是无效值
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
    }

    private fun evaluateItems(element: ParadoxScriptInlineMath, arguments: Map<String, Argument>, args: Map<String, String>): MathResult {
        val tokenElement = element.tokenElement ?: throw IllegalStateException("Cannot evaluate: token element is missing.")
        PsiTreeUtil.findChildOfType(tokenElement, PsiErrorElement::class.java)?.let { error ->
            val errorText = error.errorDescription.ifEmpty { "Syntax error" }
            throw IllegalStateException("Cannot evaluate: $errorText")
        }

        val tokens = mutableListOf<MathToken>()
        collectTokens(tokenElement, tokens, arguments, args)
        if (tokens.isEmpty()) throw IllegalStateException("Cannot evaluate: empty expression.")
        return evaluateTokens(tokens)
    }

    private fun collectTokens(element: PsiElement, tokens: MutableList<MathToken>, arguments: Map<String, Argument>, args: Map<String, String>) {
        val operandToken = resolveOperandToken(element, arguments, args)
        if (operandToken != null) {
            tokens.add(operandToken)
            return
        }

        val operatorToken = resolveOperatorToken(element)
        if (operatorToken != null) {
            tokens.add(operatorToken)
        }

        for (child in element.children()) {
            ProgressManager.checkCanceled()
            collectTokens(child, tokens, arguments, args)
        }
    }

    private fun resolveOperandToken(element: PsiElement, arguments: Map<String, Argument>, args: Map<String, String>): MathToken.Operand? {
        return when (element) {
            is ParadoxScriptInlineMathNumber -> {
                val valueText = element.value
                val number = parseNumberOrNull(valueText)
                    ?: throw IllegalStateException("Cannot evaluate: invalid number '$valueText'.")
                MathToken.Operand(number)
            }
            is ParadoxScriptInlineMathParameter -> {
                val expression = element.text?.trim()?.orNull()
                    ?: throw IllegalStateException("Cannot evaluate: parameter text is missing.")
                val argument = arguments[expression]
                    ?: throw IllegalStateException("Cannot evaluate: parameter '$expression' is not resolved.")
                val number = parseNumberOrNull(argument.value)
                    ?: throw IllegalArgumentException("Invalid argument value for '$expression': '${argument.value}'")
                MathToken.Operand(number)
            }
            is ParadoxScriptInlineMathScriptedVariableReference -> {
                val expression = element.text?.trim()?.orNull()
                    ?: throw IllegalStateException("Cannot evaluate: scripted variable reference text is missing.")
                val argument = arguments[expression]
                    ?: throw IllegalStateException("Cannot evaluate: scripted variable reference '$expression' is not resolved.")
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
                                        evaluateInternal(resolvedValueElement, args)
                                    } ?: throw IllegalArgumentException("Cannot evaluate: recursive scripted variable reference '$expression'.")
                                } ?: throw IllegalArgumentException("Cannot evaluate: recursion detected.")
                            }
                            else -> {
                                val valueText = resolvedValueElement.text?.trim().orEmpty()
                                parseNumberOrNull(valueText)
                                    ?: throw IllegalStateException("Cannot evaluate: invalid scripted variable value '$valueText' for '$expression'.")
                            }
                        }
                    }
                }
                MathToken.Operand(resolvedNumber)
            }
            else -> null
        }
    }

    private fun resolveOperatorToken(element: PsiElement): MathToken.Operator? {
        return when (element.elementType) {
            PLUS_SIGN -> MathToken.Operator.Plus
            MINUS_SIGN -> MathToken.Operator.Minus
            TIMES_SIGN -> MathToken.Operator.Times
            DIV_SIGN -> MathToken.Operator.Div
            MOD_SIGN -> MathToken.Operator.Mod
            LABS_SIGN -> MathToken.Operator.LeftAbs
            RABS_SIGN -> MathToken.Operator.RightAbs
            LP_SIGN -> MathToken.Operator.LeftPar
            RP_SIGN -> MathToken.Operator.RightPar
            else -> null
        }
    }

    private fun parseNumberOrNull(text: String): MathResult? {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null
        val intValue = trimmed.toIntOrNull()
        if (intValue != null) return MathResult(intValue.toFloat(), isInt = true)
        val floatValue = trimmed.toFloatOrNull() ?: return null
        if (!floatValue.isFinite()) return null
        return MathResult(floatValue, isInt = false)
    }

    private fun evaluateTokens(tokens: List<MathToken>): MathResult {
        val evaluator = MathExpressionEvaluator()
        return evaluator.evaluate(tokens)
    }
}
