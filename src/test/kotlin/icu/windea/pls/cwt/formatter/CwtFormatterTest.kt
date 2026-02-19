package icu.windea.pls.cwt.formatter

import com.intellij.application.options.CodeStyle
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.cwt.CwtLanguage
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtFormatterTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    private fun reformat(before: String): String {
        myFixture.configureByText("formatter_test.test.cwt", before)
        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformat(myFixture.file)
        }
        return myFixture.editor.document.text
    }

    private fun getCustomSettings(): CwtCodeStyleSettings {
        return CodeStyle.getSettings(project).getCustomSettings(CwtCodeStyleSettings::class.java)
    }

    private fun getCommonSettings(): CommonCodeStyleSettings {
        return CodeStyle.getSettings(project).getCommonSettings(CwtLanguage)
    }

    // region 属性分隔符周围的空格

    @Test
    fun testSpaceAroundPropertySeparator_default() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        val before = "k=v"
        val after = reformat(before)
        assertEquals("k = v", after)
    }

    @Test
    fun testSpaceAroundPropertySeparator_addSpaces() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        val before = "k=v\na=b"
        val after = reformat(before)
        assertEquals("k = v\na = b", after)
    }

    @Test
    fun testSpaceAroundPropertySeparator_removeSpaces() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = false
        val before = "k = v"
        val after = reformat(before)
        assertEquals("k=v", after)
    }

    @Test
    fun testSpaceAroundPropertySeparator_extraSpaces() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        val before = "k  =  v"
        val after = reformat(before)
        assertEquals("k = v", after)
    }

    @Test
    fun testSpaceAroundPropertySeparator_notEqualSign() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        val before = "k  !=  v"
        val after = reformat(before)
        assertEquals("k != v", after)
    }

    @Test
    fun testSpaceAroundPropertySeparator_notEqualSign_disabled() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = false
        // `!=` 周围始终保留空格：移除空格会导致词法分析器将 `k!=v` 解析为键 `k!`、分隔符 `=`、值 `v`
        val before = "k != v"
        val after = reformat(before)
        assertEquals("k != v", after)
    }

    @Test
    fun testSpaceAroundPropertySeparator_doubleEqualSign() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        // `==` 和 `=` 同为 EQUAL_SIGN，词法分析器中优先匹配 `==`
        val before = "k  ==  v"
        val after = reformat(before)
        assertEquals("k == v", after)
    }

    @Test
    fun testSpaceAroundPropertySeparator_doubleEqualSign_disabled() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = false
        // `==` 是 EQUAL_SIGN，可以安全移除空格
        val before = "k == v"
        val after = reformat(before)
        assertEquals("k==v", after)
    }

    @Test
    fun testSpaceAroundPropertySeparator_diamondSign() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        val before = "k  <>  v"
        val after = reformat(before)
        assertEquals("k <> v", after)
    }

    @Test
    fun testSpaceAroundPropertySeparator_diamondSign_disabled() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = false
        // `<>` 周围始终保留空格：移除空格会导致 `k<>v` 被解析为单个 STRING_TOKEN
        val before = "k <> v"
        val after = reformat(before)
        assertEquals("k <> v", after)
    }

    @Test
    fun testSpaceAroundPropertySeparator_quotedKey() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        val before = "\"key\"=\"value\""
        val after = reformat(before)
        assertEquals("\"key\" = \"value\"", after)
    }

    @Test
    fun testSpaceAroundPropertySeparator_unquotedKeyWithSpecialChars() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        // CWT 的未加引号的键可以包含 `[].<>!` 等特殊字符
        val before = "type[army]=v"
        val after = reformat(before)
        assertEquals("type[army] = v", after)
    }

    @Test
    fun testSpaceAroundPropertySeparator_unquotedValueWithSpecialChars() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        // CWT 的未加引号的值同样可以包含 `<>` 等特殊字符
        val before = "icon=<sprite>"
        val after = reformat(before)
        assertEquals("icon = <sprite>", after)
    }

    @Test
    fun testSpaceAroundPropertySeparator_pathExtension() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        val before = "path_extension=.txt"
        val after = reformat(before)
        assertEquals("path_extension = .txt", after)
    }

    // endregion

    // region 选项注释分隔符周围的空格

    @Test
    fun testSpaceAroundOptionSeparator_default() {
        getCustomSettings().SPACE_AROUND_OPTION_SEPARATOR = true
        val before = "## cardinality=0..1"
        val after = reformat(before)
        assertEquals("## cardinality = 0..1", after)
    }

    @Test
    fun testSpaceAroundOptionSeparator_removeSpaces() {
        getCustomSettings().SPACE_AROUND_OPTION_SEPARATOR = false
        val before = "## cardinality = 0..1"
        val after = reformat(before)
        assertEquals("## cardinality=0..1", after)
    }

    @Test
    fun testSpaceAroundOptionSeparator_extraSpaces() {
        getCustomSettings().SPACE_AROUND_OPTION_SEPARATOR = true
        val before = "## severity  =  warning"
        val after = reformat(before)
        assertEquals("## severity = warning", after)
    }

    @Test
    fun testSpaceAroundOptionSeparator_notEqualSign() {
        getCustomSettings().SPACE_AROUND_OPTION_SEPARATOR = true
        val before = "## key  !=  value"
        val after = reformat(before)
        assertEquals("## key != value", after)
    }

    @Test
    fun testSpaceAroundOptionSeparator_notEqualSign_disabled() {
        getCustomSettings().SPACE_AROUND_OPTION_SEPARATOR = false
        // `!=` 周围始终保留空格，与属性分隔符的行为一致
        val before = "## key != value"
        val after = reformat(before)
        assertEquals("## key != value", after)
    }

    @Test
    fun testSpaceAroundOptionSeparator_diamondSign_disabled() {
        getCustomSettings().SPACE_AROUND_OPTION_SEPARATOR = false
        // `<>` 周围始终保留空格，与属性分隔符的行为一致
        val before = "## key <> value"
        val after = reformat(before)
        assertEquals("## key <> value", after)
    }

    // endregion

    // region 花括号内的空格

    @Test
    fun testSpaceWithinEmptyBraces_enabled() {
        getCustomSettings().SPACE_WITHIN_EMPTY_BRACES = true
        val before = "k = {}"
        val after = reformat(before)
        assertEquals("k = { }", after)
    }

    @Test
    fun testSpaceWithinEmptyBraces_disabled() {
        getCustomSettings().SPACE_WITHIN_EMPTY_BRACES = false
        val before = "k = { }"
        val after = reformat(before)
        assertEquals("k = {}", after)
    }

    @Test
    fun testSpaceWithinBraces_enabled() {
        getCustomSettings().SPACE_WITHIN_BRACES = true
        // 单行块中花括号内侧的空格
        val before = "k = {a = v}"
        val after = reformat(before)
        assertEquals("k = { a = v }", after)
    }

    @Test
    fun testSpaceWithinBraces_disabled() {
        getCustomSettings().SPACE_WITHIN_BRACES = false
        val before = "k = { a = v }"
        val after = reformat(before)
        assertEquals("k = {a = v}", after)
    }

    // endregion

    // region 块内缩进

    @Test
    fun testIndentation_singleLevel() {
        val before = "types = {\nk = v\n}"
        val after = reformat(before)
        assertEquals("types = {\n    k = v\n}", after)
    }

    @Test
    fun testIndentation_nestedBlocks() {
        val before = "types = {\ntype[a] = {\npath = \"game\"\n}\n}"
        val after = reformat(before)
        assertEquals("types = {\n    type[a] = {\n        path = \"game\"\n    }\n}", after)
    }

    @Test
    fun testIndentation_tripleNested() {
        val before = "a = {\nb = {\nc = {\nd = v\n}\n}\n}"
        val after = reformat(before)
        assertEquals("a = {\n    b = {\n        c = {\n            d = v\n        }\n    }\n}", after)
    }

    @Test
    fun testIndentation_commentInBlock() {
        val before = "types = {\n# comment\nk = v\n}"
        val after = reformat(before)
        assertEquals("types = {\n    # comment\n    k = v\n}", after)
    }

    @Test
    fun testIndentation_docCommentInBlock() {
        val before = "types = {\n### doc comment\nk = v\n}"
        val after = reformat(before)
        assertEquals("types = {\n    ### doc comment\n    k = v\n}", after)
    }

    @Test
    fun testIndentation_optionCommentInBlock() {
        getCustomSettings().SPACE_AROUND_OPTION_SEPARATOR = true
        val before = "types = {\n## cardinality = 0..1\nk = v\n}"
        val after = reformat(before)
        assertEquals("types = {\n    ## cardinality = 0..1\n    k = v\n}", after)
    }

    @Test
    fun testIndentation_rootLevel_noIndent() {
        val before = "a = yes\nb = no\nc = 1"
        val after = reformat(before)
        assertEquals("a = yes\nb = no\nc = 1", after)
    }

    // endregion

    // region 幂等性

    @Test
    fun testIdempotent_alreadyFormatted() {
        val code = "boolean_value = yes\nnumber_value = 1.0\nstring_value = \"text\""
        val after = reformat(code)
        assertEquals(code, after)
    }

    @Test
    fun testIdempotent_formattedBlock() {
        val code = "types = {\n    ### Doc comment\n    type[army] = {\n        path = \"game/common/armies\"\n    }\n}"
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
    fun testEdge_singleLineBlock() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        getCustomSettings().SPACE_WITHIN_BRACES = true
        // 单行块保持在同一行，不会被拆分为多行
        val code = "k = { a = v }"
        val after = reformat(code)
        assertEquals(code, after)
    }

    @Test
    fun testEdge_valueOnlyInRootBlock() {
        // root_block 中可以包含独立的值（不作为属性值）
        val after = reformat("yes\nno\n1\n2.0")
        assertEquals("yes\nno\n1\n2.0", after)
    }

    @Test
    fun testEdge_dollarSignInValue() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        // `$` 在 CWT 值中是有效字符，常用于本地化名称模板
        val before = "name=\"\$\""
        val after = reformat(before)
        assertEquals("name = \"\$\"", after)
    }

    // endregion

    // region 综合场景

    @Test
    fun testComplex_multipleSettings() {
        val settings = getCustomSettings()
        settings.SPACE_AROUND_PROPERTY_SEPARATOR = true
        settings.SPACE_AROUND_OPTION_SEPARATOR = true
        settings.SPACE_WITHIN_EMPTY_BRACES = true

        val before = """
            |a=yes
            |b = {
            |c=1
            |## cardinality=0..1
            |d="text"
            |}
        """.trimMargin()

        val expected = """
            |a = yes
            |b = {
            |    c = 1
            |    ## cardinality = 0..1
            |    d = "text"
            |}
        """.trimMargin()

        val after = reformat(before)
        assertEquals(expected, after)
    }

    @Test
    fun testComplex_noSpaces() {
        val settings = getCustomSettings()
        settings.SPACE_AROUND_PROPERTY_SEPARATOR = false
        settings.SPACE_AROUND_OPTION_SEPARATOR = false
        settings.SPACE_WITHIN_EMPTY_BRACES = false

        val before = """
            |a = yes
            |b = {}
        """.trimMargin()

        val expected = """
            |a=yes
            |b={}
        """.trimMargin()

        val after = reformat(before)
        assertEquals(expected, after)
    }

    @Test
    fun testComplex_blockWithValues() {
        val settings = getCustomSettings()
        settings.SPACE_AROUND_PROPERTY_SEPARATOR = true

        val before = "colors = {\nyes\nno\n1\n2.0\n\"text\"\n}"
        val expected = "colors = {\n    yes\n    no\n    1\n    2.0\n    \"text\"\n}"
        val after = reformat(before)
        assertEquals(expected, after)
    }

    @Test
    fun testComplex_inlineComment() {
        val settings = getCustomSettings()
        settings.SPACE_AROUND_PROPERTY_SEPARATOR = true

        val before = "icon=icon # <sprite>"
        val after = reformat(before)
        assertEquals("icon = icon # <sprite>", after)
    }

    // endregion

    // region 保留空行

    @Test
    fun testKeepBlankLines_zero() {
        getCommonSettings().KEEP_BLANK_LINES_IN_CODE = 0
        val before = "a = yes\n\n\nb = no"
        val after = reformat(before)
        assertEquals("a = yes\nb = no", after)
    }

    @Test
    fun testKeepBlankLines_one() {
        getCommonSettings().KEEP_BLANK_LINES_IN_CODE = 1
        val before = "a = yes\n\n\n\nb = no"
        val after = reformat(before)
        assertEquals("a = yes\n\nb = no", after)
    }

    @Test
    fun testKeepBlankLines_two() {
        getCommonSettings().KEEP_BLANK_LINES_IN_CODE = 2
        val before = "a = yes\n\n\n\n\nb = no"
        val after = reformat(before)
        assertEquals("a = yes\n\n\nb = no", after)
    }

    @Test
    fun testKeepBlankLines_inBlock() {
        getCommonSettings().KEEP_BLANK_LINES_IN_CODE = 1
        val before = "types = {\nk = v\n\n\n\na = b\n}"
        val after = reformat(before)
        assertEquals("types = {\n    k = v\n\n    a = b\n}", after)
    }

    // endregion
}
