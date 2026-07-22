package icu.windea.pls.core.math

/**
 * 使用文本作为输入的数学表达式评估器。
 */
@Suppress("unused")
class TextBasedMathExpressionEvaluator(
    override var precision: Int? = null,
    override var isFloatingPoint: Boolean? = null,
) : MathExpressionEvaluator {
    /**
     * 评估来自 [text] 的数学表达式。如果发生意外，则会抛出异常。
     *
     * @throws ArithmeticException 如果在评估过程中发生任何数学异常。
     * @throws IllegalStateException 如果在评估过程中发生任何导致无法评估的异常。
     */
    fun evaluate(text: String): MathResult {
        return evaluateInternal(text)
    }

    /**
     * 评估来自 [text] 的数学表达式。如果发生意外，则会直接返回 `null`。
     */
    fun evaluateOrNull(text: String): MathResult? {
        return runCatching { evaluateInternal(text) }.getOrNull()
    }

    // region Implementations

    private fun evaluateInternal(text: String): MathResult {
        val text = text.trim()
        if (text.isEmpty()) throw IllegalStateException("Cannot evaluate: empty expression.")
        val tokens = mutableListOf<MathToken>()
        collectTokens(text, tokens)
        if (tokens.isEmpty()) throw IllegalStateException("Cannot evaluate: empty expression.")
        return evaluateTokens(tokens)
    }

    private fun collectTokens(text: String, tokens: MutableList<MathToken>) {
        val scanner = TextMathExpressionScanner(text, tokens)
        scanner.scan()
    }

    private fun evaluateTokens(tokens: List<MathToken>): MathResult {
        val evaluator = TokenBasedMathExpressionEvaluator(precision, isFloatingPoint)
        return evaluator.evaluate(tokens)
    }

    // endregion
}
