@file:Suppress("UnstableApiUsage")

package icu.windea.pls.extensions.markdown.references

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.IndexingTestUtil
import icu.windea.pls.extensions.markdown.MarkdownTestBase
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see MarkdownInlineCodeReferencesSearcher
 */
@RunWith(JUnit4::class)
class MarkdownInlineCodeReferencesSearcherTest : MarkdownTestBase() {
    @Test
    fun testFindsScriptedVariableInlineCode() {
        val scriptedVariableFile = addScriptedVariableFile(
            "common/scripted_variables/00_test.txt",
            "@greeting = \"hello\""
        )
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        configureMarkdown("`@greeting`")

        val target = findElement(scriptedVariableFile, ParadoxScriptScriptedVariable::class.java, "greeting")
        val references = searchMarkdownReferences(target)
        assertEquals(1, references.size)
        assertEquals("`@greeting`", references.single().element.text)
        assertEquals("greeting", references.single().rangeInElement.substring(references.single().element.text))
    }

    @Test
    fun testFindsLocalisationInlineCode() {
        val localisationFile = copyFixture("localisation/main_l_english.yml")
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        configureMarkdown("`narrative`")

        val target = findElement(localisationFile, ParadoxLocalisationProperty::class.java, "narrative")
        val references = searchMarkdownReferences(target)
        assertEquals(1, references.size)
        assertEquals("`narrative`", references.single().element.text)
    }

    @Test
    fun testIgnoresMismatchedPrefix() {
        val localisationFile = copyFixture("localisation/main_l_english.yml")
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        configureMarkdown("`@narrative`")

        val target = findElement(localisationFile, ParadoxLocalisationProperty::class.java, "narrative")
        assertTrue(searchMarkdownReferences(target).isEmpty())
    }

    private fun searchMarkdownReferences(target: com.intellij.psi.PsiElement): List<MarkdownInlineCodeReference> {
        return ReferencesSearch.search(target, GlobalSearchScope.projectScope(project))
            .findAll()
            .filterIsInstance<MarkdownInlineCodeReference>()
    }

    private fun <T : com.intellij.psi.PsiElement> findElement(
        file: VirtualFile,
        type: Class<T>,
        name: String = type.simpleName,
    ): T {
        val psiFile = PsiManager.getInstance(project).findFile(file) ?: error("Expected a PSI file")
        return PsiTreeUtil.findChildrenOfType(psiFile, type).firstOrNull {
            (it as? com.intellij.psi.PsiNamedElement)?.name == name
        } ?: error("Expected an element named '$name' in ${file.path}")
    }
}

