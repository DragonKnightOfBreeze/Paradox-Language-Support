package icu.windea.pls.core.math

import org.junit.Assert
import org.junit.Test

/**
 * @see MathOperator
 */
class MathOperatorTest {
    @Test
    fun unary_precedence_and_associativity_test() {
        Assert.assertEquals(3, MathOperator.Unary.Plus.precedence)
        Assert.assertEquals(3, MathOperator.Unary.Minus.precedence)
        Assert.assertEquals(3, MathOperator.Unary.Abs.precedence)
        Assert.assertTrue(MathOperator.Unary.Plus.rightAssociative)
        Assert.assertTrue(MathOperator.Unary.Minus.rightAssociative)
        Assert.assertTrue(MathOperator.Unary.Abs.rightAssociative)
    }

    @Test
    fun binary_precedence_and_associativity_test() {
        Assert.assertEquals(1, MathOperator.Binary.Plus.precedence)
        Assert.assertEquals(1, MathOperator.Binary.Minus.precedence)
        Assert.assertEquals(2, MathOperator.Binary.Times.precedence)
        Assert.assertEquals(2, MathOperator.Binary.Div.precedence)
        Assert.assertEquals(2, MathOperator.Binary.Mod.precedence)
        Assert.assertEquals(2, MathOperator.Binary.Pow.precedence)
        Assert.assertFalse(MathOperator.Binary.Plus.rightAssociative)
        Assert.assertFalse(MathOperator.Binary.Times.rightAssociative)
    }
}
