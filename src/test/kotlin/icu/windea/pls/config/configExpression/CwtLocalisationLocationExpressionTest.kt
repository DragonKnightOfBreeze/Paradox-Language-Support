package icu.windea.pls.config.configExpression

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CwtLocalisationLocationExpressionTest {
    @Test
    fun resolveEmpty() {
        val e = CwtLocalisationLocationExpression.resolveEmpty()
        assertEquals("", e.expressionString)
        assertEquals("", e.location)
        assertFalse(e.isPlaceholder)
        assertTrue(e.namePaths.isEmpty())
        assertFalse(e.forceUpperCase)
        assertEquals(e, CwtLocalisationLocationExpression.resolve(""))
    }

    @Test
    fun resolveSimpleWithoutArgs() {
        val s = "title"
        val e = CwtLocalisationLocationExpression.resolve(s)
        assertEquals(s, e.location)
        assertFalse(e.isPlaceholder)
        assertTrue(e.namePaths.isEmpty())
        assertFalse(e.forceUpperCase)
    }

    @Test
    fun resolvePlaceholder() {
        val s = "\$_desc"
        val e = CwtLocalisationLocationExpression.resolve(s)
        assertEquals(s, e.location)
        assertTrue(e.isPlaceholder)
        assertTrue(e.namePaths.isEmpty())
        assertFalse(e.forceUpperCase)
    }

    @Test
    fun resolveNamePathsArg() {
        val s = "\$_desc|\$name"
        val e = CwtLocalisationLocationExpression.resolve(s)
        assertEquals("\$_desc", e.location)
        assertEquals(setOf("name"), e.namePaths)
        assertFalse(e.forceUpperCase)
    }

    @Test
    fun resolveForceUpperCaseArg() {
        val s = "\$_desc|u"
        val e = CwtLocalisationLocationExpression.resolve(s)
        assertEquals("\$_desc", e.location)
        assertTrue(e.namePaths.isEmpty())
        assertTrue(e.forceUpperCase)
    }

    @Test
    fun resolveBothArgs() {
        val s = "\$_desc|\$name|u"
        val e = CwtLocalisationLocationExpression.resolve(s)
        assertEquals("\$_desc", e.location)
        assertEquals(setOf("name"), e.namePaths)
        assertTrue(e.forceUpperCase)
    }
}
