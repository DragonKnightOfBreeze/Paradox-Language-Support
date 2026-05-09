package icu.windea.pls.core.math

/**
 * 数学表达式的评估器。
 *
 * @property precision 评估结果的精确度。用于格式化。
 * @property isFloatingPoint 评估结果是否是浮点数。用于规范化和格式化。
 */
interface MathExpressionEvaluator {
    var precision: Int?
    var isFloatingPoint: Boolean?

    fun evaluate(tokens: List<MathToken>): MathResult

    fun toUnaryOperator(token: MathToken.Operator): MathOperator.Unary?

    fun toBinaryOperator(token: MathToken.Operator): MathOperator.Binary?

    fun evaluateUnaryOperator(operator: MathOperator.Unary, input: MathResult): MathResult

    fun evaluateBinaryOperator(operator: MathOperator.Binary, left: MathResult, right: MathResult): MathResult
}
