package icu.windea.pls.script.formatter

import com.intellij.application.options.CodeStyle
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.test.markIntegrationTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptFormatterTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    private fun reformat(before: String): String {
        myFixture.configureByText("formatter_test.test.txt", before)
        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformat(myFixture.file)
        }
        return myFixture.editor.document.text
    }

    private fun getCustomSettings(): ParadoxScriptCodeStyleSettings {
        return CodeStyle.getSettings(project).getCustomSettings(ParadoxScriptCodeStyleSettings::class.java)
    }

    private fun getCommonSettings(): CommonCodeStyleSettings {
        return CodeStyle.getSettings(project).getCommonSettings(ParadoxScriptLanguage)
    }

    // region 封装变量分隔符周围的空格

    @Test
    fun testSpaceAroundScriptedVariableSeparator_addSpaces() {
        getCustomSettings().SPACE_AROUND_SCRIPTED_VARIABLE_SEPARATOR = true
        val after = reformat("@var=1")
        assertEquals("@var = 1", after)
    }

    @Test
    fun testSpaceAroundScriptedVariableSeparator_removeSpaces() {
        getCustomSettings().SPACE_AROUND_SCRIPTED_VARIABLE_SEPARATOR = false
        val after = reformat("@var = 1")
        assertEquals("@var=1", after)
    }

    @Test
    fun testSpaceAroundScriptedVariableSeparator_extraSpaces() {
        getCustomSettings().SPACE_AROUND_SCRIPTED_VARIABLE_SEPARATOR = true
        val after = reformat("@var  =  1")
        assertEquals("@var = 1", after)
    }

    // endregion

    // region 属性分隔符周围的空格

    @Test
    fun testSpaceAroundPropertySeparator_addSpaces() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        val after = reformat("k=v")
        assertEquals("k = v", after)
    }

    @Test
    fun testSpaceAroundPropertySeparator_removeSpaces() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = false
        val after = reformat("k = v")
        assertEquals("k=v", after)
    }

    @Test
    fun testSpaceAroundPropertySeparator_extraSpaces() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        val after = reformat("k  =  v")
        assertEquals("k = v", after)
    }

    @Test
    fun testSpaceAroundPropertySeparator_ltSign() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        val after = reformat("level  <  2")
        assertEquals("level < 2", after)
    }

    @Test
    fun testSpaceAroundPropertySeparator_gtSign() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        val after = reformat("level  >  2")
        assertEquals("level > 2", after)
    }

    @Test
    fun testSpaceAroundPropertySeparator_leSign() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        val after = reformat("level  <=  2")
        assertEquals("level <= 2", after)
    }

    @Test
    fun testSpaceAroundPropertySeparator_geSign() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        val after = reformat("level  >=  2")
        assertEquals("level >= 2", after)
    }

    @Test
    fun testSpaceAroundPropertySeparator_safeEqualSign() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        // `?=` 是安全赋值符，仅在目标未被设置时生效
        val after = reformat("size  ?=  @var")
        assertEquals("size ?= @var", after)
    }

    @Test
    fun testSpaceAroundPropertySeparator_notEqualSign() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        // 输入需要预留空格：词法分析器会将 `k!=v` 解析为键 `k!`、分隔符 `=`、值 `v`（与 CWT 相同）
        val after = reformat("k != v")
        assertEquals("k != v", after)
    }

    @Test
    fun testSpaceAroundPropertySeparator_disabled_variousOperators() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = false
        assertEquals("k=v", reformat("k = v"))
        assertEquals("level<2", reformat("level < 2"))
        assertEquals("level>=2", reformat("level >= 2"))
    }

    // endregion

    // region 内联数学操作符周围的空格

    @Test
    fun testSpaceAroundInlineMathOperator_addSpaces() {
        getCustomSettings().SPACE_AROUND_INLINE_MATH_OPERATOR = true
        getCustomSettings().SPACE_WITHIN_INLINE_MATH_BRACKETS = true
        val after = reformat("v = @[ 2+3 ]")
        assertEquals("v = @[ 2 + 3 ]", after)
    }

    @Test
    fun testSpaceAroundInlineMathOperator_removeSpaces() {
        getCustomSettings().SPACE_AROUND_INLINE_MATH_OPERATOR = false
        getCustomSettings().SPACE_WITHIN_INLINE_MATH_BRACKETS = true
        val after = reformat("v = @[ 2 + 3 ]")
        assertEquals("v = @[ 2+3 ]", after)
    }

    @Test
    fun testSpaceAroundInlineMathOperator_multipleOperators() {
        getCustomSettings().SPACE_AROUND_INLINE_MATH_OPERATOR = true
        getCustomSettings().SPACE_WITHIN_INLINE_MATH_BRACKETS = true
        val after = reformat("v = @[ 2+3*4 ]")
        assertEquals("v = @[ 2 + 3 * 4 ]", after)
    }

    @Test
    fun testSpaceAroundInlineMathOperator_withParentheses() {
        getCustomSettings().SPACE_AROUND_INLINE_MATH_OPERATOR = true
        getCustomSettings().SPACE_WITHIN_INLINE_MATH_BRACKETS = true
        val after = reformat("v = @[ ( 2+3 ) ]")
        assertEquals("v = @[ ( 2 + 3 ) ]", after)
    }

    // endregion

    // region 花括号内的空格

    @Test
    fun testSpaceWithinEmptyBraces_enabled() {
        getCustomSettings().SPACE_WITHIN_EMPTY_BRACES = true
        val after = reformat("k = {}")
        assertEquals("k = { }", after)
    }

    @Test
    fun testSpaceWithinEmptyBraces_disabled() {
        getCustomSettings().SPACE_WITHIN_EMPTY_BRACES = false
        val after = reformat("k = { }")
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

    // region 内联数学表达式括号内的空格

    @Test
    fun testSpaceWithinInlineMathBrackets_enabled() {
        getCustomSettings().SPACE_WITHIN_INLINE_MATH_BRACKETS = true
        getCustomSettings().SPACE_AROUND_INLINE_MATH_OPERATOR = true
        val after = reformat("v = @[2 + 3]")
        assertEquals("v = @[ 2 + 3 ]", after)
    }

    @Test
    fun testSpaceWithinInlineMathBrackets_disabled() {
        getCustomSettings().SPACE_WITHIN_INLINE_MATH_BRACKETS = false
        getCustomSettings().SPACE_AROUND_INLINE_MATH_OPERATOR = true
        val after = reformat("v = @[ 2 + 3 ]")
        assertEquals("v = @[2 + 3]", after)
    }

    // endregion

    // region 块内缩进

    @Test
    fun testIndentation_singleLevel() {
        val after = reformat("effect = {\nk = v\n}")
        assertEquals("effect = {\n    k = v\n}", after)
    }

    @Test
    fun testIndentation_nestedBlocks() {
        val after = reformat("a = {\nb = {\nc = 1\n}\n}")
        assertEquals("a = {\n    b = {\n        c = 1\n    }\n}", after)
    }

    @Test
    fun testIndentation_commentInBlock() {
        val after = reformat("a = {\n# comment\nk = v\n}")
        assertEquals("a = {\n    # comment\n    k = v\n}", after)
    }

    @Test
    fun testIndentation_scriptedVariableInBlock() {
        getCustomSettings().SPACE_AROUND_SCRIPTED_VARIABLE_SEPARATOR = true
        val after = reformat("a = {\n@var = 1\nk = v\n}")
        assertEquals("a = {\n    @var = 1\n    k = v\n}", after)
    }

    @Test
    fun testIndentation_rootLevel_noIndent() {
        val after = reformat("@var = 1\na = yes\nb = no")
        assertEquals("@var = 1\na = yes\nb = no", after)
    }

    @Test
    fun testIndentation_valuesInBlock() {
        val after = reformat("list = {\nyes\nno\n1\n2.0\n\"text\"\n}")
        assertEquals("list = {\n    yes\n    no\n    1\n    2.0\n    \"text\"\n}", after)
    }

    // endregion

    // region 幂等性

    @Test
    fun testIdempotent_alreadyFormatted() {
        val code = "@var = 1\n\n# Line comment\nsettings = {\n    boolean_value = yes\n    number_value = 1.0\n}"
        val after = reformat(code)
        assertEquals(code, after)
    }

    // endregion

    // region 参数条件块

    @Test
    fun testParameterCondition_spacingEnabled() {
        getCustomSettings().SPACE_WITHIN_PARAMETER_CONDITION_BRACKETS = true
        getCustomSettings().SPACE_WITHIN_PARAMETER_CONDITION_EXPRESSION_BRACKETS = false
        // 参数条件块结构：[ (LEFT_BRACKET) [ (NESTED_LEFT_BRACKET) expr ] (NESTED_RIGHT_BRACKET) items ] (RIGHT_BRACKET)
        // BRACKETS 控制 NESTED_RIGHT_BRACKET 与 MEMBERS 以及 MEMBERS 与 RIGHT_BRACKET 之间的空格
        val after = reformat("[[param]k = v]")
        assertEquals("[[param] k = v ]", after)
    }

    @Test
    fun testParameterCondition_spacingDisabled() {
        getCustomSettings().SPACE_WITHIN_PARAMETER_CONDITION_BRACKETS = false
        getCustomSettings().SPACE_WITHIN_PARAMETER_CONDITION_EXPRESSION_BRACKETS = false
        val after = reformat("[[param] k = v ]")
        assertEquals("[[param]k = v]", after)
    }

    @Test
    fun testParameterCondition_expressionBracketsSpacing() {
        getCustomSettings().SPACE_WITHIN_PARAMETER_CONDITION_BRACKETS = true
        getCustomSettings().SPACE_WITHIN_PARAMETER_CONDITION_EXPRESSION_BRACKETS = true
        // EXPRESSION_BRACKETS 控制 NESTED_LEFT_BRACKET 与表达式以及表达式与 NESTED_RIGHT_BRACKET 之间的空格
        val after = reformat("[[param]k = v]")
        assertEquals("[[ param ] k = v ]", after)
    }

    @Test
    fun testParameterCondition_negated() {
        getCustomSettings().SPACE_WITHIN_PARAMETER_CONDITION_BRACKETS = true
        getCustomSettings().SPACE_WITHIN_PARAMETER_CONDITION_EXPRESSION_BRACKETS = false
        // 取反参数条件：`!` 是表达式内部的 NOT_SIGN
        val after = reformat("[[!param]k = v]")
        assertEquals("[[!param] k = v ]", after)
    }

    @Test
    fun testParameterCondition_indentedInBlock() {
        getCustomSettings().SPACE_WITHIN_PARAMETER_CONDITION_BRACKETS = true
        getCustomSettings().SPACE_WITHIN_PARAMETER_CONDITION_EXPRESSION_BRACKETS = false
        val before = "effect = {\n[[param]\nk = v\n]\n}"
        val after = reformat(before)
        assertEquals("effect = {\n    [[param]\n        k = v\n    ]\n}", after)
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
        // 单行块保持在同一行
        val code = "k = { a = v }"
        val after = reformat(code)
        assertEquals(code, after)
    }

    @Test
    fun testEdge_valueOnlyInRootBlock() {
        // root_block 中可以包含独立的值
        val after = reformat("yes\nno\n1\n2.0")
        assertEquals("yes\nno\n1\n2.0", after)
    }

    @Test
    fun testEdge_quotedKey() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        val after = reformat("\"quoted.key\"=v")
        assertEquals("\"quoted.key\" = v", after)
    }

    // endregion

    // region 综合场景

    @Test
    fun testComplex_multipleSettings() {
        val settings = getCustomSettings()
        settings.SPACE_AROUND_PROPERTY_SEPARATOR = true
        settings.SPACE_AROUND_SCRIPTED_VARIABLE_SEPARATOR = true
        settings.SPACE_WITHIN_EMPTY_BRACES = true

        val before = """
            |@var=1
            |a=yes
            |b = {
            |c=1
            |d="text"
            |}
        """.trimMargin()

        val expected = """
            |@var = 1
            |a = yes
            |b = {
            |    c = 1
            |    d = "text"
            |}
        """.trimMargin()

        val after = reformat(before)
        assertEquals(expected, after)
    }

    @Test
    fun testComplex_noSpaces() {
        val settings = getCustomSettings()
        settings.SPACE_AROUND_SCRIPTED_VARIABLE_SEPARATOR = false
        settings.SPACE_AROUND_PROPERTY_SEPARATOR = false
        settings.SPACE_WITHIN_EMPTY_BRACES = false

        val before = """
            |@var = 1
            |a = yes
            |b = {}
        """.trimMargin()

        val expected = """
            |@var=1
            |a=yes
            |b={}
        """.trimMargin()

        val after = reformat(before)
        assertEquals(expected, after)
    }

    @Test
    fun testComplex_inlineComment() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        val after = reformat("icon=icon # comment")
        assertEquals("icon = icon # comment", after)
    }

    @Test
    fun testComplex_colorValue() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        val after = reformat("color = rgb { 142 188 241 }")
        assertEquals("color = rgb { 142 188 241 }", after)
    }

    @Test
    fun testComplex_scriptedVariableReference() {
        getCustomSettings().SPACE_AROUND_PROPERTY_SEPARATOR = true
        val after = reformat("number_value=@var")
        assertEquals("number_value = @var", after)
    }

    // endregion

    // region 保留空行

    @Test
    fun testKeepBlankLines_zero() {
        getCommonSettings().KEEP_BLANK_LINES_IN_CODE = 0
        val after = reformat("a = yes\n\n\nb = no")
        assertEquals("a = yes\nb = no", after)
    }

    @Test
    fun testKeepBlankLines_one() {
        getCommonSettings().KEEP_BLANK_LINES_IN_CODE = 1
        val after = reformat("a = yes\n\n\n\nb = no")
        assertEquals("a = yes\n\nb = no", after)
    }

    @Test
    fun testKeepBlankLines_two() {
        getCommonSettings().KEEP_BLANK_LINES_IN_CODE = 2
        val after = reformat("a = yes\n\n\n\n\nb = no")
        assertEquals("a = yes\n\n\nb = no", after)
    }

    @Test
    fun testKeepBlankLines_inBlock() {
        getCommonSettings().KEEP_BLANK_LINES_IN_CODE = 1
        val after = reformat("effect = {\nk = v\n\n\n\na = b\n}")
        assertEquals("effect = {\n    k = v\n\n    a = b\n}", after)
    }

    // endregion
}
