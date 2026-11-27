@file:Suppress("UnstableApiUsage")

package icu.windea.pls.extensions.markdown.references

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.IndexingTestUtil
import icu.windea.pls.base.settings.ChronicleExtensionSettings
import icu.windea.pls.extensions.markdown.MarkdownTestBase
import icu.windea.pls.lang.references.paths.ParadoxPathReference
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownLinkDestination
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see MarkdownLinkReferenceContributor
 */
@RunWith(JUnit4::class)
class MarkdownLinkReferenceContributorTest : MarkdownTestBase() {
    @Test
    fun testProvidesParadoxPathReference() {
        copyFixture("localisation/main_l_english.yml")
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        configureMarkdown("[valid](pdx.l:stellaris:title_windea)")

        val reference = findParadoxPathReference()
        assertNotNull(reference)
        assertEquals("pdx.l:stellaris:title_windea", reference!!.link)
        assertNotNull(reference.resolve())
    }

    @Test
    fun testProvidesUnresolvedParadoxPathReference() {
        copyFixture("localisation/main_l_english.yml")
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        configureMarkdown("[invalid](pdx.l:stellaris:missing)")

        val reference = findParadoxPathReference()
        assertNotNull(reference)
        assertEquals("pdx.l:stellaris:missing", reference!!.link)
        assertNull(reference.resolve())
    }

    @Test
    fun testIgnoresNonParadoxLink() {
        configureMarkdown("[file](other.md)")

        assertNull(findParadoxPathReference())
    }

    @Test
    fun testIgnoresParadoxLinkWhenDisabled() {
        copyFixture("localisation/main_l_english.yml")
        IndexingTestUtil.waitUntilIndexesAreReady(project)
        ChronicleExtensionSettings.getInstance().state.markdown.resolveLinks = false
        configureMarkdown("[valid](pdx.l:stellaris:title_windea)")

        assertNull(findParadoxPathReference())
    }

    private fun findParadoxPathReference(): ParadoxPathReference? {
        val destination = PsiTreeUtil.findChildOfType(myFixture.file, MarkdownLinkDestination::class.java)
            ?: error("Expected a Markdown link destination")
        return destination.references.filterIsInstance<ParadoxPathReference>().firstOrNull()
    }
}
