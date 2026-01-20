package icu.windea.pls.lang.util.renderers

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.ColorUtil
import com.jetbrains.rd.util.AtomicInteger
import icu.windea.pls.core.toAtomicProperty
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.model.ParadoxGameType
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
import java.awt.Color

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxLocalisationTextHtmlRendererTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    private val counter = AtomicInteger()
    private val gameType = ParadoxGameType.Vic3

    @Before
    fun setup() {
        markIntegrationTest()
        initConfigGroups(project, gameType)
    }

    @After
    fun clear() = clearIntegrationTest()

    @Test
    fun text() {
        assertResult("Value", "Value")
        assertResult("Value<br>\nNew line", "Value\\nNew line")
        assertResult("Value", "Value\nNew line")
    }

    @Test
    fun text_escapeXml() {
        assertResult("Value &lt;b&gt; &amp;", "Value <b> &")
    }

    @Test
    fun text_withSv() {
        markFileInfo(gameType, "common/scripted_variables/global.txt")
        myFixture.configureByText("global.txt", "@var = 1")

        assertResult("Scripted variable: 1", "Scripted variable: $@var$")
        assertResult("Scripted variable: <code>$@unresolved$</code>", "Scripted variable: $@unresolved$")
    }

    @Test
    fun colorfulText() {
        renderLocalisationColorfulText(true) {
            markFileInfo(gameType, "interface/fonts.gfx")
            myFixture.configureByText("fonts.gfx", mapOf("R" to "{ 252 86 70 }").asTextColors())

            val redColor = Color(252, 86, 70)
            val redHex = ColorUtil.toHex(redColor, true)
            assertResult("Colorful text: <span style=\"color: #$redHex\">Red text</span>", "Colorful text: §RRed text§!")
            assertResult("Colorful text: Green text", "Colorful text: §GGreen text§!")
        }
    }

    @Test
    fun colorfulText_renderLocalisationColorfulText_disabled() {
        renderLocalisationColorfulText(false) {
            markFileInfo(gameType, "interface/fonts.gfx")
            myFixture.configureByText("fonts.gfx", mapOf("R" to "{ 252 86 70 }").asTextColors())

            assertResult("Colorful text: Red text", "Colorful text: §RRed text§!")
        }
    }

    @Test
    fun parameter() {
        renderLocalisationColorfulText(true) {
            markFileInfo(gameType, "interface/fonts.gfx")
            myFixture.configureByText("fonts.gfx", mapOf("B" to "{ 51 167 255 }").asTextColors())

            markFileInfo(gameType, "localisation/main.yml")
            myFixture.configureByText("main.yml", mapOf("name_windea" to "Windea", "title_windea" to "The Unfading").asLocalisations())

            assertResult("Parameter: <code>\$KEY\$</code> and <code>\$KEY|Y\$</code>", "Parameter: \$KEY\$ and \$KEY|Y\$")
            assertResult("Unresolved: <code>\$unresolved\$</code>", "Unresolved: \$unresolved\$")
            assertResult("Recursion: <code>\$key\$</code>", "Recursion: \$key\$")

            assertResult("Hello world from Windea", "Hello world from \$name_windea\$")

            val blueColor = Color(51, 167, 255)
            val blueHex = ColorUtil.toHex(blueColor, true)
            assertResult("Windea <span style=\"color: #$blueHex\">The Unfading</span>", "\$name_windea\$ \$title_windea|B\$")
        }
    }

    @Test
    fun parameter_renderLocalisationColorfulText_disabled() {
        renderLocalisationColorfulText(false) {
            markFileInfo(gameType, "interface/fonts.gfx")
            myFixture.configureByText("fonts.gfx", mapOf("B" to "{ 51 167 255 }").asTextColors())

            markFileInfo(gameType, "localisation/main.yml")
            myFixture.configureByText("main.yml", mapOf("name_windea" to "Windea", "title_windea" to "The Unfading").asLocalisations())

            assertResult("Windea The Unfading", "\$name_windea\$ \$title_windea|B\$")
        }
    }

    @Test
    fun simpleCommand() {
        assertResult("Command: <code>[Root.GetName]</code>", "Command: [Root.GetName]")
    }

    @Test
    fun config_withColor() {
        val c = Color(1, 2, 3)
        val hex = ColorUtil.toHex(c, true)
        assertResult("<span style=\"color: #$hex\">Value</span>", "Value") {
            withColor(c)
        }
    }

    private inline fun <R> renderLocalisationColorfulText(value: Boolean, action: () -> R) {
        val property = PlsSettings.getInstance().state.others::renderLocalisationColorfulText.toAtomicProperty()
        val old = property.get()
        try {
            property.set(value)
            action()
        } finally {
            property.set(old)
        }
    }

    private fun Map<String, String>.asTextColors(): String {
        val itemsString = entries.joinToString(" ") { (k, v) -> "$k = $v" }
        return """bitmapfonts = { textcolors = { $itemsString } }"""
    }

    private fun Map<String, String>.asLocalisations(): String {
        val itemsString = entries.joinToString("\n", " ") { (k, v) -> " $k:0 \"$v\"" }
        return "l_english:\n$itemsString"
    }

    private inline fun assertResult(expect: String, input: String, configure: ParadoxLocalisationTextHtmlRenderer.() -> Unit = {}) {
        val id = counter.getAndIncrement()
        markFileInfo(gameType, "localisation/renderer_test_$id.yml")
        myFixture.configureByText("renderer_test_$id.yml", "l_english:\n key:0 \"$input\"")
        val file = myFixture.file as ParadoxLocalisationFile
        val property = file.properties.first()
        val renderer = ParadoxLocalisationTextHtmlRenderer().apply(configure)
        val result = renderer.render(property)
        Assert.assertEquals(expect, result)
    }
}
