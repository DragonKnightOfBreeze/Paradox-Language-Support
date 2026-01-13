package icu.windea.pls.lang.psi.select

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxPsiSelectDslSemanticTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() {
        markIntegrationTest()
        initConfigGroups(project, ParadoxGameType.Stellaris)
    }

    @After
    fun clear() = clearIntegrationTest()

    @Test
    fun nameElement_and_nameFieldElement_variants() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_semantic_name_1.txt")
        myFixture.configureByText(
            "test_semantic_name_1.txt",
            "k1 = { name = foo other = bar }"
        )

        val file = myFixture.file as ParadoxScriptFile
        val k1 = selectScope { file.ofPath("k1").asProperty().one() }
        assertNotNull(k1)
        val definition = k1!!

        val nameElementNull = selectScope { definition.nameElement(null) }
        assertNotNull(nameElementNull)
        assertEquals("k1", nameElementNull!!.text)

        val nameFieldNull = selectScope { definition.nameFieldElement(null) }
        assertNull(nameFieldNull)

        val nameFieldEmpty = selectScope { definition.nameFieldElement("") }
        assertNull(nameFieldEmpty)

        val nameFieldDash = selectScope { definition.nameFieldElement("-") }
        assertNotNull(nameFieldDash)
        assertTrue(nameFieldDash is ParadoxScriptBlock)

        val nameFieldName = selectScope { definition.nameFieldElement("name") }
        assertNotNull(nameFieldName)
        assertEquals("foo", nameFieldName!!.value)

        val nameElementName = selectScope { definition.nameElement("name") }
        assertNotNull(nameElementName)
        assertEquals("foo", (nameElementName as? ParadoxScriptProperty)?.value ?: nameElementName!!.text)

        val nameFieldNotExists = selectScope { definition.nameFieldElement("not_exists") }
        assertNull(nameFieldNotExists)
    }

    @Test
    fun inline_simple() {
        markFileInfo(ParadoxGameType.Stellaris, "common/inline_scripts/test/inline_script.txt")
        myFixture.configureByText("inline_script.txt", "k0 = v0")

        markFileInfo(ParadoxGameType.Stellaris, "common/test_1.txt")
        myFixture.configureByText("test_1.txt", "inline_script = test/inline_script")
        val file1 = myFixture.file as ParadoxScriptFile
        val usage1 = selectScope { file1.ofPath("inline_script").asProperty().one() }
        assertNotNull(usage1)
        val inlined1 = selectScope { file1.ofPath("k0", inline = true).asProperty().one() }
        assertNotNull(inlined1)

        markFileInfo(ParadoxGameType.Stellaris, "common/test_2.txt")
        myFixture.configureByText("test_2.txt", "inline_script = { script = test/inline_script PARAM = var }")
        val file2 = myFixture.file as ParadoxScriptFile
        val usage2 = selectScope { file2.ofPath("inline_script").asProperty().one() }
        assertNotNull(usage2)
        val inlined2 = selectScope { file2.ofPath("k0", inline = true).asProperty().one() }
        assertNotNull(inlined2)
    }

    @Test
    fun inline_anyDepth_usage() {
        markFileInfo(ParadoxGameType.Stellaris, "common/inline_scripts/test/inline_script_depth.txt")
        myFixture.configureByText("inline_script_depth.txt", "k0 = v0")

        markFileInfo(ParadoxGameType.Stellaris, "common/test_inline_depth.txt")
        myFixture.configureByText(
            "test_inline_depth.txt",
            "a = { b = { inline_script = test/inline_script_depth } }"
        )

        val file = myFixture.file as ParadoxScriptFile
        val notInlined = selectScope { file.ofPath("a/b/k0", inline = false).asProperty().one() }
        assertNull(notInlined)

        val inlined = selectScope { file.ofPath("a/b/k0", inline = true).asProperty().one() }
        assertNotNull(inlined)
        assertEquals("k0", inlined!!.name)
    }

    @Test
    fun inline_nestedInline() {
        markFileInfo(ParadoxGameType.Stellaris, "common/inline_scripts/test/inline_script_nested_2.txt")
        myFixture.configureByText("inline_script_nested_2.txt", "k2 = v2")

        markFileInfo(ParadoxGameType.Stellaris, "common/inline_scripts/test/inline_script_nested_1.txt")
        myFixture.configureByText(
            "inline_script_nested_1.txt",
            "inline_script = test/inline_script_nested_2 k1 = v1"
        )

        markFileInfo(ParadoxGameType.Stellaris, "common/test_inline_nested.txt")
        myFixture.configureByText("test_inline_nested.txt", "inline_script = test/inline_script_nested_1")

        val file = myFixture.file as ParadoxScriptFile
        val k2 = selectScope { file.ofPath("k2", inline = true).asProperty().one() }
        assertNotNull(k2)
        assertEquals("v2", k2!!.value)
    }

    @Test
    fun inline_recursive_shouldBeIgnoredSafely() {
        markFileInfo(ParadoxGameType.Stellaris, "common/inline_scripts/test/inline_script_recursive.txt")
        myFixture.configureByText(
            "inline_script_recursive.txt",
            "inline_script = test/inline_script_recursive k0 = v0"
        )

        markFileInfo(ParadoxGameType.Stellaris, "common/test_inline_recursive.txt")
        myFixture.configureByText("test_inline_recursive.txt", "inline_script = test/inline_script_recursive")

        val file = myFixture.file as ParadoxScriptFile
        val k0List = selectScope { file.ofPath("k0", inline = true).asProperty().all() }
        assertEquals(1, k0List.size)
        assertEquals("v0", k0List.single().value)
    }

    @Test
    fun inline_pathWithExtension_shouldNotBeRecognized() {
        markFileInfo(ParadoxGameType.Stellaris, "common/inline_scripts/test/inline_script_ext.txt")
        myFixture.configureByText("inline_script_ext.txt", "k0 = v0")

        markFileInfo(ParadoxGameType.Stellaris, "common/test_inline_ext.txt")
        myFixture.configureByText("test_inline_ext.txt", "inline_script = test/inline_script_ext.txt")

        val file = myFixture.file as ParadoxScriptFile
        val inlined = selectScope { file.ofPath("k0", inline = true).asProperty().one() }
        assertNull(inlined)
    }

    @Test
    fun inline_missingScript_shouldBeIgnoredSafely() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test_inline_missing.txt")
        myFixture.configureByText(
            "test_inline_missing.txt",
            "inline_script = test/not_exists k1 = v1"
        )

        val file = myFixture.file as ParadoxScriptFile
        val k1 = selectScope { file.ofPath("k1").asProperty().one() }
        assertNotNull(k1)

        val inlined = selectScope { file.ofPath("k0", inline = true).asProperty().one() }
        assertNull(inlined)
    }
}
