@file:Suppress("UnstableApiUsage")

package icu.windea.pls.extensions.markdown

import com.intellij.openapi.util.TextRange
import icu.windea.pls.base.settings.ChronicleSettings
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see MarkdownInlineCodeManager
 */
@RunWith(JUnit4::class)
class MarkdownInlineCodeManagerTest : MarkdownTestBase() {
    private val manager = MarkdownInlineCodeManager()

    // region getIdentifierText

    @Test
    fun testGetIdentifierText_plainName() {
        configureMarkdown("`foo`")

        assertEquals("foo", manager.getIdentifierText(findCodeSpan()))
    }

    @Test
    fun testGetIdentifierText_doubleBackticks() {
        configureMarkdown("``foo``")

        assertEquals("foo", manager.getIdentifierText(findCodeSpan()))
    }

    @Test
    fun testGetIdentifierText_notCodeSpan() {
        configureMarkdown("plain text")

        assertNull(manager.getIdentifierText(myFixture.file))
    }

    // endregion

    // region resolveInfo

    @Test
    fun testResolveInfo_plainName() {
        configureMarkdown("`foo`")

        val info = manager.resolveInfo(findCodeSpan())
        assertNotNull(info)
        assertEquals("", info!!.prefix)
        assertEquals("foo", info.name)
        assertEquals(TextRange(1, 4), info.rangeInElement)
    }

    @Test
    fun testResolveInfo_scriptedVariablePrefix() {
        configureMarkdown("`@greeting`")

        val info = manager.resolveInfo(findCodeSpan())
        assertNotNull(info)
        assertEquals("@", info!!.prefix)
        assertEquals("greeting", info.name)
        assertEquals(TextRange(2, 10), info.rangeInElement)
    }

    @Test
    fun testResolveInfo_blankContent() {
        configureMarkdown("` `")

        assertNull(manager.resolveInfo(findCodeSpan()))
    }

    @Test
    fun testResolveInfo_nameWithBlank() {
        configureMarkdown("`foo bar`")

        assertNull(manager.resolveInfo(findCodeSpan()))
    }

    @Test
    fun testResolveInfo_notCodeSpan() {
        configureMarkdown("plain text")

        assertNull(manager.resolveInfo(myFixture.file))
    }

    // endregion

    // region resolveTarget

    @Test
    fun testResolveTarget_scriptedVariable() {
        prepareInlineCodeTargets()
        configureMarkdown("`@greeting`")

        val target = manager.resolveTarget(findCodeSpan(), "@", "greeting")
        assertTrue(target is ParadoxScriptScriptedVariable)
        assertEquals("greeting", (target as ParadoxScriptScriptedVariable).name)
    }

    @Test
    fun testResolveTarget_definition() {
        prepareInlineCodeTargets()
        configureMarkdown("`windea`")

        val target = manager.resolveTarget(findCodeSpan(), "", "windea")
        assertTrue(target is ParadoxDefinitionElement)
        assertEquals("windea", (target as ParadoxDefinitionElement).name)
    }

    @Test
    fun testResolveTarget_localisation() {
        prepareInlineCodeTargets()
        configureMarkdown("`title_windea`")

        val target = manager.resolveTarget(findCodeSpan(), "", "title_windea")
        assertTrue(target is ParadoxLocalisationProperty)
        assertEquals("title_windea", (target as ParadoxLocalisationProperty).name)
    }

    @Test
    fun testResolveTarget_localisation_preferredLocale() {
        prepareInlineCodeTargets()
        ChronicleSettings.getInstance().state.preferredLocale = "l_simp_chinese"
        configureMarkdown("`title_windea`")

        val target = manager.resolveTarget(findCodeSpan(), "", "title_windea")
        assertTrue(target is ParadoxLocalisationProperty)
        assertEquals("main_l_simp_chinese.yml", target!!.containingFile.name)
    }

    @Test
    fun testResolveTarget_invalidScriptedVariableName() {
        prepareInlineCodeTargets()
        configureMarkdown("`@greeting`")

        assertNull(manager.resolveTarget(findCodeSpan(), "@", "foo-bar"))
    }

    @Test
    fun testResolveTarget_invalidLocalisationName() {
        prepareInlineCodeTargets()
        configureMarkdown("`windea`")

        assertNull(manager.resolveTarget(findCodeSpan(), "", "windea!"))
    }

    @Test
    fun testResolveTarget_unsupportedPrefix() {
        prepareInlineCodeTargets()
        configureMarkdown("`windea`")

        assertNull(manager.resolveTarget(findCodeSpan(), "x", "windea"))
    }

    @Test
    fun testResolveTarget_unresolvedName() {
        prepareInlineCodeTargets()
        configureMarkdown("`missing_name`")

        assertNull(manager.resolveTarget(findCodeSpan(), "", "missing_name"))
    }

    // endregion
}
