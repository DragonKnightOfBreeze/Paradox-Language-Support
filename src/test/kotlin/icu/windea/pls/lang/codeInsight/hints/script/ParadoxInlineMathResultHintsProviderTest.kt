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
class ParadoxInlineMathResultHintsProviderTest : DeclarativeInlayHintsProviderTestCase() {
    @Before
    fun setup() = markIntegrationTest()

    @After
    fun clear() = clearIntegrationTest()

    @Test
    fun simple() {
        doTest("""
key = @[ 1 + 1 ]/*<# => 2 #>*/
        """.trimIndent())
    }

    @Test
    fun withSv() {
        doTest("""
@var = 1
key = @[ var + 1 ]/*<# => 2 #>*/
        """.trimIndent())
    }

    @Test
    fun notStatic() {
        doTest("""
// NO_HINTS
key = @[ var + 1 ]
        """.trimIndent())
    }

    private fun doTest(text: String) {
        markFileInfo(ParadoxGameType.Stellaris, "common/test.txt")
        doTestProvider("test.txt", text, ParadoxInlineMathResultHintsProvider(), verifyHintsPresence = true, testMode = ProviderTestMode.SIMPLE)
    }
}
