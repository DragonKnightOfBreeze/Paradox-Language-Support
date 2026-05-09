package icu.windea.pls.core.math

import icu.windea.pls.core.isExactDigit
import icu.windea.pls.core.util.ToggleMarker

/**
 * 使用文本作为输入的数学表达式的扫描器。
 */
class TextMathExpressionScanner(
    val source: String,
    val tokens: MutableList<MathToken> = mutableListOf(),
    var precision: Int? = null,
    var isFloatingPoint: Boolean? = null,
) {
    private var start = 0
    private var current = 0
    private val pipeMarker = ToggleMarker(true)

    /**
     * 扫描输入的文本，将产生的标记加入并返回标记序列。
     *
     * 备注：
     * - 输入的文本允许包含包括换行在内的任意空白，并在扫描时被忽略。
     * - 允许以前导的零开始的数字字面量。
     * - 允许以小数点开始或结尾的数字字面量。
     *
     * @throws IllegalStateException 如果在扫描过程中发生任何词法错误。
     */
    fun scan(): List<MathToken> {
        scanTokens()
        return tokens
    }

    private fun scanTokens() {
        while (!end()) {
            start = current
            scanToken()
        }
    }

    private fun scanToken() {
        val c = advance()
        when (c) {
            '+' -> addOperatorToken(MathToken.Operator.Plus)
            '-' -> addOperatorToken(MathToken.Operator.Minus)
            '*' -> addOperatorToken(MathToken.Operator.Times)
            '/' -> addOperatorToken(MathToken.Operator.Div)
            '%' -> addOperatorToken(MathToken.Operator.Mod)
            '^' -> addOperatorToken(MathToken.Operator.Pow)
            '|' -> addOperatorToken(if (pipeMarker.mark()) MathToken.Operator.LeftAbs else MathToken.Operator.RightAbs)
            '(' -> addOperatorToken(MathToken.Operator.LeftPar)
            ')' -> addOperatorToken(MathToken.Operator.RightPar)
            else -> {
                when {
                    c == '.' -> scanNumber(expectDot = false)
                    c.isExactDigit() -> scanNumber()
                    c.isWhitespace() -> return
                    else -> throw IllegalStateException("Unexpected character '$c' at offset $start")
                }
            }
        }
    }

    private fun scanNumber(expectDot: Boolean = true) {
        var isDouble = !expectDot
        while (peek().isExactDigit()) advance()
        if (expectDot && peek() == '.') {
            isDouble = true
            advance()
            while (peek().isExactDigit()) advance()
        }
        if (isDouble && peek() == '.') {
            // e.g., `1.2.3`
            throw IllegalStateException("Unexpected character '.' at offset $current")
        }
        val text = source.substring(start, current)
        val result = resolveRsult(text, isDouble)
        addOperandToken(result)
    }

    private fun resolveRsult(text: String, isDouble: Boolean): MathResult {
        val result = if (isDouble) MathResult.fromDoubleString(text) else MathResult.fromLongString(text)
        if (result == null) throw IllegalStateException("Cannot resolve number literal from text: $text")
        this.precision?.let { result.precision = it }
        this.isFloatingPoint?.let { result.isFloatingPoint = it }
        return result
    }

    private fun end(): Boolean {
        return current >= source.length
    }

    private fun advance(): Char {
        return source[current++]
    }

    private fun peek(): Char {
        if (current >= source.length) return '\u0000'
        return source[current]
    }

    private fun addOperandToken(result: MathResult) {
        tokens.add(MathToken.Operand(result))
    }

    private fun addOperatorToken(token: MathToken.Operator) {
        tokens.add(token)
    }
}
