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
}
