package icu.windea.pls.core.math

import org.junit.Assert
import org.junit.Test

/**
 * @see MathToken
 */
class MathTokenTest {
    @Test
    fun operand_render_test() {
        Assert.assertEquals("2", MathToken.Operand(MathResult.from(2)).render())
        Assert.assertEquals("2.5", MathToken.Operand(MathResult.from(2.5)).render())
    }

    @Test
    fun operator_render_test() {
        Assert.assertEquals("+", MathToken.Operator.Plus.render())
        Assert.assertEquals("-", MathToken.Operator.Minus.render())
        Assert.assertEquals("*", MathToken.Operator.Times.render())
        Assert.assertEquals("/", MathToken.Operator.Div.render())
        Assert.assertEquals("%", MathToken.Operator.Mod.render())
        Assert.assertEquals("^", MathToken.Operator.Pow.render())
        Assert.assertEquals("|", MathToken.Operator.LeftAbs.render())
        Assert.assertEquals("|", MathToken.Operator.RightAbs.render())
        Assert.assertEquals("(", MathToken.Operator.LeftPar.render())
        Assert.assertEquals(")", MathToken.Operator.RightPar.render())
    }
}
