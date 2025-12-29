package icu.windea.pls.lang.util.evaluators

abstract class MathExpressionEvaluatorBase {
    abstract fun evaluate(tokens: List<MathToken>): MathResult

    protected abstract fun toUnaryOperator(token: MathToken.Operator): MathOperator.Unary?

    protected abstract fun toBinaryOperator(token: MathToken.Operator): MathOperator.Binary?

    protected open fun validateUnary(operator: MathOperator.Unary, input: MathResult) {}

    protected open fun validateBinary(operator: MathOperator.Binary, left: MathResult, right: MathResult) {}

    protected open fun onUnaryApplied(operator: MathOperator.Unary, input: MathResult, result: MathResult) {}

    protected open fun onBinaryApplied(operator: MathOperator.Binary, left: MathResult, right: MathResult, result: MathResult) {}
}
