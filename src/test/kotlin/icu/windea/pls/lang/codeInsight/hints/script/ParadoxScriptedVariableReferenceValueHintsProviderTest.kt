package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.utils.inlays.declarative.DeclarativeInlayHintsProviderTestCase
import icu.windea.pls.core.loadText
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
        markFileInfo(ParadoxGameType.Stellaris, "common/test.txt")
        val text = loadText("/inlayProviders/paradox.script.scriptedVariableReferenceValue/preview.txt")
        doTest(text)
    }

    @Test
    fun local() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test.txt")
        val text = """
@var = 1
key = @var/*<# => 1 #>*/
        """.trimIndent()
        doTest(text)
    }

    @Test
    fun global() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_variables/global.txt")
        myFixture.configureByText("global.txt", "@var = 1")

        markFileInfo(ParadoxGameType.Stellaris, "common/test.txt")
        val text = """
key = @var/*<# => 1 #>*/
        """.trimIndent()
        doTest(text)
    }

    @Test
    fun unresolved() {
        markFileInfo(ParadoxGameType.Stellaris, "common/test.txt")
        val text = """
// NO_HINTS
key = @var
        """.trimIndent()
        doTest(text)
    }

    private fun doTest(text: String) {
        doTestProvider("test.txt", text, ParadoxScriptedVariableReferenceValueHintsProvider(), verifyHintsPresence = true, testMode = ProviderTestMode.SIMPLE)
    }
}
