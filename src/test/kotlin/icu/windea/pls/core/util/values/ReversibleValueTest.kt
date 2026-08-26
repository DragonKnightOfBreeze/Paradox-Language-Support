package icu.windea.pls.core.util.values

import org.junit.Assert
import org.junit.Test

/**
 * @see ReversibleValue
 */
class ReversibleValueTest {
    @Test
    fun reversed_and_not_test() {
        val v = ReversibleValue("a", true)
        Assert.assertEquals(ReversibleValue("a", false), v.reversed())
        Assert.assertEquals(ReversibleValue("a", false), !v)
    }

    @Test
    fun takeWithOperator_test() {
        Assert.assertEquals("a", ReversibleValue("a", true).takeWithOperator())
        Assert.assertNull(ReversibleValue("a", false).takeWithOperator())
    }

    @Test
    fun withOperator_test() {
        val p: (Int) -> Boolean = { it > 0 }
        Assert.assertTrue(ReversibleValue(1, true).withOperator(p))
        Assert.assertFalse(ReversibleValue(1, false).withOperator(p))
        Assert.assertFalse(ReversibleValue(-1, true).withOperator(p))
        Assert.assertTrue(ReversibleValue(-1, false).withOperator(p))
    }

    @Test
    fun from_expression_test() {
        Assert.assertEquals(ReversibleValue("a", true), ReversibleValue.from("a"))
        Assert.assertEquals(ReversibleValue("a", false), ReversibleValue.from("!a"))
        Assert.assertEquals(ReversibleValue("a", true), ReversibleValue.from("  a  "))
        Assert.assertEquals(ReversibleValue("a", false), ReversibleValue.from("!  a"))
    }

    @Test
    fun from_expression_edge_cases_test() {
        Assert.assertEquals(ReversibleValue("!a", false), ReversibleValue.from("!!a")) // 仅取第一个 ! 表示取反
        Assert.assertEquals(ReversibleValue("", false), ReversibleValue.from("!"))
        Assert.assertEquals(ReversibleValue("", true), ReversibleValue.from(""))
        Assert.assertEquals(ReversibleValue("", true), ReversibleValue.from("   ")) // 整体 trim
        Assert.assertEquals(ReversibleValue("", false), ReversibleValue.from(" ! "))
    }
}
