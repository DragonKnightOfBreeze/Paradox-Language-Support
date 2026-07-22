package icu.windea.pls.core.math

/**
 * 数学表达式的评估器。
 *
 * @property precision 评估结果的精确度。用于格式化。
 * @property isFloatingPoint 评估结果是否是浮点数。用于规范化和格式化。
 *
 * @see MathResult
 * @see TokenBasedMathExpressionEvaluator
 * @see TextBasedMathExpressionEvaluator
 */
interface MathExpressionEvaluator {
    var precision: Int?
    var isFloatingPoint: Boolean?
}
