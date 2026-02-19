package icu.windea.pls.localisation.formatter

import com.intellij.application.options.CodeStyle
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.test.markIntegrationTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxLocalisationFormatterTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    private fun getCommonSettings(): CommonCodeStyleSettings {
        return CodeStyle.getSettings(project).getCommonSettings(ParadoxLocalisationLanguage)
    }

    private fun reformat(before: String): String {
        myFixture.configureByText("formatter_test.test.yml", before)
        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformat(myFixture.file)
        }
        return myFixture.editor.document.text
    }

    // region 属性编号与属性值之间的空格

    @Test
    fun testSpacingBetweenPropertyNumberAndValue() {
        val after = reformat("l_english:\n key:0 \"Value\"")
        assertEquals("l_english:\n key:0 \"Value\"", after)
    }

    @Test
    fun testSpacingBetweenPropertyNumberAndValue_extraSpaces() {
        val after = reformat("l_english:\n key:0   \"Value\"")
        assertEquals("l_english:\n key:0 \"Value\"", after)
    }

    @Test
    fun testSpacingBetweenPropertyNumberAndValue_noSpace() {
        val after = reformat("l_english:\n key:0\"Value\"")
        assertEquals("l_english:\n key:0 \"Value\"", after)
    }

    // endregion

    // region 冒号与属性值之间的空格（无编号时）

    @Test
    fun testSpacingBetweenColonAndValue() {
        val after = reformat("l_english:\n key: \"Value\"")
        assertEquals("l_english:\n key: \"Value\"", after)
    }

    @Test
    fun testSpacingBetweenColonAndValue_extraSpaces() {
        val after = reformat("l_english:\n key:   \"Value\"")
        assertEquals("l_english:\n key: \"Value\"", after)
    }

    @Test
    fun testSpacingBetweenColonAndValue_noSpace() {
        val after = reformat("l_english:\n key:\"Value\"")
        assertEquals("l_english:\n key: \"Value\"", after)
    }

    // endregion

    // region 缩进

    @Test
    fun testIndentation_property() {
        // 本地化的默认缩进大小为 1 个空格
        val after = reformat("l_english:\nkey:0 \"Value\"")
        assertEquals("l_english:\n key:0 \"Value\"", after)
    }

    @Test
    fun testIndentation_multipleProperties() {
        val before = "l_english:\nkey1:0 \"A\"\nkey2:0 \"B\"\nkey3:0 \"C\""
        val expected = "l_english:\n key1:0 \"A\"\n key2:0 \"B\"\n key3:0 \"C\""
        val after = reformat(before)
        assertEquals(expected, after)
    }

    @Test
    fun testIndentation_commentInPropertyList() {
        val before = "l_english:\n# comment\nkey:0 \"Value\""
        val expected = "l_english:\n # comment\n key:0 \"Value\""
        val after = reformat(before)
        assertEquals(expected, after)
    }

    @Test
    fun testIndentation_alreadyIndented() {
        val code = "l_english:\n key:0 \"Value\""
        val after = reformat(code)
        assertEquals(code, after)
    }

    // endregion

    // region 幂等性

    @Test
    fun testIdempotent_fullExample() {
        val code = """
            |l_english:
            | # Comment
            | text_empty:0 ""
            | text:0 "Value"
            | text_multiline:0 "Value\nNew line"
        """.trimMargin()
        val after = reformat(code)
        assertEquals(code, after)
    }

    // endregion

    // region 边界情况

    @Test
    fun testEdge_emptyFile() {
        val after = reformat("")
        assertEquals("", after)
    }

    @Test
    fun testEdge_localeOnly() {
        // 仅有语言区域头，无属性
        val code = "l_english:"
        val after = reformat(code)
        assertEquals(code, after)
    }

    @Test
    fun testEdge_keyWithSpecialChars() {
        // 本地化键可以包含点号、连字符、撇号：[A-Za-z0-9_.\-']+
        val after = reformat("l_english:\nmod_country.leader-trait's:0 \"Value\"")
        assertEquals("l_english:\n mod_country.leader-trait's:0 \"Value\"", after)
    }

    @Test
    fun testEdge_commentBeforeLocale() {
        // property_list 可以以注释开头，位于语言区域头之前
        val before = "# file comment\nl_english:\n key:0 \"Value\""
        val after = reformat(before)
        assertEquals("# file comment\nl_english:\n key:0 \"Value\"", after)
    }

    @Test
    fun testEdge_blankLineBetweenCommentAndProperty() {
        getCommonSettings().KEEP_BLANK_LINES_IN_CODE = 1
        val before = "l_english:\n # comment\n\n\n\n key:0 \"Value\""
        val after = reformat(before)
        assertEquals("l_english:\n # comment\n\n key:0 \"Value\"", after)
    }

    @Test
    fun testEdge_propertyWithoutNumber() {
        // 编号是可选的，冒号后直接跟属性值
        val after = reformat("l_english:\n key: \"Value\"")
        assertEquals("l_english:\n key: \"Value\"", after)
    }

    // endregion

    // region 综合场景

    @Test
    fun testComplex_mixedSpacingAndIndentation() {
        val before = """
            |l_english:
            |key1:0   "A"
            |# comment
            |key2:0"B"
        """.trimMargin()
        val expected = """
            |l_english:
            | key1:0 "A"
            | # comment
            | key2:0 "B"
        """.trimMargin()
        val after = reformat(before)
        assertEquals(expected, after)
    }

    @Test
    fun testComplex_emptyValue() {
        val after = reformat("l_english:\nkey:0 \"\"")
        assertEquals("l_english:\n key:0 \"\"", after)
    }

    @Test
    fun testComplex_richText() {
        // 本地化值中可以包含颜色代码、图标引用、命令等富文本元素
        val code = "l_english:\n key:0 \"Colorful text: §RRed text§!\""
        val after = reformat(code)
        assertEquals(code, after)
    }

    // endregion

    // region 保留空行

    @Test
    fun testKeepBlankLines_zero() {
        getCommonSettings().KEEP_BLANK_LINES_IN_CODE = 0
        val before = "l_english:\n key1:0 \"A\"\n\n\n key2:0 \"B\""
        val after = reformat(before)
        assertEquals("l_english:\n key1:0 \"A\"\n key2:0 \"B\"", after)
    }

    @Test
    fun testKeepBlankLines_one() {
        getCommonSettings().KEEP_BLANK_LINES_IN_CODE = 1
        val before = "l_english:\n key1:0 \"A\"\n\n\n\n key2:0 \"B\""
        val after = reformat(before)
        assertEquals("l_english:\n key1:0 \"A\"\n\n key2:0 \"B\"", after)
    }

    // endregion
}
