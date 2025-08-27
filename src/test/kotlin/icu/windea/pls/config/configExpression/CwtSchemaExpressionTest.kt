package icu.windea.pls.config.configExpression

import org.junit.Assert.*
import org.junit.Test

class CwtSchemaExpressionTest {
    @Test
    fun resolveEmpty() {
        val e = CwtSchemaExpression.resolveEmpty()
        assertTrue(e is CwtSchemaExpression.Constant)
        assertEquals("", e.expressionString)
        assertEquals(e, CwtSchemaExpression.resolve(""))
    }

    @Test
    fun resolveConstant() {
        val s = "some_constant"
        val e = CwtSchemaExpression.resolve(s)
        assertTrue(e is CwtSchemaExpression.Constant)
        assertEquals(s, e.expressionString)
        assertEquals(e, CwtSchemaExpression.resolve(s))
        assertEquals(e.hashCode(), CwtSchemaExpression.resolve(s).hashCode())
        assertEquals(s, e.toString())
    }

    @Test
    fun resolveType() {
        val e = CwtSchemaExpression.resolve("\$any")
        assertTrue(e is CwtSchemaExpression.Type)
        val t = e as CwtSchemaExpression.Type
        assertEquals("any", t.name)
    }

    @Test
    fun resolveConstraint() {
        val e = CwtSchemaExpression.resolve("\$\$custom")
        assertTrue(e is CwtSchemaExpression.Constraint)
        val c = e as CwtSchemaExpression.Constraint
        assertEquals("custom", c.name)
    }

    @Test
    fun resolveEnum() {
        val e = CwtSchemaExpression.resolve("\$enum:ship_size\$")
        assertTrue(e is CwtSchemaExpression.Enum)
        val en = e as CwtSchemaExpression.Enum
        assertEquals("ship_size", en.name)
    }

    @Test
    fun resolveTemplate() {
        val s = "a \$x\$ b \$y\$ c"
        val e = CwtSchemaExpression.resolve(s)
        assertTrue(e is CwtSchemaExpression.Template)
        val t = e as CwtSchemaExpression.Template
        assertEquals("a * b * c", t.pattern)
        val parts = t.parameterRanges.map { r -> s.substring(r.startOffset, r.endOffset) }
        assertEquals(listOf("\$x\$", "\$y\$"), parts)
    }

    @Test
    fun resolveOddDollarsFallbackToConstant() {
        val s = "a \$x"
        val e = CwtSchemaExpression.resolve(s)
        assertTrue(e is CwtSchemaExpression.Constant)
        assertEquals(s, e.expressionString)
    }
}
