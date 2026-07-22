package icu.windea.pls.core.math

import org.junit.Assert
import org.junit.Test

/**
 * @see TokenBasedMathExpressionEvaluator
 */
class TokenBasedMathExpressionEvaluatorTest {
    @Test
    fun evaluate_simpleTest_1() {
        val source = listOf(
            MathToken.Operand(MathResult.from(2.2)),
        )
        val expect = MathResult.from(2.2)
        assertEvaluate(expect, source)
    }

    @Test
    fun evaluate_simpleTest_2() {
        val source = listOf(
            MathToken.Operand(MathResult.from(2.2)),
            MathToken.Operator.Plus,
            MathToken.Operand(MathResult.from(3.3)),
        )
        val expect = MathResult.from(5.5)
        assertEvaluate(expect, source)
    }

    @Test
    fun evaluate_simpleTest_3() {
        val source = listOf(
            MathToken.Operand(MathResult.from(2.0)),
            MathToken.Operator.Plus,
            MathToken.Operand(MathResult.from(0.3)),
        )
        val expect = MathResult.from(2.3)
        assertEvaluate(expect, source)
    }

    @Test
    fun evaluate_complexTest_1() {
        val source = listOf(
            MathToken.Operand(MathResult.from(1)),
            MathToken.Operator.Plus,
            MathToken.Operand(MathResult.from(2)),
            MathToken.Operator.Times,
            MathToken.Operand(MathResult.from(3)),
            MathToken.Operator.Plus,
            MathToken.Operator.LeftPar,
            MathToken.Operand(MathResult.from(10)),
            MathToken.Operator.Div,
            MathToken.Operand(MathResult.from(2)),
            MathToken.Operator.Mod,
            MathToken.Operand(MathResult.from(3)),
            MathToken.Operator.Pow,
            MathToken.Operand(MathResult.from(2)),
            MathToken.Operator.RightPar,
            MathToken.Operator.Minus,
            MathToken.Operator.LeftAbs,
            MathToken.Operand(MathResult.from(1.0)),
            MathToken.Operator.Minus,
            MathToken.Operand(MathResult.from(2.0)),
            MathToken.Operator.RightAbs,
        )
        val expect = MathResult.from(10.0)
        assertEvaluate(expect, source)
    }

    private fun assertEvaluate(expect: MathResult, source: List<MathToken>) {
        Assert.assertEquals(expect, TokenBasedMathExpressionEvaluator().evaluate(source))
    }
}
