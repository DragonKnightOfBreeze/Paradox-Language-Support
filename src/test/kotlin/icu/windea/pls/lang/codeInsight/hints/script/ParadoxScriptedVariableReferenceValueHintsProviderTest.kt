package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.testFramework.utils.inlays.declarative.DeclarativeInlayHintsProviderTestCase
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
class ParadoxScriptedVariableReferenceValueHintsProviderTest : DeclarativeInlayHintsProviderTestCase() {
    @Before
    fun setup() = markIntegrationTest()

    @After
    fun clear() = clearIntegrationTest()

    @Test
    fun simple() {
        doTest("""
@var = 1
key = @var/*<# => 1 #>*/
        """.trimIndent())
    }

    @Test
    fun unresolved() {
        doTest("""
// NO_HINTS
key = @var
        """.trimIndent())
    }

    private fun doTest(text: String) {
        markFileInfo(ParadoxGameType.Stellaris, "common/test.txt")
        doTestProvider("test.txt", text, ParadoxScriptedVariableReferenceValueHintsProvider(), verifyHintsPresence = true, testMode = ProviderTestMode.SIMPLE)
    }
}
