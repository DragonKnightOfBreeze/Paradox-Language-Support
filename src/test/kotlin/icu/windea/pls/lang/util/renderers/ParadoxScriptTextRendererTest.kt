package icu.windea.pls.lang.util.renderers

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.script.ParadoxScriptFileType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptTextRendererTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Test
    fun testNotMultiline() {
        val input = "key = { k1 = v1 v }"
        val expect = "key = { k1 = v1 v }"
        assert(expect, input) { multiline = false }
    }

    @Test
    fun testMultiline() {
        val input = "key = { k1 = v1 v }"
        val expect = """
            key = {
                k1 = v1
                v
            }
            """.trimIndent()
        assert(expect, input) { multiline = true }
    }

    @Test
    fun testMultiline_nestedBlockIndent() {
        val input = "key = { k1 = { v } }"
        val expect = """
            key = {
                k1 = {
                    v
                }
            }
            """.trimIndent()
        assert(expect, input) { multiline = true }
    }

    @Test
    fun testConditional_false() {
        val input = "settings = { a = b [[!PARAM] parameter_condition = \$PARAM$ ] c }"
        val expect = """
            settings = {
                a = b
                c
            }
            """.trimIndent()
        assert(expect, input) {
            multiline = true
            conditional = false
        }
    }

    @Test
    fun testConditional_true() {
        val input = "settings = { a = b [[!PARAM] parameter_condition = \$PARAM$ ] c }"
        val expect = """
            settings = {
                a = b
                parameter_condition = ${'$'}PARAM$
                c
            }
            """.trimIndent()
        assert(expect, input) {
            multiline = true
            conditional = true
        }
    }

    private fun assert(expect: String, input: String, configure: ParadoxScriptTextRenderer.() -> Unit = {}) {
        myFixture.configureByText(ParadoxScriptFileType, input)
        val element = myFixture.file
        val renderer = ParadoxScriptTextRenderer().also { it.configure() }
        val context = renderer.initContext()
        val result = renderer.render(element, context)
        assertEquals(expect, result)
    }
}
