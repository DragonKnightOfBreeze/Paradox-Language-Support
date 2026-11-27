@file:Suppress("UnstableApiUsage")

package icu.windea.pls.extensions.markdown

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.refactoring.rename.api.ModifiableRenameUsage
import com.intellij.refactoring.rename.api.PsiRenameUsage
import com.intellij.refactoring.rename.api.RenameTarget
import com.intellij.refactoring.rename.api.RenameUsage
import com.intellij.refactoring.rename.api.RenameUsageSearchParameters
import com.intellij.testFramework.IndexingTestUtil
import icu.windea.pls.base.settings.ChronicleExtensionSettings
import icu.windea.pls.base.settings.ChronicleSettings
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.extensions.markdown.psi.MarkdownInlineCodeReferenceProvider
import icu.windea.pls.extensions.markdown.psi.MarkdownInlineCodeRenameUsageSearcher
import icu.windea.pls.extensions.markdown.psi.MarkdownInlineCodeSymbol
import icu.windea.pls.lang.codeInsight.documentation.ParadoxDocumentationTarget
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see MarkdownInlineCodeReferenceProvider
 * @see MarkdownInlineCodeSymbol
 */
@RunWith(JUnit4::class)
class MarkdownInlineCodeReferenceProviderTest : MarkdownTestBase() {
    // region resolve

    @Test
    fun testResolveInlineCode_scriptedVariable() {
        prepareCommonFixtures()
        configureMarkdown("`@greeting`")

        val symbol = findInlineCodeSymbol()
        val element = symbol.element
        assertTrue(element is ParadoxScriptScriptedVariable)
        assertEquals("greeting", (element as ParadoxScriptScriptedVariable).name)
    }

    @Test
    fun testResolveInlineCode_definition() {
        prepareCommonFixtures()
        configureMarkdown("`windea`")

        val symbol = findInlineCodeSymbol()
        val element = symbol.element
        assertTrue(element is ParadoxDefinitionElement)
        assertEquals("windea", (element as ParadoxDefinitionElement).definitionInfo?.name)
    }

    @Test
    fun testResolveInlineCode_localisation() {
        prepareCommonFixtures()
        configureMarkdown("`title_windea`")

        val symbol = findInlineCodeSymbol()
        val element = symbol.element
        assertTrue(element is ParadoxLocalisationProperty)
        assertEquals("title_windea", (element as ParadoxLocalisationProperty).name)
        assertEquals("main_l_english.yml", element.containingFile.name)
    }

    @Test
    fun testResolveInlineCode_localisation_preferredLocale() {
        prepareCommonFixtures()
        ChronicleSettings.getInstance().state.preferredLocale = "l_simp_chinese"
        configureMarkdown("`title_windea`")

        val symbol = findInlineCodeSymbol()
        val element = symbol.element as ParadoxLocalisationProperty
        assertEquals("title_windea", element.name)
        assertEquals("main_l_simp_chinese.yml", element.containingFile.name)
    }

    @Test
    fun testResolveInlineCode_disabledBySettings() {
        prepareCommonFixtures()
        ChronicleExtensionSettings.getInstance().state.markdown.resolveInlineCodes = false
        configureMarkdown("`windea`")

        val codeSpan = findCodeSpan()
        assertNull(MarkdownInlineCodeReferenceProvider().getImplicitReference(codeSpan, codeSpan.textLength / 2))
    }

    // endregion

    // region documentation and navigation

    @Test
    fun testInlineCodeSymbol_documentationAndNavigation() {
        prepareCommonFixtures()
        configureMarkdown("`windea`")

        val symbol = findInlineCodeSymbol()
        val documentationTarget = symbol.documentationTarget
        assertTrue(documentationTarget is ParadoxDocumentationTarget)
        assertSame(symbol.element, (documentationTarget as ParadoxDocumentationTarget).element)
        assertNotNull(documentationTarget.computePresentation())

        val navigationTargets = symbol.getNavigationTargets(project)
        assertFalse(navigationTargets.isEmpty())

        assertEquals("windea", symbol.renameTarget.targetName)
    }

    // endregion

    // region rename from inline code

    @Test
    fun testRenameFromInlineCode_scriptedVariable_preservesPrefix() {
        val scriptedVariableFile = addScriptedVariableFile("common/scripted_variables/00_test.txt", "@greeting = \"hello\"")
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        configureMarkdown("`@greeting`")

        val symbol = findInlineCodeSymbol()
        assertEquals("greeting", symbol.renameTarget.targetName)
        val usages = collectRenameUsages(symbol.renameTarget)
        assertTrue(usages.any { it.declaration })
        val markdownUsage = usages.filterIsInstance<PsiRenameUsage>().first { !it.declaration && it.file.name == "test.md" }
        assertEquals("greeting", markdownUsage.range.substring(markdownUsage.file.text))

        applyRename(usages, "farewell")

        assertEquals("`@farewell`", myFixture.file.text)
        assertTrue(textOf(scriptedVariableFile).contains("@farewell = \"hello\""))
    }

    @Test
    fun testRenameFromInlineCode_definition() {
        val definitionFile = copyFixture("common/characters/00_characters.txt")
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        configureMarkdown("`windea`")

        val symbol = findInlineCodeSymbol()
        assertEquals("windea", symbol.renameTarget.targetName)
        val usages = collectRenameUsages(symbol.renameTarget)
        assertTrue(usages.any { it.declaration })
        assertTrue(usages.any { !it.declaration })

        applyRename(usages, "windea_renamed")

        assertEquals("`windea_renamed`", myFixture.file.text)
        assertTrue(textOf(definitionFile).contains("windea_renamed = {"))
    }

    @Test
    fun testRenameFromInlineCode_localisation() {
        val localisationFile = copyFixture("localisation/main_l_english.yml")
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        configureMarkdown("`title_windea`")

        val symbol = findInlineCodeSymbol()
        assertEquals("title_windea", symbol.renameTarget.targetName)
        val usages = collectRenameUsages(symbol.renameTarget)
        assertTrue(usages.any { it.declaration })
        assertTrue(usages.any { !it.declaration })

        applyRename(usages, "title_windea_renamed")

        assertEquals("`title_windea_renamed`", myFixture.file.text)
        assertTrue(textOf(localisationFile).contains("title_windea_renamed:0"))
    }

    // endregion

    // region rename from target PSI

    @Test
    fun testRenameFromTargetPsi_scriptedVariable_updatesInlineCode() {
        val scriptedVariableFile = addScriptedVariableFile("common/scripted_variables/00_test.txt", "@greeting = \"hello\"")
        val markdownFile = myFixture.addFileToProject("common/tests/reference.md", "`@greeting`").virtualFile
        markdownFile.injectFileInfo(gameType, "common/tests/reference.md", group = ParadoxFileGroup.Other)
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        myFixture.configureFromExistingVirtualFile(scriptedVariableFile)
        val offset = myFixture.file.text.indexOf("greeting") + 1 // inside the name, after `@`
        myFixture.editor.caretModel.moveToOffset(offset)
        myFixture.renameElementAtCaretUsingHandler("farewell")

        assertTrue(textOf(scriptedVariableFile).contains("@farewell = \"hello\""))
        assertEquals("`@farewell`", textOf(markdownFile))
    }

    @Test
    fun testRenameFromTargetPsi_definition_updatesInlineCode() {
        val definitionFile = copyFixture("common/characters/00_characters.txt")
        val markdownFile = myFixture.addFileToProject("common/tests/reference.md", "`windea`").virtualFile
        markdownFile.injectFileInfo(gameType, "common/tests/reference.md", group = ParadoxFileGroup.Other)
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        myFixture.configureFromExistingVirtualFile(definitionFile)
        val offset = myFixture.file.text.indexOf("windea") + 2
        myFixture.editor.caretModel.moveToOffset(offset)
        myFixture.renameElementAtCaretUsingHandler("windea_renamed")

        assertTrue(textOf(definitionFile).contains("windea_renamed = {"))
        assertEquals("`windea_renamed`", textOf(markdownFile))
    }

    // endregion

    private fun collectRenameUsages(renameTarget: RenameTarget): List<RenameUsage> {
        val parameters = object : RenameUsageSearchParameters {
            override fun getProject(): Project = this@MarkdownInlineCodeReferenceProviderTest.project
            override val target: RenameTarget get() = renameTarget
            override val searchScope: SearchScope get() = GlobalSearchScope.projectScope(project)
            override fun areValid(): Boolean = true
        }
        return runSmartReadAction {
            MarkdownInlineCodeRenameUsageSearcher().collectSearchRequests(parameters).flatMap { it.findAll() }
        }
    }

    private fun applyRename(usages: List<RenameUsage>, newName: String) {
        WriteCommandAction.writeCommandAction(project).run<Throwable> {
            // Replace references first, then update the declaration, matching the rename framework's intent.
            usages.filterIsInstance<PsiRenameUsage>()
                .filter { !it.declaration }
                .groupBy { it.file }
                .forEach { (file, fileUsages) ->
                    val document = PsiDocumentManager.getInstance(project).getDocument(file) ?: return@forEach
                    fileUsages.sortedByDescending { it.range.startOffset }.forEach { usage ->
                        document.replaceString(usage.range.startOffset, usage.range.endOffset, newName)
                    }
                }
            usages.filterIsInstance<ModifiableRenameUsage>()
                .filter { it.declaration }
                .forEach { usage ->
                    usage.modelUpdater?.prepareModelUpdate(usage)?.updateModel(newName)
                }
        }
        PsiDocumentManager.getInstance(project).commitAllDocuments()
    }

    private fun prepareCommonFixtures() {
        copyFixture("common/characters/00_characters.txt")
        copyFixture("localisation/main_l_english.yml")
        copyFixture("localisation/main_l_simp_chinese.yml")
        addScriptedVariableFile("common/scripted_variables/00_test.txt", "@greeting = \"hello\"")
        IndexingTestUtil.waitUntilIndexesAreReady(project)
    }
}
