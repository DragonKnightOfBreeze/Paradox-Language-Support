package icu.windea.pls.core.util

import org.junit.Assert.*
import org.junit.Test

/**
 * 纯 Kotlin 单元测试：TemplateInfo / UnaryTemplateInfo
 *
 * 覆盖点：
 * - create(...) 表达式解析（合法模板、空表达式、无占位符、多个占位符）
 * - from(...) 缓存与去重
 * - resolve(...) 占位符代入
 * - extract(...) 完整文本提取（正常提取、前后缀不匹配、文本过短、边界情况）
 *
 * @see TemplateInfo
 * @see UnaryTemplateInfo
 */
class TemplateInfoTest {
    // --------------- UnaryTemplateInfo.create ---------------

    @Test
    fun testCreate_validTemplates() {
        val t1 = UnaryTemplateInfo.create($$"$_desc")!!
        assertEquals($$"$_desc", t1.expression)
        assertEquals("", t1.prefix)
        assertEquals("_desc", t1.suffix)

        val t2 = UnaryTemplateInfo.create("GFX_$")!!
        assertEquals("GFX_", t2.prefix)
        assertEquals("", t2.suffix)

        val t3 = UnaryTemplateInfo.create($$"c_$_d")!!
        assertEquals("c_", t3.prefix)
        assertEquals("_d", t3.suffix)

        // a template consisting of only the placeholder is also valid
        val t4 = UnaryTemplateInfo.create("$")!!
        assertEquals("", t4.prefix)
        assertEquals("", t4.suffix)
    }

    @Test
    fun testCreate_invalidTemplates_returnNull() {
        assertNull(UnaryTemplateInfo.create(""))
        assertNull(UnaryTemplateInfo.create("abc"))
        assertNull(UnaryTemplateInfo.create($$"a_$b_$c")) // multiple placeholders
    }

    // --------------- UnaryTemplateInfo.from ---------------

    @Test
    fun testFrom_cachingAndEquality() {
        val t = UnaryTemplateInfo.from($$"$_desc")!!
        // cached by expression string
        assertSame(t, UnaryTemplateInfo.from($$"$_desc"))
        assertEquals(UnaryTemplateInfo($$"$_desc"), t)
        assertNull(UnaryTemplateInfo.from("abc"))
    }

    // --------------- UnaryTemplateInfo.resolve ---------------

    @Test
    fun testResolve() {
        assertEquals("test_desc", UnaryTemplateInfo($$"$_desc").resolve("test"))
        assertEquals("GFX_test", UnaryTemplateInfo("GFX_$").resolve("test"))
        assertEquals("c_test_d", UnaryTemplateInfo($$"c_$_d").resolve("test"))
        assertEquals("test", UnaryTemplateInfo("$").resolve("test"))
    }

    // --------------- UnaryTemplateInfo.extract ---------------

    @Test
    fun testExtract() {
        assertEquals("test", UnaryTemplateInfo($$"$_desc").extract("test_desc"))
        assertEquals("test", UnaryTemplateInfo("GFX_$").extract("GFX_test"))
        assertEquals("test", UnaryTemplateInfo($$"c_$_d").extract("c_test_d"))
        // extra content between prefix and suffix is preserved as the extracted value
        assertEquals("te_st", UnaryTemplateInfo($$"$_desc").extract("te_st_desc"))
        // a template consisting of only the placeholder extracts the whole text
        assertEquals("test", UnaryTemplateInfo("$").extract("test"))
    }

    @Test
    fun testExtract_notMatched_returnNull() {
        assertNull(UnaryTemplateInfo($$"$_desc").extract("test")) // missing suffix
        assertNull(UnaryTemplateInfo($$"$_desc").extract("test_desc_extra")) // trailing content
        assertNull(UnaryTemplateInfo("GFX_$").extract("test")) // missing prefix
        assertNull(UnaryTemplateInfo($$"c_$_d").extract("c__")) // too short
        assertNull(UnaryTemplateInfo($$"c_$_d").extract("c_xe")) // missing suffix
    }

    @Test
    fun testExtract_emptyResultIsAllowed() {
        // prefix and suffix match exactly, so the extracted value is empty
        assertEquals("", UnaryTemplateInfo($$"$_desc").extract("_desc"))
        assertEquals("", UnaryTemplateInfo("GFX_$").extract("GFX_"))
    }
}
