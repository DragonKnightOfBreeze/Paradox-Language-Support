package icu.windea.pls.config.expression

import org.junit.*

class CwtTemplateExpressionTest {
    @Test
    fun testResolve() {
        val e = CwtTemplateExpression.resolve("foo")
        Assert.assertTrue(e.expressionString.isNotEmpty())
        val e1 = CwtTemplateExpression.resolve("ant:foo*")
        Assert.assertTrue(e1.expressionString.isNotEmpty())
        val e2 = CwtTemplateExpression.resolve("re:foo.*")
        Assert.assertTrue(e2.expressionString.isNotEmpty())
        val e3 = CwtTemplateExpression.resolve("foo_<bar>_enum[abc]_value[def]")
        Assert.assertTrue(e3.expressionString.isNotEmpty())
        val e4 = CwtTemplateExpression.resolve("ant:foo_<bar>_enum[abc]_value[def]")
        Assert.assertTrue(e4.expressionString.isNotEmpty())
        val e5 = CwtTemplateExpression.resolve("re:foo_<bar>_enum[abc]_value[def]")
        Assert.assertTrue(e5.expressionString.isNotEmpty())
        println()
    }
}