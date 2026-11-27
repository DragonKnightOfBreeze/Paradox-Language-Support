@file:Suppress("UnstableApiUsage")

package icu.windea.pls.extensions.markdown.injection

import com.intellij.psi.util.PsiTreeUtil
import icu.windea.pls.base.settings.ChronicleExtensionSettings
import icu.windea.pls.extensions.markdown.MarkdownTestBase
import icu.windea.pls.lang.analysis.ParadoxAnalysisInjectionManager
import icu.windea.pls.lang.resolve.ParadoxConfigService
import icu.windea.pls.lang.resolve.ParadoxMemberService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptProperty
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see MarkdownCodeFenceInjectedFileProcessor
 */
@RunWith(JUnit4::class)
class MarkdownCodeFenceInjectedFileProcessorTest : MarkdownTestBase() {
    @Test
    fun testProcess_injectsFileInfoAndConfigContext() {
        configureScriptCodeFence()

        val injectedFile = findInjectedFile()
        MarkdownCodeFenceInjectedFileProcessor().process(injectedFile)

        val fileInfo = ParadoxAnalysisInjectionManager.getInjectedFileInfo(injectedFile.virtualFile)
        assertNotNull(fileInfo)
        assertEquals(ParadoxGameType.Stellaris, fileInfo!!.gameType)
        assertEquals("common/events/test_events.txt", fileInfo.path.toString())

        val eventProperty = PsiTreeUtil.findChildrenOfType(injectedFile, ParadoxScriptProperty::class.java)
            .firstOrNull { it.name == "event" }
        assertNotNull(eventProperty)
        assertSame(ParadoxScriptLanguage, eventProperty!!.language)
        assertNotNull(selectGameType(eventProperty))
        assertNotNull(ParadoxMemberService.getPath(eventProperty))
        assertNotNull(ParadoxConfigService.getConfigContext(eventProperty))
    }

    @Test
    fun testProcess_doesNothingWhenDisabled() {
        ChronicleExtensionSettings.getInstance().state.markdown.injectCodeBlocks = false
        configureScriptCodeFence()

        val injectedFile = findInjectedFile()
        MarkdownCodeFenceInjectedFileProcessor().process(injectedFile)

        assertNull(ParadoxAnalysisInjectionManager.getInjectedFileInfo(injectedFile.virtualFile))
    }

    private fun configureScriptCodeFence() {
        configureMarkdown(
            """
            ```PARADOX_SCRIPT path=stellaris:common/events/test_events.txt
            namespace = hello_world
            event = {
                id = hello_world.1
            }
            ```
            """.trimIndent()
        )
    }
}
