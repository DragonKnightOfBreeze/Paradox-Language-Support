package icu.windea.pls.config.configExpression

import org.junit.Assert.*
import org.junit.Test

class CwtImageLocationExpressionTest {
    @Test
    fun resolveEmpty() {
        val e = CwtImageLocationExpression.resolveEmpty()
        assertEquals("", e.expressionString)
        assertEquals("", e.location)
        assertFalse(e.isPlaceholder)
        assertTrue(e.namePaths.isEmpty())
        assertTrue(e.framePaths.isEmpty())
        assertEquals(e, CwtImageLocationExpression.resolve(""))
    }

    @Test
    fun resolveSimplePathWithoutArgs() {
        val s = "gfx/interface/icons/modifiers/mod_icon.dds"
        val e = CwtImageLocationExpression.resolve(s)
        assertEquals(s, e.expressionString)
        assertEquals(s, e.location)
        assertFalse(e.isPlaceholder)
        assertTrue(e.namePaths.isEmpty())
        assertTrue(e.framePaths.isEmpty())
        assertEquals(s, e.toString())
    }

    @Test
    fun resolvePlaceholderPath() {
        val s = "gfx/interface/icons/modifiers/mod_\$.dds"
        val e = CwtImageLocationExpression.resolve(s)
        assertEquals(s, e.location)
        assertTrue(e.isPlaceholder)
        assertTrue(e.namePaths.isEmpty())
        assertTrue(e.framePaths.isEmpty())
    }

    @Test
    fun resolveNamePathsArg() {
        val s = "gfx/interface/icons/modifiers/mod_icon.dds|\$name"
        val e = CwtImageLocationExpression.resolve(s)
        assertEquals("gfx/interface/icons/modifiers/mod_icon.dds", e.location)
        assertEquals(setOf("name"), e.namePaths)
        assertTrue(e.framePaths.isEmpty())
        assertFalse(e.isPlaceholder)
    }

    @Test
    fun resolveFramePathsArg() {
        val s = "icon|p1,p2"
        val e = CwtImageLocationExpression.resolve(s)
        assertEquals("icon", e.location)
        assertTrue(e.namePaths.isEmpty())
        assertEquals(setOf("p1", "p2"), e.framePaths)
        assertFalse(e.isPlaceholder)
    }

    @Test
    fun resolveBothArgs() {
        val s = "icon|\$name|p1,p2"
        val e = CwtImageLocationExpression.resolve(s)
        assertEquals("icon", e.location)
        assertEquals(setOf("name"), e.namePaths)
        assertEquals(setOf("p1", "p2"), e.framePaths)
        assertFalse(e.isPlaceholder)
    }
}
