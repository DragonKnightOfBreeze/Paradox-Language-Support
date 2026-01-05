package icu.windea.pls.lang.codeInsight.hints.localisation

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.utils.inlays.declarative.DeclarativeInlayHintsProviderTestCase
import icu.windea.pls.core.toClasspathUrl
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markFileInfo
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptedVariableReferenceValueHintsProviderTest : DeclarativeInlayHintsProviderTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @After
    fun clear() = clearIntegrationTest()

    @Test
    fun preview() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_variables/global.txt")
        myFixture.configureByText("global.txt", "@var = 1")

        markFileInfo(ParadoxGameType.Stellaris, "common/test.yml")
        val text = "/inlayProviders/paradox.localisation.scriptedVariableReferenceValue/preview.yml".toClasspathUrl().readText()
        doTest(text)
    }

    @Test
    fun global() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_variables/global.txt")
        myFixture.configureByText("global.txt", "@var = 1")

        markFileInfo(ParadoxGameType.Stellaris, "common/test.yml")
        val text = """
l_default:
 key:0 "value: $@var/*<# => 1 #>*/$"
        """.trimIndent()
        doTest(text)
    }

    @Test
    fun unresolved() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test.yml")
        val text = """
// NO_HINTS
l_default:
 key:0 "value: $@var$"
        """.trimIndent()
        doTest(text)
    }

    private fun doTest(text: String) {
        doTestProvider("test.yml", text, ParadoxScriptedVariableReferenceValueHintsProvider(), verifyHintsPresence = true, testMode = ProviderTestMode.SIMPLE)
    }
}
