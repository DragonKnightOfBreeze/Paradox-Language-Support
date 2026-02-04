package icu.windea.pls.lang.util.renderers

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.rd.util.AtomicInteger
import icu.windea.pls.core.util.values.FallbackStrings
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.PlsStrings
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptTextPlainRendererTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    private val counter = AtomicInteger()
    private val gameType = ParadoxGameType.Vic3
    private val unresolved = FallbackStrings.unresolved
    private val blockFolder = PlsStrings.blockFolder

    @Before
    fun setup() {
        markIntegrationTest()
        initConfigGroups(project, gameType)
    }

    @Test
    fun conditional_inBlock_fullExample_fromRendererTest() {
        assertResult(
            "settings = {\n    a = b\n    c\n}",
            "settings = { a = b [[!PARAM] parameter_condition = \$PARAM$ ] c }"
        ) {
            multiline = true
            conditional = false
        }
        assertResult(
            "settings = {\n    a = b\n    parameter_condition = \$PARAM$\n    c\n}",
            "settings = { a = b [[!PARAM] parameter_condition = \$PARAM$ ] c }"
        ) {
            multiline = true
            conditional = true
        }
    }

    @Test
    fun scriptedVariableReference_localDefinition() {
        assertResult("key = 1", "@var = 1\nkey = @var") {
            multiline = false
        }
    }

    @Test
    fun scriptedVariableReference_unresolved_local() {
        assertResult("key = $unresolved", "key = @var") {
            multiline = false
        }
    }

    @After
    fun clear() = clearIntegrationTest()

    @Test
    fun emptyFile() {
        val input = ""
        val expect = ""
        assertResult(expect, input)
    }

    @Test
    fun simple() {
        assertResult("key = value", "key = value")
    }

    @Test
    fun file_multipleMembers_multiline_default() {
        assertResult(
            "a = 1\nb = 2",
            "a = 1 b = 2"
        )
    }

    @Test
    fun file_multipleMembers_multiline_false() {
        assertResult(
            "a = 1 b = 2",
            "a = 1 b = 2"
        ) {
            multiline = false
        }
    }

    @Test
    fun separator_greaterOrEqual() {
        assertResult("key >= value", "key >= value")
    }

    @Test
    fun separator_lessOrEqual() {
        assertResult("key <= value", "key <= value")
    }

    @Test
    fun separator_notEqual() {
        assertResult("key != value", "key != value")
    }

    @Test
    fun separator_safeEqual() {
        assertResult("key ?= value", "key ?= value")
    }

    @Test
    fun property_missingValue_shouldRenderUnresolved() {
        assertResult("key = $unresolved", "key =")
    }

    @Test
    fun quotedKeyAndValue() {
        assertResult("\"foo bar\" = \"baz qux\"", "\"foo bar\" = \"baz qux\"") {
            multiline = false
        }
    }

    @Test
    fun block_multiline_default() {
        assertResult(
            "key = {\n    a = 1\n    b = 2\n}",
            "key = { a = 1 b = 2 }"
        )
    }

    @Test
    fun block_multiline_emptyBlock() {
        assertResult("key = {}", "key = { }")
    }

    @Test
    fun block_multiline_valueOnlyMember() {
        assertResult(
            "key = {\n    v\n}",
            "key = { v }"
        )
    }

    @Test
    fun block_multiline_mixedPropertyAndValueMembers() {
        assertResult(
            "key = {\n    k1 = v1\n    v\n}",
            "key = { k1 = v1 v }"
        )
    }

    @Test
    fun block_multiline_false() {
        assertResult(
            "key = { a = 1 b = 2 }",
            "key = { a = 1 b = 2 }"
        ) {
            multiline = false
        }
    }

    @Test
    fun block_nested() {
        assertResult(
            "key = {\n    a = {\n        b = 1\n    }\n}",
            "key = { a = { b = 1 } }"
        )
    }

    @Test
    fun block_multiline_nestedBlockIndent() {
        assertResult(
            "key = {\n    k1 = {\n        v\n    }\n}",
            "key = { k1 = { v } }"
        )
    }

    @Test
    fun block_multiline_nestedEmptyBlock() {
        assertResult(
            "root = {\n    key = {}\n}",
            "root = { key = { } }"
        )
    }

    @Test
    fun block_customIndent() {
        assertResult(
            "key = {\n  a = 1\n}",
            "key = { a = 1 }"
        ) {
            indent = "  "
        }
    }

    @Test
    fun string_quoteIfNecessary() {
        assertResult("key = \"a b\"", "key = \"a b\"")
    }

    @Test
    fun block_renderInBlock_false_shouldRenderUnresolved() {
        assertResult("key = $blockFolder", "key = { a = 1 }") {
            renderInBlock = false
        }
    }

    @Test
    fun conditional_disabled_shouldSkipParameterConditionMembers() {
        assertResult("", "[[!PARAM] a = 1 ]") {
            conditional = false
        }
    }

    @Test
    fun conditional_enabled_shouldRenderParameterConditionMembers() {
        assertResult("a = 1", "[[!PARAM] a = 1 ]") {
            conditional = true
        }
    }

    @Test
    fun conditional_inBlock_enabledOrDisabled() {
        assertResult(
            "key = {\n    b = 2\n}",
            "key = { [[!PARAM] a = 1 ] b = 2 }"
        ) {
            conditional = false
        }
        assertResult(
            "key = {\n    a = 1\n    b = 2\n}",
            "key = { [[!PARAM] a = 1 ] b = 2 }"
        ) {
            conditional = true
        }
    }

    @Test
    fun simple_withSv() {
        markFileInfo(gameType, "common/scripted_variables/global.txt")
        myFixture.configureByText("global.txt", "@var = 1")

        assertResult("key = 1", "key = @var")
        assertResult("key = $unresolved", "key = @unresolved")
    }

    @Test
    fun simple_withSv_string_shouldBeQuoted() {
        markFileInfo(gameType, "common/scripted_variables/global_string.txt")
        myFixture.configureByText("global_string.txt", "@var = \"a b\"")

        assertResult("key = \"a b\"", "key = @var")
    }

    @Test
    fun inline_simple() {
        markFileInfo(gameType, "common/inline_scripts/test/inline_script.txt")
        myFixture.configureByText("inline_script.txt", "k0 = v0")

        assertResult(
            "inline_script = test/inline_script\nk0 = v0",
            "inline_script = test/inline_script"
        ) {
            inline = true
        }
    }

    @Test
    fun inline_multiline_false() {
        markFileInfo(gameType, "common/inline_scripts/test/inline_script_2.txt")
        myFixture.configureByText("inline_script_2.txt", "k0 = v0 k1 = v1")

        assertResult(
            "inline_script = test/inline_script_2 k0 = v0 k1 = v1",
            "inline_script = test/inline_script_2"
        ) {
            inline = true
            multiline = false
        }
    }

    private inline fun assertResult(expect: String, input: String, configure: ParadoxScriptTextPlainRenderer.() -> Unit = {}) {
        val id = counter.getAndIncrement()
        markFileInfo(gameType, "common/renderer_test_$id.txt")
        myFixture.configureByText("renderer_test_$id.txt", input)
        val file = myFixture.file as ParadoxScriptFile
        val renderer = ParadoxScriptTextPlainRenderer().apply(configure)
        val result = renderer.render(file)
        Assert.assertEquals(expect, result)
    }
}
