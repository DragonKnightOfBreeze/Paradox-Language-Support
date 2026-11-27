@file:Suppress("UnstableApiUsage")

package icu.windea.pls.extensions.markdown.inspections

import com.intellij.testFramework.IndexingTestUtil
import icu.windea.pls.base.settings.ChronicleExtensionSettings
import icu.windea.pls.ep.ChronicleEpBundle
import icu.windea.pls.extensions.markdown.MarkdownTestBase
import icu.windea.pls.test.dsl.highlightingScope
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see MarkdownUnresolvedReferenceLinkInspection
 */
@RunWith(JUnit4::class)
class MarkdownUnresolvedReferenceLinkInspectionTest : MarkdownTestBase() {
    @Test
    fun testInspection_validAndUnresolvedLinks() {
        copyFixture("localisation/main_l_english.yml")
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        myFixture.enableInspections(MarkdownUnresolvedReferenceLinkInspection::class.java)
        val message = ChronicleEpBundle.message("reference.link.unresolved.loc", "pdx.l:stellaris:missing")
        val text = highlightingScope {
            "[valid](pdx.l:stellaris:title_windea) [invalid](${warning(message)}pdx.l:stellaris:missing${warningEnd()})"
        }
        configureMarkdown(text)
        myFixture.checkHighlighting()
    }

    @Test
    fun testInspection_disabledBySettings() {
        copyFixture("localisation/main_l_english.yml")
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        ChronicleExtensionSettings.getInstance().state.markdown.resolveLinks = false
        myFixture.enableInspections(MarkdownUnresolvedReferenceLinkInspection::class.java)
        configureMarkdown("[invalid](pdx.l:stellaris:missing)")
        myFixture.checkHighlighting()
    }
}
