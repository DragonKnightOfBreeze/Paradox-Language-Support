@file:Suppress("UnstableApiUsage")

package icu.windea.pls.extensions.markdown.refactoring.rename

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
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.extensions.markdown.MarkdownTestBase
import icu.windea.pls.extensions.markdown.psi.MarkdownInlineCodeSymbol
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see MarkdownInlineCodeRenameUsageSearcher
 */
@RunWith(JUnit4::class)
class MarkdownInlineCodeRenameUsageSearcherTest : MarkdownTestBase() {
    @Test
    fun testCollectSearchRequests_returnsEmptyForNamelessSymbol() {
        configureMarkdown("`windea`")
        val codeSpan = findCodeSpan()
        val symbol = MarkdownInlineCodeSymbol(codeSpan.firstChild ?: error("Expected a leaf element"))

        assertTrue(collectSearchRequests(symbol).isEmpty())
    }

    @Test
    fun testCollectSearchRequests_includesDeclarationAndReferences() {
        val scriptedVariableFile = addScriptedVariableFile(
            "common/scripted_variables/00_test.txt",
            "@greeting = \"hello\""
        )
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        configureMarkdown("`@greeting`")

        val symbol = findInlineCodeSymbol()
        val usages = collectSearchRequests(symbol)
        assertFalse(usages.isEmpty())
        assertTrue(usages.any { it.declaration })

        val markdownUsage = usages.filterIsInstance<PsiRenameUsage>()
            .first { !it.declaration && it.file.name == "test.md" }
        assertEquals("greeting", markdownUsage.range.substring(markdownUsage.file.text))

        val declarationUsage = usages.filterIsInstance<ModifiableRenameUsage>().single { it.declaration }
        val update = declarationUsage.modelUpdater?.prepareModelUpdate(declarationUsage)
            ?: error("Expected a model update")
        WriteCommandAction.runWriteCommandAction(project) { update.updateModel("farewell") }
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        assertTrue(textOf(scriptedVariableFile).contains("@farewell = \"hello\""))
    }

    private fun collectSearchRequests(renameTarget: RenameTarget): List<RenameUsage> {
        val parameters = object : RenameUsageSearchParameters {
            override fun getProject(): Project = this@MarkdownInlineCodeRenameUsageSearcherTest.project
            override val target: RenameTarget get() = renameTarget
            override val searchScope: SearchScope get() = GlobalSearchScope.projectScope(project)
            override fun areValid(): Boolean = true
        }
        return runSmartReadAction {
            MarkdownInlineCodeRenameUsageSearcher().collectSearchRequests(parameters).flatMap { it.findAll() }
        }
    }
}
