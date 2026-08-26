package icu.windea.pls.script.text

import org.junit.Assert
import org.junit.Test

/**
 * @see ParadoxScriptTextBuilder
 */
class ParadoxScriptTextBuilderTest {
    @Test
    fun inlineMath() {
        with(ParadoxScriptTextBuilder) {
            Assert.assertEquals("@[ 1+1 ]", inlineMath("1+1"))
        }
    }

    @Test
    fun parameter() {
        with(ParadoxScriptTextBuilder) {
            Assert.assertEquals("\$foo$", parameter("foo"))
        }
    }

    @Test
    fun parameter_withDefault() {
        with(ParadoxScriptTextBuilder) {
            Assert.assertEquals("\$foo|bar$", parameter("foo", "bar"))
        }
    }

    @Test
    fun conditionalBlock() {
        with(ParadoxScriptTextBuilder) {
            Assert.assertEquals("[[expr] block ]", conditionalBlock("expr", "block"))
            Assert.assertEquals("[[expr] block ]", conditionalBlock("expr") { "block" })
        }
    }

    @Test
    fun complex() {
        val expect = buildString {
            appendLine("key = \$PARAM|0$")
            appendLine("value = @[ 1 + 1 ]")
            appendLine("[[INPUT] input = yes ]")
        }.trim()
        val actual = buildScriptText {
            """
                key = ${parameter("PARAM", "0")}
                value = ${inlineMath("1 + 1")}
                ${conditionalBlock("INPUT") { "input = yes" }}
            """.trimIndent()
        }
        Assert.assertEquals(expect, actual)
    }
}
