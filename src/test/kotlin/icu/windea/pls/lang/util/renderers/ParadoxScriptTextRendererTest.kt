package icu.windea.pls.lang.util.renderers

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.script.ParadoxScriptFileType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptTextRendererTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Test
    fun testEmptyFile() {
        val input = ""
        val expect = ""
        assert(expect, input)
    }

    @Test
    fun testMultipleTopLevelMembers_multiline() {
        val input = "a = b\nc = d"
        val expect = """
            a = b
            c = d
            """.trimIndent()
        assert(expect, input) { multiline = true }
    }

    @Test
    fun testMultipleTopLevelMembers_notMultiline() {
        val input = "a = b c = d"
        val expect = "a = b c = d"
        assert(expect, input) { multiline = false }
    }

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
    fun testMultiline_emptyBlock() {
        val input = "key = { }"
        val expect = """
            key = {}
            """.trimIndent()
        assert(expect, input) { multiline = true }
    }

    @Test
    fun testMultiline_nestedEmptyBlock() {
        val input = "root = { key = { } }"
        val expect = """
            root = {
                key = {}
            }
            """.trimIndent()
        assert(expect, input) { multiline = true }
    }

    @Test
    fun testPropertySeparator_notEqual() {
        val input = "a != b"
        val expect = "a != b"
        assert(expect, input) { multiline = false }
    }

    @Test
    fun testPropertySeparator_safeEqual() {
        val input = "a ?= b"
        val expect = "a ?= b"
        assert(expect, input) { multiline = false }
    }

    @Test
    fun testPropertySeparator_ge() {
        val input = "a >= b"
        val expect = "a >= b"
        assert(expect, input) { multiline = false }
    }

    @Test
    fun testPropertySeparator_le() {
        val input = "a <= b"
        val expect = "a <= b"
        assert(expect, input) { multiline = false }
    }

    @Test
    fun testMissingValue_unresolved() {
        val input = "a ="
        val expect = "a = (unresolved)"
        assert(expect, input) { multiline = false }
    }

    @Test
    fun testQuotedKeyAndValue() {
        val input = "\"foo bar\" = \"baz qux\""
        val expect = "\"foo bar\" = \"baz qux\""
        assert(expect, input) { multiline = false }
    }

    @Test
    fun testBlockMultipleMembers_notMultiline() {
        val input = "key = { a = b c = d }"
        val expect = "key = { a = b c = d }"
        assert(expect, input) { multiline = false }
    }

    @Test
    fun testValueOnlyMember_multiline() {
        val input = "key = { v }"
        val expect = """
            key = {
                v
            }
            """.trimIndent()
        assert(expect, input) { multiline = true }
    }

    @Test
    fun testScriptedVariableReference() {
        val input = "@var = 1\nkey = @var"
        val expect = "key = (unresolved)"
        assert(expect, input) { multiline = false }
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
        FileBasedIndex.getInstance().requestReindex(myFixture.file.virtualFile)
        val renderer = ParadoxScriptTextRenderer().also { it.configure() }
        val context = renderer.initContext()
        val result = renderer.render(element, context)
        assertEquals(expect, result)
    }
}
