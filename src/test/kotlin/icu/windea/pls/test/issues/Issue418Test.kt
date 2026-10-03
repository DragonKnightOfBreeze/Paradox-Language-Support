package icu.windea.pls.test.issues

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import icu.windea.pls.test.dsl.configureByText
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import icu.windea.pls.localisation.highlighting.ParadoxLocalisationHighlighterColors as Colors

/**
 * See: [#418](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/418)
 *
 * Two regressions:
 * - Localisation text starting with a leading blank (e.g., a leading `\n`) was not rendered in inlay hints.
 * - `<effect>log` text containing commands (e.g., `[This.GetName]`) was not recognized as localisation text,
 *   so it was not injected/rendered like localisation.
 *
 * Cause:
 * - In the localisation language injection service, the call to the rich-localisation-text check was not
 *   negated, so the injection was accepted only for plain (non-rich) text instead of rich text.
 * - In the localisation text inlay render context, the leading blank of the whole localisation text was not
 *   trimmed before rendering the inlay hint, so the text starting with a blank was skipped.
 *
 * @see icu.windea.pls.lang.injection.ParadoxLanguageInjectionService.acceptLocalisationTextInjection
 * @see icu.windea.pls.lang.util.ParadoxLocalisationManager.isRichLocalisationText
 * @see icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextInlayRenderContext.truncatedSmallText
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue418Test : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/418")
        markConfigDirectory("issues/418/.config")
        initInjectedConfigGroups(project, ParadoxGameType.Stellaris) // on demand
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testSemanticAnnotator() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.txt")
        myFixture.configureByText("test.txt") {
            val text = inject() +
                "${info(Colors.TEXT)}Log: ${infoEnd()}" +
                "${info(Colors.MARKER)}[${infoEnd()}" +
                "${info(Colors.SYSTEM_COMMAND_SCOPE)}Root${infoEnd()}" +
                "${info(Colors.OPERATOR)}.${infoEnd()}" +
                "${info(Colors.COMMAND_FIELD)}GetName${infoEnd()}" +
                "${info(Colors.MARKER)}]${infoEnd()}" +
                injectEnd()
            """
            test = {
                log = "${text}"
            }
            """.trimIndent()
        }
    }

    @Test
    fun testSemanticAnnotator_notApplicableSinceNoSpecialCharacters() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test/test.txt")
        myFixture.configureByText("test.txt") {
            val text = "log: Root.GetName"
            """
            test = {
                log = "${text}"
            }
            """.trimIndent()
        }
    }
}
