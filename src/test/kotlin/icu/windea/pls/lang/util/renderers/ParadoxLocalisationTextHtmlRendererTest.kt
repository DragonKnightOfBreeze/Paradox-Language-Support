package icu.windea.pls.lang.util.renderers

import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.ColorUtil
import com.jetbrains.rd.util.AtomicInteger
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
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
import java.awt.Color
import icu.windea.pls.model.ParadoxGameType

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxLocalisationTextHtmlRendererTest : BasePlatformTestCase() {
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
        assertResult("Value<br>\nNew line", "Value\\nNew line")
        assertResult("Value", "Value\nNew line")
    }

    @Test
    fun text_empty() {
        assertResult("", "")
    }

    @Test
    fun text_escapeXml() {
        assertResult("Value &lt;b&gt; &amp;", "Value <b> &")
    }

    @Test
    fun text_withQuotes() {
        // localisation text may contain quotes without requiring escaping in many cases
        // HTML output should escape quotes.
        assertResult("Value &quot;quoted&quot;", "Value \"quoted\"")
    }

    @Test
    fun text_escapedTab() {
        assertResult("a&emsp;b", "a\\tb")
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
        assertResult("Scripted variable: <code>$@unresolved$</code>", "Scripted variable: $@unresolved$")
    }

    @Test
    fun parameter_empty() {
        // empty parameter name should be treated as raw text (fallback path)
        assertResult("Empty: <code>$$</code>", "Empty: $$")
    }

    @Test
    fun command_empty() {
        assertResult("Empty: <code>[]</code>", "Empty: []")
    }

    @Test
    fun text_withColor() {
        val c = Color(1, 2, 3)
        val hex = ColorUtil.toHex(c, true)
        assertResult("<span style=\"color: #$hex\">Value</span>", "Value") { color = c }
    }

    @Test
    fun colorfulText_withColorful_true() {
        withColorful(true) {
            configureFile("interface/fonts.gfx")

            IndexingTestUtil.waitUntilIndexesAreReady(project)

            val redColor = Color(252, 86, 70)
            val redHex = ColorUtil.toHex(redColor, true)
            assertResult("Colorful text: <span style=\"color: #$redHex\">Red text</span>", "Colorful text: §RRed text§!")
            assertResult("Colorful text: Green text", "Colorful text: §GGreen text§!")
        }
    }

    @Test
    fun colorfulText_withColorful_false() {
        withColorful(false) {
            configureFile("interface/fonts.gfx")

            IndexingTestUtil.waitUntilIndexesAreReady(project)

            assertResult("Colorful text: Red text", "Colorful text: §RRed text§!")
        }
    }

    @Test
    fun colorfulText_unknownColorId() {
        withColorful(true) {
            configureFile("interface/fonts.gfx")

            IndexingTestUtil.waitUntilIndexesAreReady(project)

            // `X` is not defined in fonts.gfx -> should not apply any span, but should strip markers
            assertResult("Colorful text: Unknown", "Colorful text: §XUnknown§!")
        }
    }

    @Test
    fun colorfulText_empty() {
        withColorful(true) {
            assertResult("Colorful text: ", "Colorful text: §R§!")
        }
    }

    @Test
    fun parameter_withColorful_true() {
        withColorful(true) {
            configureFile("interface/fonts.gfx")
            configureFile("localisation/main.test.yml")

            IndexingTestUtil.waitUntilIndexesAreReady(project)

            assertResult("Parameter: <code>\$KEY$</code> and <code>\$KEY|Y$</code>", "Parameter: \$KEY$ and \$KEY|Y$")
            assertResult("Unresolved: <code>\$unresolved$</code>", "Unresolved: \$unresolved$")
            assertResult("Recursion: <code>\$key$</code>", "Recursion: \$key$")

            assertResult("Hello world from Windea", "Hello world from \$name_windea$")

            val blueColor = Color(51, 167, 255)
            val blueHex = ColorUtil.toHex(blueColor, true)
            assertResult("Windea <span style=\"color: #$blueHex\">The Unfading</span>", "\$name_windea$ \$title_windea|B$")
        }
    }

    @Test
    fun parameter_withColorful_false() {
        withColorful(false) {
            configureFile("interface/fonts.gfx")
            configureFile("localisation/main.test.yml")

            IndexingTestUtil.waitUntilIndexesAreReady(project)

            assertResult("Windea The Unfading", "\$name_windea$ \$title_windea|B$")
        }
    }

    @Test
    fun command_simple() {
        assertResult("Command: <code>[Root.GetName]</code>", "Command: [Root.GetName]")
    }

    @Test
    fun conceptCommand_simple() {
        configureFile("common/game_concepts/game_concepts.test.txt")
        configureFile("localisation/game_concepts.test.yml")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        val r = render("Concept: ['concept_foo', Foo]")
        Assert.assertTrue(r.contains("Foo"))
        Assert.assertFalse(r.contains("psi_element://"))
    }

    @Test
    fun conceptCommand_alias_simple() {
        configureFile("common/game_concepts/game_concepts_alias.test.txt")
        configureFile("localisation/game_concepts.test.yml")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        val r = render("Concept: ['concept_bar', Bar]")
        Assert.assertTrue(r.contains("Bar"))
        Assert.assertFalse(r.contains("psi_element://"))
    }

    @Test
    fun conceptCommand_tooltipOverride_simple() {
        configureFile("common/game_concepts/game_concepts_override.test.txt")
        configureFile("localisation/game_concepts_override.test.yml")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        // no explicit conceptText -> should use tooltip_override
        val r = render("Concept: ['concept_foo']")
        // should still link to definition, but display override text
        Assert.assertTrue(r.contains("Tooltip Text"))
        Assert.assertFalse(r.contains("psi_element://"))
    }

    private fun configureFile(path: String) {
        markFileInfo(gameType, path)
        myFixture.copyFileToProject("features/renderers/$path", path)
    }

    private fun <R> withColorful(value: Boolean, action: () -> R) {
        val property = PlsSettings.getInstance().state.others::renderLocalisationColorfulText.toAtomicProperty()
        val old = property.get()
        try {
            property.set(value)
            action()
        } finally {
            property.set(old)
        }
    }

    private fun render(input: String, configure: ParadoxLocalisationTextHtmlRenderSettings.() -> Unit = {}): String {
        val id = counter.getAndIncrement()
        markFileInfo(gameType, "localisation/renderer_test_$id.yml")
        myFixture.configureByText("renderer_test_$id.yml", "l_english:\n key:0 \"$input\"")
        val file = myFixture.file as ParadoxLocalisationFile
        val property = file.properties.first()
        val renderer = ParadoxLocalisationTextHtmlRenderer().apply { settings.configure() }
        return renderer.render(property)
    }

    private fun assertResult(expect: String, input: String, configure: ParadoxLocalisationTextHtmlRenderSettings.() -> Unit = {}) {
        val result = render(input, configure)
        Assert.assertEquals(expect, result)
    }
}
