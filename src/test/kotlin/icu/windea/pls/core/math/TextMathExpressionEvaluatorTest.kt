package icu.windea.pls.core.math

import org.junit.Assert
import org.junit.Test

class TextMathExpressionEvaluatorTest {
    @Test
    fun evaluate_simpleTest_1() {
        val source = "2.2"
        val expect = MathResult.from(2.2)
        assertEvaluate(expect, source)
    }

    @Test
    fun evaluate_simpleTest_2() {
        val source = "2.2 + 3.3"
        val expect = MathResult.from(5.5)
        assertEvaluate(expect, source)
    }

    @Test
    fun evaluate_simpleTest_3() {
        val source = "2. + .3"
        val expect = MathResult.from(2.3)
        assertEvaluate(expect, source)
    }

    @Test
    fun evaluate_simpleTest_failed() {
        val source = "2.2 + 3.3 = ?"
        assertEvaluateFailed(IllegalStateException::class.java, source)
    }

    @Test
    fun evaluate_simpleTest_failed_2() {
        val source = "2.2 + 3.3 + 4.5.6"
        assertEvaluateFailed(IllegalStateException::class.java, source)
    }

    @Test
    fun evaluate_complexTest_1() {
        val source = " 1 + 2 * 3 + (10 / 2 % 3 ^ 2) - | 1.0 - 2.0 | " // = 10.0
        val expect = MathResult.from(10.0)
        assertEvaluate(expect, source)
    }

    private fun assertEvaluate(expect: MathResult, source: String) {
        Assert.assertEquals(expect, TextMathExpressionEvaluator().evaluate(source))
    }

    private fun assertEvaluateFailed(expect: Class<out Throwable>, source: String) {
        Assert.assertThrows(expect) { TextMathExpressionEvaluator().evaluate(source) }
    }
}
