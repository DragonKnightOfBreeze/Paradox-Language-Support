package icu.windea.pls.lang.util.renderers

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.jetbrains.rd.util.AtomicInteger
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.initConfigGroups
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import icu.windea.pls.test.markRootDirectory
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxLocalisationTextPlainRendererTest : BasePlatformTestCase() {
    private val gameType = ParadoxGameType.Stellaris
    private val counter = AtomicInteger()

    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("features/renderers")
        initConfigGroups(project, gameType)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun text() {
        assertResult("Value", "Value")
        assertResult("Value\nNew line", "Value\\nNew line") // line break character should be escaped in input text, to render correctly
        assertResult("Value", "Value\nNew line") // line break character is not escaped in input text, so syntax error, and hence truncated
    }

    @Test
    fun text_empty() {
        assertResult("", "")
    }

    @Test
    fun text_escapedTab() {
        assertResult("a\tb", "a\\tb")
    }

    @Test
    fun text_doubleBracketEscape() {
        assertResult("[text", "[[text")
    }

    @Test
    fun text_withSv() {
        configureFile("common/scripted_variables/global.test.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        assertResult("Scripted variable: 1", "Scripted variable: $@var$")
        assertResult("Scripted variable: $@unresolved$", "Scripted variable: $@unresolved$")
    }

    @Test
    fun colorfulText() {
        configureFile("interface/fonts.gfx")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        assertResult("Colorful text: Red text", "Colorful text: §RRed text§!")
        assertResult("Colorful text: Green text", "Colorful text: §GGreen text§!")
    }

    @Test
    fun colorfulText_unknownColorId() {
        assertResult("Colorful text: Unknown", "Colorful text: §XUnknown§!")
    }

    @Test
    fun colorfulText_empty() {
        assertResult("Colorful text: ", "Colorful text: §R§!")
    }

    @Test
    fun parameter() {
        configureFile("interface/fonts.gfx")
        configureFile("localisation/main.test.yml")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        assertResult("Parameter: \$KEY$ and \$KEY|Y$", "Parameter: \$KEY$ and \$KEY|Y$")
        assertResult("Unresolved: \$unresolved$", "Unresolved: \$unresolved$")
        assertResult("Recursion: \$key$", "Recursion: \$key$")

        assertResult("Hello world from Windea", "Hello world from \$name_windea$")
        assertResult("Windea The Unfading", "\$name_windea$ \$title_windea|B$")
    }

    @Test
    fun parameter_empty() {
        assertResult("Empty: $$", "Empty: $$")
    }

    @Test
    fun command_simple() {
        assertResult("Command: [Root.GetName]", "Command: [Root.GetName]")
    }

    @Test
    fun command_empty() {
        assertResult("Empty: []", "Empty: []")
    }

    @Test
    fun conceptCommand_simple() {
        configureFile("common/game_concepts/game_concepts.test.txt")
        configureFile("localisation/game_concepts.test.yml")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        val r = render("Concept: ['concept_foo', Foo]")
        Assert.assertTrue(r.contains("Foo"))
    }

    @Test
    fun conceptCommand_alias_simple() {
        configureFile("common/game_concepts/game_concepts_alias.test.txt")
        configureFile("localisation/game_concepts.test.yml")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(gameType, "common/game_concepts/test_alias.txt")
        myFixture.configureByText(
            "test_alias.txt",
            """
                concept_foo = {
                    alias = { concept_bar }
                }
            """.trimIndent()
        )

        val r = render("Concept: ['concept_bar', Bar]")
        Assert.assertTrue(r.contains("Bar"))
    }

    @Test
    fun conceptCommand_tooltipOverride_simple() {
        configureFile("common/game_concepts/game_concepts_override.test.txt")
        configureFile("localisation/game_concepts_override.test.yml")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        val r = render("Concept: ['concept_foo']")
        Assert.assertTrue(r.contains("Tooltip Text"))
    }

    private fun configureFile(path: String) {
        markFileInfo(gameType, path)
        myFixture.copyFileToProject("features/renderers/$path", path)
    }

    private fun render(input: String, configure: ParadoxLocalisationTextPlainRenderSettings.() -> Unit = {}): String {
        val id = counter.getAndIncrement()
        markFileInfo(gameType, "localisation/renderer_test_$id.yml")
        myFixture.configureByText("renderer_test.yml", "l_english:\n key:0 \"$input\"")
        val file = myFixture.file as ParadoxLocalisationFile
        val property = file.properties.first()
        val renderer = ParadoxLocalisationTextPlainRenderer().apply { settings.configure() }
        return renderer.render(property)
    }

    private fun assertResult(expect: String, input: String, configure: ParadoxLocalisationTextPlainRenderSettings.() -> Unit = {}) {
        val result = render(input, configure)
        Assert.assertEquals(expect, result)
    }
}
