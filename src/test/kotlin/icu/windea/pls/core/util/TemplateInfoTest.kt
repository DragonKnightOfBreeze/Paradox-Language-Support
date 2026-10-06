package icu.windea.pls.core.util

import icu.windea.pls.test.dsl.expectScope
import org.junit.Test

/**
 * 纯 Kotlin 单元测试：[TemplateInfo] / [UnaryTemplateInfo]。
 *
 * 覆盖点：
 * - [UnaryTemplateInfo.create] 表达式解析（合法模板、空表达式、无占位符、多个占位符）
 * - `normalize` 去重
 * - [UnaryTemplateInfo.resolve] 占位符代入
 * - [UnaryTemplateInfo.extract] 完整文本提取（正常提取、前后缀不匹配、文本过短、边界情况）
 *
 * @see TemplateInfo
 * @see UnaryTemplateInfo
 */
class TemplateInfoTest {
    // region UnaryTemplateInfo.create

    @Test
    fun testCreate_validTemplates() {
        expectScope {
            val t1 = UnaryTemplateInfo.create($$"$_desc").expectNotNull()
            t1.expression.expectEquals($$"$_desc")
            t1.prefix.expectEquals("")
            t1.suffix.expectEquals("_desc")

            val t2 = UnaryTemplateInfo.create("GFX_$").expectNotNull()
            t2.prefix.expectEquals("GFX_")
            t2.suffix.expectEquals("")

            val t3 = UnaryTemplateInfo.create($$"c_$_d").expectNotNull()
            t3.prefix.expectEquals("c_")
            t3.suffix.expectEquals("_d")

            // a template consisting of only the placeholder is also valid
            val t4 = UnaryTemplateInfo.create("$").expectNotNull()
            t4.prefix.expectEquals("")
            t4.suffix.expectEquals("")
        }
    }

    @Test
    fun testCreate_invalidTemplates_returnNull() {
        expectScope {
            UnaryTemplateInfo.create("").expectNull()
            UnaryTemplateInfo.create("abc").expectNull()
            UnaryTemplateInfo.create($$"a_$b_$c").expectNull() // multiple placeholders
        }
    }

    // endregion

    // region normalize

    @Test
    fun testNormalize_cachingAndEquality() {
        expectScope {
            val t = UnaryTemplateInfo.create($$"$_desc").expectNotNull().normalize()
            // interned by expression string
            t.expectSame(UnaryTemplateInfo.create($$"$_desc").expectNotNull().normalize())
            t.expectEquals(UnaryTemplateInfo($$"$_desc"))
            UnaryTemplateInfo.create("abc").expectNull()
        }
    }

    // endregion

    // region UnaryTemplateInfo.resolve

    @Test
    fun testResolve() {
        expectScope {
            UnaryTemplateInfo($$"$_desc").resolve("test").expectEquals("test_desc")
            UnaryTemplateInfo("GFX_$").resolve("test").expectEquals("GFX_test")
            UnaryTemplateInfo($$"c_$_d").resolve("test").expectEquals("c_test_d")
            UnaryTemplateInfo("$").resolve("test").expectEquals("test")
        }
    }

    // endregion

    // region UnaryTemplateInfo.extract

    @Test
    fun testExtract() {
        expectScope {
            UnaryTemplateInfo($$"$_desc").extract("test_desc").expectEquals("test")
            UnaryTemplateInfo("GFX_$").extract("GFX_test").expectEquals("test")
            UnaryTemplateInfo($$"c_$_d").extract("c_test_d").expectEquals("test")
            // extra content between prefix and suffix is preserved as the extracted value
            UnaryTemplateInfo($$"$_desc").extract("te_st_desc").expectEquals("te_st")
            // a template consisting of only the placeholder extracts the whole text
            UnaryTemplateInfo("$").extract("test").expectEquals("test")
        }
    }

    @Test
    fun testExtract_notMatched_returnNull() {
        expectScope {
            UnaryTemplateInfo($$"$_desc").extract("test").expectNull() // missing suffix
            UnaryTemplateInfo($$"$_desc").extract("test_desc_extra").expectNull() // trailing content
            UnaryTemplateInfo("GFX_$").extract("test").expectNull() // missing prefix
            UnaryTemplateInfo($$"c_$_d").extract("c__").expectNull() // too short
            UnaryTemplateInfo($$"c_$_d").extract("c_xe").expectNull() // missing suffix
        }
    }

    @Test
    fun testExtract_emptyResultIsAllowed() {
        expectScope {
            // prefix and suffix match exactly, so the extracted value is empty
            UnaryTemplateInfo($$"$_desc").extract("_desc").expectEquals("")
            UnaryTemplateInfo("GFX_$").extract("GFX_").expectEquals("")
        }
    }

    // endregion
}
