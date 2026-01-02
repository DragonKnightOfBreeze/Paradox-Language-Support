package icu.windea.pls.lang.codeInsight.hints.localisation

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
    fun global() {
        markFileInfo(ParadoxGameType.Stellaris, "common/scripted_variables/global.txt")
        myFixture.configureByText("global.txt", "@var = 1")

        doTest("""
l_default:
 key:0 "value: $@var/*<# => 1 #>*/$"
        """.trimIndent())
    }

    @Test
    fun unresolved() {
        doTest("""
// NO_HINTS
l_default:
 key:0 "value: $@var$"
        """.trimIndent())
    }

    private fun doTest(text: String) {
        markFileInfo(ParadoxGameType.Stellaris, "common/test.yml")
        doTestProvider("test.yml", text, ParadoxScriptedVariableReferenceValueHintsProvider(), verifyHintsPresence = true, testMode = ProviderTestMode.SIMPLE)
    }
}
