package icu.windea.pls.core.math

/**
 * 使用文本作为输入的数学表达式的评估器。
 */
class TextMathExpressionEvaluator(
    var precision: Int? = null,
    var isFloatingPoint: Boolean? = null,
) {
    /**
     * @throws ArithmeticException 如果在评估过程中发生任何数学异常。
     * @throws IllegalStateException 如果在评估过程中发生任何导致无法评估的异常。
     */
    fun evaluate(text: String): MathResult {
        return evaluateInternal(text)
    }

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
        val evaluator = DefaultMathExpressionEvaluator(precision, isFloatingPoint)
        return evaluator.evaluate(tokens)
    }
}
