package icu.windea.pls.lang.util.calculators

abstract class MathExpressionEvaluatorBase {
    abstract fun evaluate(tokens: List<MathToken>): MathCalculationResult

    protected abstract fun toUnaryOperator(token: MathToken.Operator): MathOperator.Unary?

    protected abstract fun toBinaryOperator(token: MathToken.Operator): MathOperator.Binary?

    protected open fun validateUnary(operator: MathOperator.Unary, input: MathCalculationResult) {}

    protected open fun validateBinary(operator: MathOperator.Binary, left: MathCalculationResult, right: MathCalculationResult) {}

    protected open fun onUnaryApplied(operator: MathOperator.Unary, input: MathCalculationResult, result: MathCalculationResult) {}

    protected open fun onBinaryApplied(operator: MathOperator.Binary, left: MathCalculationResult, right: MathCalculationResult, result: MathCalculationResult) {}
}
