package icu.windea.pls.lang.codeInsight.hints

import com.intellij.codeInsight.hints.InlayDumpUtil
import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.utils.inlays.declarative.DeclarativeInlayHintsProviderTestCase
import icu.windea.pls.lang.codeInsight.hints.script.ParadoxInlineMathResultHintsProvider
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ParadoxInlineMathResultHintsProviderTest : DeclarativeInlayHintsProviderTestCase() {
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
        val sourceText = InlayDumpUtil.removeInlays(text)
        myFixture.configureByText("test.test.txt", sourceText)
        doTestProviderWithConfigured(sourceText, text, ParadoxInlineMathResultHintsProvider(), emptyMap<String, Boolean>(), verifyHintsPresence = true, testMode = ProviderTestMode.SIMPLE)
    }

    override fun getProjectDescriptor(): LightProjectDescriptor {
        return LightProjectDescriptor.EMPTY_PROJECT_DESCRIPTOR
    }
}
