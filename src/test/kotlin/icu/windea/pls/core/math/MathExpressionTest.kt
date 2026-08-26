package icu.windea.pls.core.math

import org.junit.Assert
import org.junit.Test

/**
 * @see MathExpression
 */
class MathExpressionTest {
    @Test
    fun unary_and_binary_test() {
        val unary = MathExpression.Unary(MathOperator.Unary.Plus)
        Assert.assertEquals(3, unary.precedence)
        Assert.assertTrue(unary.rightAssociative)

        val binary = MathExpression.Binary(MathOperator.Binary.Times)
        Assert.assertEquals(2, binary.precedence)
        Assert.assertFalse(binary.rightAssociative)
    }

    @Test
    fun dangling_test() {
        Assert.assertEquals(-1, MathExpression.Dangling.LeftPar.precedence)
        Assert.assertFalse(MathExpression.Dangling.LeftPar.rightAssociative)
        Assert.assertEquals(-1, MathExpression.Dangling.LeftAbs.precedence)
        Assert.assertFalse(MathExpression.Dangling.LeftAbs.rightAssociative)
    }
}
