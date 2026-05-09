package icu.windea.pls.core.math

import org.junit.Assert
import org.junit.Test

class TextMathExpressionScannerTest {
    @Test
    fun scan_simpleTest_1() {
        val source = "2.2"
        val expect = listOf(
            MathToken.Operand(MathResult.from(2.2)),
        )
        assertScan(expect, source)
    }

    @Test
    fun scan_simpleTest_2() {
        val source = "2.2 + 3.3"
        val expect = listOf(
            MathToken.Operand(MathResult.from(2.2)),
            MathToken.Operator.Plus,
            MathToken.Operand(MathResult.from(3.3)),
        )
        assertScan(expect, source)
    }

    @Test
    fun scan_simpleTest_3() {
        val source = "2. + .3"
        val expect = listOf(
            MathToken.Operand(MathResult.from(2.0)),
            MathToken.Operator.Plus,
            MathToken.Operand(MathResult.from(0.3)),
        )
        assertScan(expect, source)
    }

    @Test
    fun scan_simpleTest_failed_1() {
        val source = "2.2 + 3.3 = ?"
        assertScanFailed(IllegalStateException::class.java, source)
    }

    @Test
    fun scan_simpleTest_failed_2() {
        val source = "2.2 + 3.3 + 4.5.6"
        assertScanFailed(IllegalStateException::class.java, source)
    }

    @Test
    fun scan_complexTest_1() {
        val source = " 1 + 2 * 3 + (10 / 2 % 3 ^ 2) - | 1.0 - 2.0 | " // = 10.0
        val expect = listOf(
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
        assertScan(expect, source)
    }

    private fun assertScan(expect: List<MathToken>, source: String) {
        Assert.assertEquals(expect, TextMathExpressionScanner(source).scan())
    }

    private fun assertScanFailed(expect: Class<out Throwable>, source: String) {
        Assert.assertThrows(expect) { TextMathExpressionScanner(source).scan() }
    }
}
