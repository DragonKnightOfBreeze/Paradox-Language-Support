package icu.windea.pls.ai.prompts

import org.junit.Assert.assertEquals
import org.junit.Test

class PromptTemplateEngineTest {
    private val engine = PromptTemplateEngine()

    private fun norm(s: String) = s.replace("\r\n", "\n").trimEnd()

    @Test
    fun testPlaceholders_basicAndNoNested() {
        val out = engine.render(
            "prompts/template_placeholders.md",
            mapOf(
                "name" to "World",
                "valueWithBraces" to "{{should_not_expand}}"
            )
        )
        val expected = """
            Hello, World!
            HelloAgain, World!
            Literal: {{should_not_expand}}
        """.trimIndent()
        assertEquals(norm(expected), norm(out))
    }

    @Test
    fun testIf_trueFalseAndNegation_newlineHandling() {
        val out = engine.render(
            "prompts/template_if.md",
            mapOf(
                "flag" to true
            )
        )
        val expected = """
            A
            T
            B
            C
        """.trimIndent()
        assertEquals(norm(expected), norm(out))

        val outFalse = engine.render(
            "prompts/template_if.md",
            mapOf(
                "flag" to false
            )
        )
        val expectedFalse = """
            A
            B
            C
        """.trimIndent()
        assertEquals(norm(expectedFalse), norm(outFalse))
    }

    @Test
    fun testIf_nested() {
        val out = engine.render(
            "prompts/template_if_nested.md",
            mapOf(
                "a" to true,
                "b" to false
            )
        )
        val expected = """
            Start
            A1
            A2
            End
        """.trimIndent()
        assertEquals(norm(expected), norm(out))
    }

    @Test
    fun testInclude_relativeAndNewline() {
        val out = engine.render("prompts/template_include_root.md")
        val expected = """
            Before
            P1
            Q
            P2
            After
        """.trimIndent()
        assertEquals(norm(expected), norm(out))
    }

    @Test
    fun testInclude_cycleDetection() {
        val out = engine.render("prompts/template_cycle_root.md")
        // 在 b.md 中，循环 include 被移除，同时保留 include 注释后的换行，因此 B 与 B-end 之间会有一个空行
        val expected = """
            Start
            A
            B
            
            B-end
            A-end
            End
        """.trimIndent()
        assertEquals(norm(expected), norm(out))
    }

    @Test
    fun testInclude_missingPathDirectiveRemoved() {
        val out = engine.render("prompts/template_include_missing.md")
        // 缺失路径：移除指令，并保留紧随其后的换行
        val expected = """
            X
            
            Y
        """.trimIndent()
        assertEquals(norm(expected), norm(out))
    }

    @Test
    fun testUnmatchedEndif_removed() {
        val out = engine.render("prompts/template_unmatched_endif.md")
        // 顶层遇到未配对的 endif：移除指令并剥离其后的换行
        val expected = """
            Top
            Bottom
        """.trimIndent()
        assertEquals(norm(expected), norm(out))
    }

    @Test
    fun testUnmatchedIf_removedDirectiveButKeepContent() {
        val out = engine.render(
            "prompts/template_unmatched_if.md",
            mapOf("flag" to true)
        )
        // 未匹配的 @if：删除 @if 指令并移除其后的换行，块内容按普通文本保留
        val expected = """
            Top
            Middle
            Bottom
        """.trimIndent()
        assertEquals(norm(expected), norm(out))
    }
}
