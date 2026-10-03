package icu.windea.pls.test.issues

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptColor
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.test.ChronicleTestScope
import icu.windea.pls.test.dsl.expectScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * See: [#399](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/399)
 *
 * Color types were matched case-sensitively: `rgb { ... }` was recognized as a `color_field`, but
 * `RGB { ... }` (and other capitalizations) was not. The same applied to `hsv` and `hsv360`.
 *
 * Cause: the lexer/parser matched the color type keywords case-sensitively, but they should be
 * case-insensitive (the patterns are defined in the color patterns).
 * 
 * @see icu.windea.pls.script.psi.ParadoxScriptColor
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue399Test : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testSyntax_ColorTypes_ShouldIgnoreCase() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.txt")
        myFixture.configureByText("test.txt", """
            color = rgb { 255 0 0 }
            color = Rgb { 255 0 0 }
            color = RGB { 255 0 0 }
            color = hsv { 0 1.0 1.0 }
            color = Hsv { 0 1.0 1.0 }
            color = HSV { 0 1.0 1.0 }
            color = hsv360 { 0 100 100 }
            color = Hsv360 { 0 100 100 }
            color = HSV360 { 0 100 100 }
        """.trimIndent())

        expectScope {
            val file = myFixture.file as ParadoxScriptFile
            val colors = selectScope { file.queryBy("color").asProperty() }.mapNotNull { it.propertyValue }.toList()
            colors.size.expectEquals(9)
            colors.all { it is ParadoxScriptColor }.expectTrue()
        }
    }
}
