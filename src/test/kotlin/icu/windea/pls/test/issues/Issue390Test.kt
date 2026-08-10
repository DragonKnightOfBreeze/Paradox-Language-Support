package icu.windea.pls.test.issues

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.annotator.ParadoxScriptSemanticAnnotator
import icu.windea.pls.lang.resolve.providers.ParadoxAnnotateProvider
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import icu.windea.pls.test.dsl.highlightingScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import icu.windea.pls.script.editor.ParadoxScriptHighlighterColors as Colors

/**
 * See: [#390](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/390)
 *
 * @see ParadoxScriptSemanticAnnotator
 * @see ParadoxAnnotateProvider
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue390Test : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/390")
        markConfigDirectory("issues/390/.config")
        initConfigGroups(project, ParadoxGameType.Stellaris) // on demand
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testSemanticAnnotator() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_effects/test.txt")
        myFixture.configureByText("test.txt", highlightingScope {
            """
            ${info(Colors.DEFINITION)}test_effect${infoEnd()} = {
                ${info(Colors.SCOPE_PREFIX)}event_target:${infoEnd()}${info(Colors.DYNAMIC_VALUE)}test_target${infoEnd()} = {}
                ${info(Colors.SYSTEM_SCOPE)}root${infoEnd()}${info(Colors.OPERATOR)}.${infoEnd()}${info(Colors.SCOPE_PREFIX)}event_target:${infoEnd()}${info(Colors.DYNAMIC_VALUE)}test_target${infoEnd()} = {}
            }
            """.trimIndent()
        })
        myFixture.checkHighlighting(false, true, false)
    }
}
