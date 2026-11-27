@file:Suppress("UnstableApiUsage")

package icu.windea.pls.extensions.markdown

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import icu.windea.pls.lang.analysis.ParadoxAnalysisInjectionManager
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see MarkdownExtensionManager
 */
@RunWith(JUnit4::class)
class MarkdownExtensionManagerTest : MarkdownTestBase() {
    // region getIdentifierFromInlineCode

    @Test
    fun testGetIdentifierFromInlineCode_singleBacktick() {
        configureMarkdown("`foo`")

        val identifier = MarkdownExtensionManager.getIdentifierFromInlineCode(findCodeSpan())
        assertEquals("foo", identifier)
    }

    @Test
    fun testGetIdentifierFromInlineCode_withBlank() {
        configureMarkdown("`foo bar`")

        val identifier = MarkdownExtensionManager.getIdentifierFromInlineCode(findCodeSpan())
        assertEquals("foo bar", identifier)
    }

    @Test
    fun testGetIdentifierFromInlineCode_blankContent() {
        configureMarkdown("` `")

        val identifier = MarkdownExtensionManager.getIdentifierFromInlineCode(findCodeSpan())
        assertEquals(" ", identifier)
    }

    @Test
    fun testGetIdentifierFromInlineCode_doubleBackticks() {
        configureMarkdown("``foo``")

        val identifier = MarkdownExtensionManager.getIdentifierFromInlineCode(findCodeSpan())
        assertEquals("foo", identifier)
    }

    @Test
    fun testGetIdentifierFromInlineCode_notCodeSpan() {
        configureMarkdown("plain text")

        val identifier = MarkdownExtensionManager.getIdentifierFromInlineCode(myFixture.file)
        assertNull(identifier)
    }

    // endregion

    // region getPathInjectionInfo

    @Test
    fun testGetPathInjectionInfo_valid() {
        configureCodeFence("PARADOX_SCRIPT path=stellaris:common/events/test_events.txt")

        val info = MarkdownExtensionManager.getPathInjectionInfo(findCodeFence())
        assertNotNull(info)
        assertEquals(ParadoxGameType.Stellaris, info!!.gameType)
        assertEquals("common/events/test_events.txt", info.path)
    }

    @Test
    fun testGetPathInjectionInfo_cached() {
        configureCodeFence("PARADOX_SCRIPT path=stellaris:common/events/test_events.txt")

        val first = MarkdownExtensionManager.getPathInjectionInfo(findCodeFence())
        val second = MarkdownExtensionManager.getPathInjectionInfo(findCodeFence())
        assertSame(first, second)
    }

    @Test
    fun testGetPathInjectionInfo_languageIdIsCaseInsensitive() {
        configureCodeFence("PaRaDoX_ScRiPt path=stellaris:common/events/test_events.txt")

        val info = MarkdownExtensionManager.getPathInjectionInfo(findCodeFence())
        assertNotNull(info)
        assertEquals(ParadoxGameType.Stellaris, info!!.gameType)
    }

    @Test
    fun testGetPathInjectionInfo_missingPath() {
        configureCodeFence("PARADOX_SCRIPT")

        assertNull(MarkdownExtensionManager.getPathInjectionInfo(findCodeFence()))
    }

    @Test
    fun testGetPathInjectionInfo_invalidGameType() {
        configureCodeFence("PARADOX_SCRIPT path=unknown:common/events/test_events.txt")

        assertNull(MarkdownExtensionManager.getPathInjectionInfo(findCodeFence()))
    }

    @Test
    fun testGetPathInjectionInfo_emptyPath() {
        configureCodeFence("PARADOX_SCRIPT path=stellaris:")

        assertNull(MarkdownExtensionManager.getPathInjectionInfo(findCodeFence()))
    }

    @Test
    fun testGetPathInjectionInfo_notParadoxLanguage() {
        configureCodeFence("markdown path=stellaris:common/events/test_events.txt")

        assertNull(MarkdownExtensionManager.getPathInjectionInfo(findCodeFence()))
    }

    @Test
    fun testGetPathInjectionInfo_missingMetaInfo() {
        configureCodeFence("PARADOX_SCRIPT foo=bar")

        assertNull(MarkdownExtensionManager.getPathInjectionInfo(findCodeFence()))
    }

    // endregion

    // region getInjectedFileInfoFromInjectedFile

    @Test
    fun testGetInjectedFileInfoFromInjectedFile_scriptFile() {
        configureCodeFence("PARADOX_SCRIPT path=stellaris:common/events/test_events.txt")
        val rootFile = myFixture.tempDirFixture.findOrCreateDir("game")
        val rootInfo = ParadoxRootInfo.Injected(rootFile, ParadoxGameType.Stellaris)
        ParadoxAnalysisInjectionManager.injectRootInfo(rootFile, rootInfo)
        myFixture.file.virtualFile.injectFileInfo(rootInfo, "common/tests/test.md", group = ParadoxFileGroup.Other)

        val fileInfo = MarkdownExtensionManager.getInjectedFileInfoFromInjectedFile(findCodeFence())
        assertNotNull(fileInfo)
        assertEquals(ParadoxGameType.Stellaris, fileInfo!!.gameType)
        assertEquals("common/events/test_events.txt", fileInfo.path.toString())
        assertEquals(ParadoxFileGroup.Script, fileInfo.group)
        assertSame(rootInfo, fileInfo.rootInfo)
    }

    @Test
    fun testGetInjectedFileInfoFromInjectedFile_localisationFile() {
        configureCodeFence("PARADOX_LOCALISATION path=stellaris:localisation/test_l_english.yml")

        val fileInfo = MarkdownExtensionManager.getInjectedFileInfoFromInjectedFile(findCodeFence())
        assertNotNull(fileInfo)
        assertEquals(ParadoxGameType.Stellaris, fileInfo!!.gameType)
        assertEquals("localisation/test_l_english.yml", fileInfo.path.toString())
        assertEquals(ParadoxFileGroup.Localisation, fileInfo.group)
    }

    @Test
    fun testGetInjectedFileInfoFromInjectedFile_unsupportedExtension() {
        configureCodeFence("PARADOX_SCRIPT path=stellaris:README.md")

        assertNull(MarkdownExtensionManager.getInjectedFileInfoFromInjectedFile(findCodeFence()))
    }

    @Test
    fun testGetInjectedFileInfoFromInjectedFile_standaloneMarkdown() {
        configureMarkdownWithoutFileInfo(
            """
            ```PARADOX_SCRIPT path=stellaris:common/events/test_events.txt
            namespace = test
            ```
            """.trimIndent()
        )

        val fileInfo = MarkdownExtensionManager.getInjectedFileInfoFromInjectedFile(findCodeFence())
        assertNotNull(fileInfo)
        val rootInfo = fileInfo!!.rootInfo
        assertTrue(rootInfo is ParadoxRootInfo.Injected)
        assertEquals(ParadoxGameType.Stellaris, rootInfo.gameType)
        assertNull(rootInfo.rootFile)
    }

    // endregion

    // region getCodeFenceFromInjectedFile

    @Test
    fun testGetCodeFenceFromInjectedFile() {
        configureMarkdown(
            """
            ```PARADOX_SCRIPT path=stellaris:common/events/test_events.txt
            namespace = test
            ```
            """.trimIndent()
        )
        val codeFence = findCodeFence()

        val injectedFile = InjectedLanguageManager.getInstance(project)
            .getInjectedPsiFiles(codeFence)
            ?.mapNotNull { it.first as? PsiFile }
            ?.singleOrNull()
        assertNotNull(injectedFile)
        assertSame(codeFence, MarkdownExtensionManager.getCodeFenceFromInjectedFile(injectedFile!!))
    }

    // endregion

    // region resolveInfoFromInlineCode

    @Test
    fun testResolveInfoFromInlineCode_plainName() {
        configureMarkdown("`foo`")

        val info = MarkdownExtensionManager.resolveInfoFromInlineCode(findCodeSpan())
        assertNotNull(info)
        assertEquals("", info!!.prefix)
        assertEquals("foo", info.name)
        assertEquals(1, info.rangeInElement.startOffset)
        assertEquals(4, info.rangeInElement.endOffset)
    }

    @Test
    fun testResolveInfoFromInlineCode_scriptedVariablePrefix() {
        configureMarkdown("`@greeting`")

        val info = MarkdownExtensionManager.resolveInfoFromInlineCode(findCodeSpan())
        assertNotNull(info)
        assertEquals("@", info!!.prefix)
        assertEquals("greeting", info.name)
        assertEquals(2, info.rangeInElement.startOffset)
        assertEquals(10, info.rangeInElement.endOffset)
    }

    @Test
    fun testResolveInfoFromInlineCode_blankContent() {
        configureMarkdown("` `")

        assertNull(MarkdownExtensionManager.resolveInfoFromInlineCode(findCodeSpan()))
    }

    @Test
    fun testResolveInfoFromInlineCode_nameWithBlank() {
        configureMarkdown("`foo bar`")

        assertNull(MarkdownExtensionManager.resolveInfoFromInlineCode(findCodeSpan()))
    }

    @Test
    fun testResolveInfoFromInlineCode_notCodeSpan() {
        configureMarkdown("plain text")

        assertNull(MarkdownExtensionManager.resolveInfoFromInlineCode(myFixture.file))
    }

    // endregion

    // region resolveTargetFromInlineCode

    @Test
    fun testResolveTargetFromInlineCode_scriptedVariable() {
        prepareInlineCodeTargets()
        configureMarkdown("`@greeting`")

        val target = MarkdownExtensionManager.resolveTargetFromInlineCode(findCodeSpan(), "@", "greeting")
        assertTrue(target is ParadoxScriptScriptedVariable)
        assertEquals("greeting", (target as ParadoxScriptScriptedVariable).name)
    }

    @Test
    fun testResolveTargetFromInlineCode_definition() {
        prepareInlineCodeTargets()
        configureMarkdown("`windea`")

        val target = MarkdownExtensionManager.resolveTargetFromInlineCode(findCodeSpan(), "", "windea")
        assertTrue(target is ParadoxDefinitionElement)
        assertEquals("windea", (target as ParadoxDefinitionElement).name)
    }

    @Test
    fun testResolveTargetFromInlineCode_localisation() {
        prepareInlineCodeTargets()
        configureMarkdown("`title_windea`")

        val target = MarkdownExtensionManager.resolveTargetFromInlineCode(findCodeSpan(), "", "title_windea")
        assertTrue(target is ParadoxLocalisationProperty)
        assertEquals("title_windea", (target as ParadoxLocalisationProperty).name)
    }

    @Test
    fun testResolveTargetFromInlineCode_invalidScriptedVariableName() {
        prepareInlineCodeTargets()
        configureMarkdown("`@greeting`")

        assertNull(MarkdownExtensionManager.resolveTargetFromInlineCode(findCodeSpan(), "@", "foo-bar"))
    }

    @Test
    fun testResolveTargetFromInlineCode_invalidLocalisationName() {
        prepareInlineCodeTargets()
        configureMarkdown("`windea`")

        assertNull(MarkdownExtensionManager.resolveTargetFromInlineCode(findCodeSpan(), "", "windea!"))
    }

    @Test
    fun testResolveTargetFromInlineCode_unsupportedPrefix() {
        prepareInlineCodeTargets()
        configureMarkdown("`windea`")

        assertNull(MarkdownExtensionManager.resolveTargetFromInlineCode(findCodeSpan(), "x", "windea"))
    }

    @Test
    fun testResolveTargetFromInlineCode_unresolvedName() {
        prepareInlineCodeTargets()
        configureMarkdown("`missing_name`")

        assertNull(MarkdownExtensionManager.resolveTargetFromInlineCode(findCodeSpan(), "", "missing_name"))
    }

    // endregion

    // region resolveTargetNameFromInlineCode

    @Test
    fun testResolveTargetNameFromInlineCode_scriptedVariable() {
        val target = findElement(
            addScriptedVariableFile("common/scripted_variables/00_test_2.txt", "@farewell = \"bye\""),
            ParadoxScriptScriptedVariable::class.java
        )

        assertEquals("farewell", MarkdownExtensionManager.resolveTargetNameFromInlineCode(target))
    }

    @Test
    fun testResolveTargetNameFromInlineCode_definition() {
        val target = findElement(copyFixture("common/characters/00_characters.txt"), ParadoxDefinitionElement::class.java)

        assertEquals("windea", MarkdownExtensionManager.resolveTargetNameFromInlineCode(target))
    }

    @Test
    fun testResolveTargetNameFromInlineCode_localisation() {
        val target = findElement(copyFixture("localisation/main_l_english.yml"), ParadoxLocalisationProperty::class.java)

        assertEquals("name_windea", MarkdownExtensionManager.resolveTargetNameFromInlineCode(target))
    }

    @Test
    fun testResolveTargetNameFromInlineCode_otherNamedElement() {
        val file = myFixture.addFileToProject("common/tests/plain.txt", "content").virtualFile
        val target = PsiManager.getInstance(project).findFile(file) ?: error("Expected a PSI file")

        assertEquals("plain.txt", MarkdownExtensionManager.resolveTargetNameFromInlineCode(target))
    }

    // endregion

    private fun <T : com.intellij.psi.PsiElement> findElement(file: VirtualFile, type: Class<T>): T {
        val psiFile = PsiManager.getInstance(project).findFile(file) ?: error("Expected a PSI file")
        return PsiTreeUtil.findChildOfType(psiFile, type)
            ?: error("Expected an element of ${type.simpleName} in ${file.path}")
    }

    private fun configureCodeFence(languageInfo: String, content: String = "namespace = test") {
        configureMarkdown(
            """
            ```$languageInfo
            $content
            ```
            """.trimIndent()
        )
    }
}
