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
 * @see icu.windea.pls.lang.util.ParadoxLocalisationManager.isSpecialLocalisation
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
