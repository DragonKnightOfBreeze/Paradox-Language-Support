package icu.windea.pls.test.issues

import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfType
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.util.ParadoxLocalisationArgumentManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameterArgument
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import icu.windea.pls.test.dsl.expectScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * See: [#451](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/451)
 *
 * Victoria 3 introduced the quote escape modifier `|q` for localisation parameters used inside
 * localisation commands (e.g. `Concept` data functions). The lexer greedily consumed the parameter
 * start marker as command text, so the whole expression failed to parse and broke syntax highlighting.
 *
 * Cause: `CommandTextChar` / `CommandTextBoundChar` did not exclude `$`, so `{CommandTextToken}` won
 * the longest-match race against the parameter start rule. The fallback token was also always
 * `TEXT_TOKEN`, which could make recovery harder inside command states.
 *
 * see:
 * - `src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.flex`
 * - `src/main/kotlin/icu/windea/pls/localisation/ParadoxLocalisation.Text.flex`
 *
 * @see icu.windea.pls.lang.util.ParadoxLocalisationArgumentManager
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue451Test : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/451")
        initConfigGroups(project, ParadoxGameType.Vic3)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region reference resolution

    @Test
    fun testReferenceResolution_withQuoteEscape() {
        configureLocalisationText(
            $$"""
            l_english:
             target_loc_key:0 "Target"
             test:0 "[Concept('concept_key', '$<caret>target_loc_key|q$')]"
            """.trimIndent()
        )

        expectScope {
            val resolved = myFixture.findReferenceAtCaret().expectNotNull().resolve()
            resolved.expectIs<ParadoxLocalisationProperty>().name.expectEquals("target_loc_key")
        }
    }

    @Test
    fun testReferenceResolution_withoutQuoteEscape() {
        configureLocalisationText(
            $$"""
            l_english:
             target_loc_key:0 "Target"
             test:0 "[Concept('concept_key', '$<caret>target_loc_key$')]"
            """.trimIndent()
        )

        expectScope {
            val resolved = myFixture.findReferenceAtCaret().expectNotNull().resolve()
            resolved.expectIs<ParadoxLocalisationProperty>().name.expectEquals("target_loc_key")
        }
    }

    @Test
    fun testReferenceResolution_withRegularArgument() {
        configureLocalisationText(
            $$"""
            l_english:
             target_loc_key:0 "Target"
             test:0 "[Concept('concept_key', '$<caret>target_loc_key|v$')]"
            """.trimIndent()
        )

        expectScope {
            val resolved = myFixture.findReferenceAtCaret().expectNotNull().resolve()
            resolved.expectIs<ParadoxLocalisationProperty>().name.expectEquals("target_loc_key")
        }
    }

    // endregion

    // region quick doc

    @Test
    fun testQuickDoc_quoteEscapeInVic3() {
        configureLocalisationText(
            $$"""
            l_english:
             test:0 "[Concept('concept_key', '$target_loc_key|<caret>q$')]"
            """.trimIndent(),
            ParadoxGameType.Vic3
        )

        val argument = myFixture.findElementAtCaret()?.parentOfType<ParadoxLocalisationParameterArgument>()
        assertNotNull("Expected a localisation parameter argument at caret", argument)
        val info = ParadoxLocalisationArgumentManager.getInfo(argument!!)
        assertTrue(info, info.contains("Quote escape") || info.contains("引号转义"))
    }

    @Test
    fun testQuickDoc_quoteEscapeNotInStellaris() {
        configureLocalisationText(
            $$"""
            l_english:
             test:0 "[Concept('concept_key', '$target_loc_key|<caret>q$')]"
            """.trimIndent(),
            ParadoxGameType.Stellaris
        )

        val argument = myFixture.findElementAtCaret()?.parentOfType<ParadoxLocalisationParameterArgument>()
        assertNotNull("Expected a localisation parameter argument at caret", argument)
        val info = ParadoxLocalisationArgumentManager.getInfo(argument!!)
        assertFalse(info, info.contains("Quote escape"))
        assertFalse(info, info.contains("引号转义"))
    }

    // endregion

    // region syntax tree

    @Test
    fun testPsiStructure_withAndWithoutQuoteEscape() {
        markFileInfo(ParadoxGameType.Vic3, "localisation/00_test_l_english.yml")
        myFixture.configureByFile("issues/451/localisation/00_test_l_english.yml")
        IndexingTestUtil.waitUntilIndexesAreReady(project)

        val file = myFixture.file
        val parameters = PsiTreeUtil.findChildrenOfType(file, ParadoxLocalisationParameter::class.java)
        assertEquals(2, parameters.size)
        assertEquals(setOf("target_loc_key"), parameters.map { it.name }.toSet())
        assertNull(PsiTreeUtil.findChildOfType(file, PsiErrorElement::class.java))
    }

    // endregion

    // region fault tolerance

    @Test
    fun testFaultTolerance_isolatedDollar() {
        configureLocalisationText(
            $$"""
            l_english:
             test_1:0 "[Concept('key', '$')]"
             test_2:0 "[a $ b]"
            """.trimIndent()
        )

        val file = myFixture.file
        assertNull(PsiTreeUtil.findChildOfType(file, PsiErrorElement::class.java))
        assertNull(PsiTreeUtil.findChildOfType(file, ParadoxLocalisationParameter::class.java))
    }

    // endregion

    private fun configureLocalisationText(text: String, gameType: ParadoxGameType = ParadoxGameType.Vic3) {
        markFileInfo(gameType, "localisation/00_test_l_english.yml")
        myFixture.configureByText("00_test_l_english.yml", text)
        IndexingTestUtil.waitUntilIndexesAreReady(project)
    }
}
