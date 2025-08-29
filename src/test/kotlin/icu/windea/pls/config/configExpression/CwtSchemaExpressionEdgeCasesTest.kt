package icu.windea.pls.config.configExpression

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CwtSchemaExpressionEdgeCasesTest {
    @Test
    fun resolveTypeEmptyName() {
        val s = "${'$'}"
        val e = CwtSchemaExpression.resolve(s)
        assertTrue(e is CwtSchemaExpression.Type)
        val t = e as CwtSchemaExpression.Type
        assertEquals("", t.name)
        assertEquals(s, e.toString())
    }

    @Test
    fun resolveConstraintEmptyName() {
        val s = "${'$'}${'$'}"
        val e = CwtSchemaExpression.resolve(s)
        assertTrue(e is CwtSchemaExpression.Constraint)
        val c = e as CwtSchemaExpression.Constraint
        assertEquals("", c.name)
        assertEquals(s, e.toString())
    }

    @Test
    fun resolveTemplateTakesPrecedenceOverTypeWhenTwoDollars() {
        val s = "${'$'}any${'$'}"
        val e = CwtSchemaExpression.resolve(s)
        assertTrue(e is CwtSchemaExpression.Template)
        val t = e as CwtSchemaExpression.Template
        assertEquals("*", t.pattern)
        val parts = t.parameterRanges.map { r -> s.substring(r.startOffset, r.endOffset) }
        assertEquals(listOf("${'$'}any${'$'}"), parts)
        assertEquals(s, e.toString())
    }

    @Test
    fun resolveEnumInsideLargerStringBecomesTemplate() {
        val s = "prefix ${'$'}enum:ship_size${'$'} suffix"
        val e = CwtSchemaExpression.resolve(s)
        assertTrue(e is CwtSchemaExpression.Template)
        val t = e as CwtSchemaExpression.Template
        assertEquals("prefix * suffix", t.pattern)
        val parts = t.parameterRanges.map { r -> s.substring(r.startOffset, r.endOffset) }
        assertEquals(listOf("${'$'}enum:ship_size${'$'}"), parts)
    }

    @Test
    fun resolveTemplateWithEscapedDollarNotStarred() {
        val s = """a \${'$'}x\${'$'} b ${'$'}y${'$'} c"""
        val e = CwtSchemaExpression.resolve(s)
        assertTrue(e is CwtSchemaExpression.Template)
        val t = e as CwtSchemaExpression.Template
        assertEquals("""a \${'$'}x\${'$'} b * c""", t.pattern)
    }

    @Test
    fun resolveOddDollarsConstraintLikeFallbackToConstant() {
        val s = "${'$'}${'$'}custom${'$'}"
        val e = CwtSchemaExpression.resolve(s)
        assertTrue(e is CwtSchemaExpression.Constant)
        assertEquals(s, e.expressionString)
    }

    @Test
    fun resolveEscapedDollarsFallbackToConstant() {
        // 只有转义美元（\\$）不视为参数占位，按当前实现不构成模板
        val s = """a \${'$'}x\${'$'} b"""
        val e = CwtSchemaExpression.resolve(s)
        assertTrue(e is CwtSchemaExpression.Constant)
        assertEquals(s, e.expressionString)
    }
}
