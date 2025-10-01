package icu.windea.pls.config.configExpression

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.ep.configExpression.CwtDataExpressionResolver
import org.junit.Assert.*

class CwtTemplateExpressionTest : BasePlatformTestCase() {
    private fun hasEp(): Boolean = try {
        CwtDataExpressionResolver.EP_NAME.extensionList.isNotEmpty()
    } catch (_: Throwable) { false }

    fun testResolveEmpty() {
        val e = CwtTemplateExpression.resolveEmpty()
        assertEquals("", e.expressionString)
        assertTrue(e.snippetExpressions.isEmpty())
        assertTrue(e.referenceExpressions.isEmpty())
        assertEquals(e, CwtTemplateExpression.resolve(""))
        assertSame(e, CwtTemplateExpression.resolveEmpty())
    }

    fun testResolveConstant_returnsEmptyExpression() {
        if (!hasEp()) return
        val s = "hello"
        val e = CwtTemplateExpression.resolve(s)
        assertEquals("", e.expressionString)
        assertTrue(e.snippetExpressions.isEmpty())
        assertTrue(e.referenceExpressions.isEmpty())
    }

    fun testResolveDynamic_withoutBlanks_producesSnippets() {
        if (!hasEp()) return
        val s = "a_value[foo]_b" // uses RuleBased dynamic rule: value[...]
        val e = CwtTemplateExpression.resolve(s)
        assertEquals(s, e.expressionString)
        assertEquals(3, e.snippetExpressions.size)
        assertEquals(1, e.referenceExpressions.size)
        // snippet 0: constant "a_"
        assertEquals(CwtDataTypes.Constant, e.snippetExpressions[0].type)
        assertEquals("a_", e.snippetExpressions[0].value)
        // snippet 1: dynamic value[foo]
        assertEquals(CwtDataTypes.Value, e.snippetExpressions[1].type)
        assertEquals("foo", e.snippetExpressions[1].value)
        // snippet 2: constant "_b"
        assertEquals(CwtDataTypes.Constant, e.snippetExpressions[2].type)
        assertEquals("_b", e.snippetExpressions[2].value)

        assertEquals(s, e.toString())
        assertSame(e, CwtTemplateExpression.resolve(s)) // cached
        assertEquals(e.hashCode(), CwtTemplateExpression.resolve(s).hashCode())
    }

    fun testResolveDynamic_atStart_and_atEnd() {
        if (!hasEp()) return
        val s1 = "value[foo]_b"
        val e1 = CwtTemplateExpression.resolve(s1)
        assertEquals(2, e1.snippetExpressions.size)
        assertEquals(CwtDataTypes.Value, e1.snippetExpressions[0].type)
        assertEquals("foo", e1.snippetExpressions[0].value)
        assertEquals(CwtDataTypes.Constant, e1.snippetExpressions[1].type)
        assertEquals("_b", e1.snippetExpressions[1].value)

        val s2 = "a_value[foo]"
        val e2 = CwtTemplateExpression.resolve(s2)
        assertEquals(2, e2.snippetExpressions.size)
        assertEquals(CwtDataTypes.Constant, e2.snippetExpressions[0].type)
        assertEquals("a_", e2.snippetExpressions[0].value)
        assertEquals(CwtDataTypes.Value, e2.snippetExpressions[1].type)
        assertEquals("foo", e2.snippetExpressions[1].value)
    }

    fun testResolveDynamic_adjacentSegments() {
        if (!hasEp()) return
        val s = "a_value[foo]value[bar]_b"
        val e = CwtTemplateExpression.resolve(s)
        assertEquals(s, e.expressionString)
        assertEquals(4, e.snippetExpressions.size)
        assertEquals(2, e.referenceExpressions.size)
        assertEquals(CwtDataTypes.Constant, e.snippetExpressions[0].type)
        assertEquals("a_", e.snippetExpressions[0].value)
        assertEquals(CwtDataTypes.Value, e.snippetExpressions[1].type)
        assertEquals("foo", e.snippetExpressions[1].value)
        assertEquals(CwtDataTypes.Value, e.snippetExpressions[2].type)
        assertEquals("bar", e.snippetExpressions[2].value)
        assertEquals(CwtDataTypes.Constant, e.snippetExpressions[3].type)
        assertEquals("_b", e.snippetExpressions[3].value)
    }

    fun testResolveDynamic_multipleInMiddle() {
        if (!hasEp()) return
        val s = "foo_value[bar]_value[baz]_qux"
        val e = CwtTemplateExpression.resolve(s)
        assertEquals(5, e.snippetExpressions.size)
        assertEquals(2, e.referenceExpressions.size)
        assertEquals(CwtDataTypes.Constant, e.snippetExpressions[0].type)
        assertEquals("foo_", e.snippetExpressions[0].value)
        assertEquals(CwtDataTypes.Value, e.snippetExpressions[1].type)
        assertEquals("bar", e.snippetExpressions[1].value)
        assertEquals(CwtDataTypes.Constant, e.snippetExpressions[2].type)
        assertEquals("_", e.snippetExpressions[2].value)
        assertEquals(CwtDataTypes.Value, e.snippetExpressions[3].type)
        assertEquals("baz", e.snippetExpressions[3].value)
        assertEquals(CwtDataTypes.Constant, e.snippetExpressions[4].type)
        assertEquals("_qux", e.snippetExpressions[4].value)
    }

    fun testResolve_containsBlank_returnsEmptyExpression() {
        val s = "a value[foo] b"
        val e = CwtTemplateExpression.resolve(s)
        assertEquals("", e.expressionString)
        assertTrue(e.snippetExpressions.isEmpty())
        assertTrue(e.referenceExpressions.isEmpty())
    }

    fun testResolve_cachingDifferentInputs() {
        if (!hasEp()) return
        val e1 = CwtTemplateExpression.resolve("a_value[foo]_b")
        val e2 = CwtTemplateExpression.resolve("a_value[bar]_b")
        assertNotEquals(e1, e2)
    }

    fun testResolveDynamic_definitionAngleBrackets() {
        if (!hasEp()) return
        val s = "job_<foo>_add" // uses DynamicRule: <...>
        val e = CwtTemplateExpression.resolve(s)
        assertEquals(s, e.expressionString)
        assertEquals(3, e.snippetExpressions.size)
        assertEquals(1, e.referenceExpressions.size)
        assertEquals(CwtDataTypes.Constant, e.snippetExpressions[0].type)
        assertEquals("job_", e.snippetExpressions[0].value)
        assertEquals(CwtDataTypes.Definition, e.snippetExpressions[1].type)
        assertEquals("foo", e.snippetExpressions[1].value)
        assertEquals(CwtDataTypes.Constant, e.snippetExpressions[2].type)
        assertEquals("_add", e.snippetExpressions[2].value)
    }

    fun testResolveDynamic_iconWithGamePrefixStripped() {
        if (!hasEp()) return
        val s = "a_icon[game/ui/icon.dds]_b" // Core resolver removes game/ prefix
        val e = CwtTemplateExpression.resolve(s)
        assertEquals(3, e.snippetExpressions.size)
        assertEquals(1, e.referenceExpressions.size)
        assertEquals(CwtDataTypes.Constant, e.snippetExpressions[0].type)
        assertEquals("a_", e.snippetExpressions[0].value)
        assertEquals(CwtDataTypes.Icon, e.snippetExpressions[1].type)
        assertEquals("ui/icon.dds", e.snippetExpressions[1].value)
        assertEquals(CwtDataTypes.Constant, e.snippetExpressions[2].type)
        assertEquals("_b", e.snippetExpressions[2].value)
    }

    fun testResolveDynamic_scopeAnyValueNull() {
        if (!hasEp()) return
        val s = "a_scope[any]_b" // scope[any] -> value should be null
        val e = CwtTemplateExpression.resolve(s)
        assertEquals(3, e.snippetExpressions.size)
        assertEquals(1, e.referenceExpressions.size)
        assertEquals(CwtDataTypes.Scope, e.snippetExpressions[1].type)
        assertNull(e.snippetExpressions[1].value)
    }

    fun testResolveDynamic_enumAndValueMix() {
        if (!hasEp()) return
        val s = "x_enum[PLANET_CLASS]_value[foo]_y"
        val e = CwtTemplateExpression.resolve(s)
        assertEquals(5, e.snippetExpressions.size)
        assertEquals(2, e.referenceExpressions.size)
        assertEquals(CwtDataTypes.Constant, e.snippetExpressions[0].type)
        assertEquals("x_", e.snippetExpressions[0].value)
        assertEquals(CwtDataTypes.EnumValue, e.snippetExpressions[1].type)
        assertEquals("PLANET_CLASS", e.snippetExpressions[1].value)
        assertEquals(CwtDataTypes.Constant, e.snippetExpressions[2].type)
        assertEquals("_", e.snippetExpressions[2].value)
        assertEquals(CwtDataTypes.Value, e.snippetExpressions[3].type)
        assertEquals("foo", e.snippetExpressions[3].value)
        assertEquals(CwtDataTypes.Constant, e.snippetExpressions[4].type)
        assertEquals("_y", e.snippetExpressions[4].value)
    }

    fun testResolve_singleDynamicOnly_returnsEmptyExpression() {
        if (!hasEp()) return
        val s = "value[foo]"
        val e = CwtTemplateExpression.resolve(s)
        assertEquals("", e.expressionString)
        assertTrue(e.snippetExpressions.isEmpty())
        assertTrue(e.referenceExpressions.isEmpty())
    }
}
