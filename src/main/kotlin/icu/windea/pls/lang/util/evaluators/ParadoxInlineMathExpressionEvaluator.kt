package icu.windea.pls.lang.util.evaluators

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.elementType
import icu.windea.pls.core.children
import icu.windea.pls.core.math.MathExpressionEvaluator
import icu.windea.pls.core.math.MathResult
import icu.windea.pls.core.math.MathToken
import icu.windea.pls.core.math.TokenBasedMathExpressionEvaluator
import icu.windea.pls.core.orNull
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.surroundsWith
import icu.windea.pls.core.withRecursionGuard
import icu.windea.pls.lang.psi.resolved
import icu.windea.pls.model.ParadoxInlineMathArgument
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptInlineMathExpression
import icu.windea.pls.script.psi.ParadoxScriptInlineMathNumber
import icu.windea.pls.script.psi.ParadoxScriptInlineMathParameter
import icu.windea.pls.script.psi.ParadoxScriptInlineMathScriptedVariableReference

/**
 * 内联数学表达式的评估器。
 *
 * @see ParadoxScriptInlineMath
 * @see ParadoxScriptInlineMathExpression
 * @see TokenBasedMathExpressionEvaluator
 */
@Suppress("unused")
class ParadoxInlineMathExpressionEvaluator(
    override var precision: Int? = null,
    override var isFloatingPoint: Boolean? = null,
) : MathExpressionEvaluator {
    fun resolveArguments(element: ParadoxScriptInlineMath): Map<String, ParadoxInlineMathArgument> {
        return resolveArgumentsInternal(element.inlineMathExpression)
    }

    fun resolveArguments(element: ParadoxScriptInlineMathExpression): Map<String, ParadoxInlineMathArgument> {
        return resolveArgumentsInternal(element)
    }

    /**
     * 评估来自 [element] 的内联数学表达式。如果发生意外，则会抛出异常。
     *
     * @throws ArithmeticException 如果在评估过程中发生任何数学异常。
     * @throws IllegalArgumentException 如果在评估过程中发生任何与参数有关的异常（缺少参数、参数值不合法等）。
     * @throws IllegalStateException 如果在评估过程中发生任何导致无法评估的异常。
     */
    fun evaluate(element: ParadoxScriptInlineMath, args: Map<String, String> = emptyMap()): MathResult {
        return evaluateInternal(element.inlineMathExpression, args)
    }

    /**
     * 评估来自 [element] 的内联数学表达式。如果发生意外，则会抛出异常。
     *
     * @throws ArithmeticException 如果在评估过程中发生任何数学异常。
     * @throws IllegalArgumentException 如果在评估过程中发生任何与参数有关的异常（缺少参数、参数值不合法等）。
     * @throws IllegalStateException 如果在评估过程中发生任何导致无法评估的异常。
     */
    fun evaluate(element: ParadoxScriptInlineMathExpression, args: Map<String, String> = emptyMap()): MathResult {
        return evaluateInternal(element, args)
    }

    /**
     * 评估来自 [element] 的内联数学表达式。如果发生意外，则会直接返回 `null`。
     */
    fun evaluateOrNull(element: ParadoxScriptInlineMath, args: Map<String, String> = emptyMap()): MathResult? {
        return runCatchingCancelable { evaluateInternal(element.inlineMathExpression, args) }.getOrNull()
    }

    /**
     * 评估来自 [element] 的内联数学表达式。如果发生意外，则会直接返回 `null`。
     */
    fun evaluateOrNull(element: ParadoxScriptInlineMathExpression, args: Map<String, String> = emptyMap()): MathResult? {
        return runCatchingCancelable { evaluateInternal(element, args) }.getOrNull()
    }

    // region Implementations

    private fun resolveArgumentsInternal(element: ParadoxScriptInlineMathExpression?): Map<String, ParadoxInlineMathArgument> {
        if (element == null) return emptyMap()
        val result = sortedMapOf<String, ParadoxInlineMathArgument>()
        element.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                when (element) {
                    is ParadoxScriptInlineMathParameter -> {
                        val expression = element.text?.trim()?.orNull() ?: return
                        val id = element.name?.trim()?.orNull()?.let { "$$it$" } ?: return
                        val defaultValue = element.defaultValue.orEmpty()
                        result[expression] = ParadoxInlineMathArgument(expression, id, defaultValue)
                        if (id != expression) result[id] = ParadoxInlineMathArgument(id, id, defaultValue)
                    }
                    is ParadoxScriptInlineMathScriptedVariableReference -> {
                        val expression = element.text?.trim()?.orNull() ?: return
                        val id = element.name?.trim()?.orNull() ?: return // = expression
                        val resolved = when {
                            DumbService.isDumb(element.project) -> null
                            else -> element.resolved()
                        }
                        val defaultValue = resolved?.text.orEmpty()
                        result[expression] = ParadoxInlineMathArgument(expression, id, defaultValue).withResolvedElement(resolved)
                        // if (id != expression) result[id] = ParadoxInlineMathArgument(id, id, defaultValue)
                    }
                }
                super.visitElement(element)
            }
        })
        if (result.isEmpty()) return emptyMap()
        return result
    }

    private fun evaluateInternal(element: ParadoxScriptInlineMathExpression?, args: Map<String, String>): MathResult {
        if (element == null) throw IllegalStateException("Cannot evaluate: empty inline math expression.")
        val arguments = resolveArgumentsInternal(element)
        prepareArguments(args, arguments)
        return evaluateWithArguments(element, args, arguments)
    }

    private fun prepareArguments(args: Map<String, String>, arguments: Map<String, ParadoxInlineMathArgument>) {
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
                if (resolveResult(value) == null) {
                    throw IllegalArgumentException("Invalid argument value for '${argument.expression}': '$value'")
                }
                argument.value = value
                continue
            }

            val defaultValue = argument.defaultValue.trim()
            if (defaultValue.isNotEmpty()) {
                // 这里的默认值是用于展示的，因此可以是无效值
                if (resolveResult(defaultValue) != null) {
                    argument.value = defaultValue
                }
            }
        }
        val missingArguments = arguments.values.filter { a ->
            if (a.expression.surroundsWith('$', '$')) {
                a.value.isEmpty()
            } else {
                a.value.isEmpty() && a.resolvedElement == null
            }
        }
        if (missingArguments.isNotEmpty()) {
            throw IllegalArgumentException("Missing arguments: ${missingArguments.joinToString(", ") { it.expression }}")
        }
    }

    private fun evaluateWithArguments(element: ParadoxScriptInlineMathExpression, args: Map<String, String>, arguments: Map<String, ParadoxInlineMathArgument>): MathResult {
        PsiTreeUtil.findChildOfType(element, PsiErrorElement::class.java)?.let { error ->
            val errorText = error.errorDescription.ifEmpty { "Syntax error" }
            throw IllegalStateException("Cannot evaluate: $errorText")
        }

        val tokens = mutableListOf<MathToken>()
        collectTokens(element, tokens, args, arguments)
        if (tokens.isEmpty()) throw IllegalStateException("Cannot evaluate: empty inline math expression.")
        return evaluateTokens(tokens)
    }

    private fun collectTokens(element: PsiElement, tokens: MutableList<MathToken>, args: Map<String, String>, arguments: Map<String, ParadoxInlineMathArgument>) {
        if (element is PsiWhiteSpace) return

        val operandToken = resolveOperand(element, args, arguments)
        if (operandToken != null) {
            tokens.add(operandToken)
            return
        }

        val operatorToken = resolveOperator(element)
        if (operatorToken != null) {
            tokens.add(operatorToken)
        }

        for (child in element.children()) {
            ProgressManager.checkCanceled()
            collectTokens(child, tokens, args, arguments)
        }
    }

    private fun evaluateTokens(tokens: List<MathToken>): MathResult {
        val evaluator = TokenBasedMathExpressionEvaluator(precision, isFloatingPoint)
        return evaluator.evaluate(tokens)
    }

    private fun resolveOperand(element: PsiElement, args: Map<String, String>, arguments: Map<String, ParadoxInlineMathArgument>): MathToken.Operand? {
        return when (element) {
            is ParadoxScriptInlineMathNumber -> {
                val valueText = element.value
                val number = resolveResult(valueText)
                    ?: throw IllegalStateException("Cannot evaluate: invalid number '$valueText'.")
                MathToken.Operand(number)
            }
            is ParadoxScriptInlineMathParameter -> {
                val expression = element.text?.trim()?.orNull()
                    ?: throw IllegalStateException("Cannot evaluate: parameter text is missing.")
                val argument = arguments[expression]
                    ?: throw IllegalStateException("Cannot evaluate: parameter '$expression' is not resolved.")
                val number = resolveResult(argument.value)
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
                        resolveResult(argument.value)
                            ?: throw IllegalArgumentException("Invalid argument value for '$expression': '${argument.value}'")
                    }
                    else -> {
                        val resolvedValueElement = argument.resolvedElement
                            ?: throw IllegalArgumentException("Missing arguments: $expression")
                        when (resolvedValueElement) {
                            is ParadoxScriptInlineMath -> {
                                val expressionElement = resolvedValueElement.inlineMathExpression ?: throw IllegalStateException("Cannot evaluate: empty inline math expression in '$expression'.")
                                withRecursionGuard {
                                    withRecursionCheck("sv:${argument.id}") {
                                        evaluateInternal(expressionElement, args)
                                    } ?: throw IllegalArgumentException("Recursive scripted variable reference '$expression'.")
                                } ?: throw IllegalArgumentException("Recursion detected.")
                            }
                            else -> {
                                val valueText = resolvedValueElement.text.orEmpty()
                                resolveResult(valueText)
                                    ?: throw IllegalArgumentException("Invalid scripted variable value '$valueText' for '$expression'.")
                            }
                        }
                    }
                }
                MathToken.Operand(resolvedNumber)
            }
            else -> null
        }
    }

    private fun resolveOperator(element: PsiElement): MathToken.Operator? {
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

    private fun resolveResult(text: String): MathResult? {
        val text = text.trim().orNull() ?: return null
        val result = MathResult.fromIntString(text) ?: MathResult.fromFloatString(text)
        if (result == null) return null
        this.precision?.let { result.precision = it }
        this.isFloatingPoint?.let { result.isFloatingPoint = it }
        return result
    }

    // endregion
}
