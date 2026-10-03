package icu.windea.pls.test.issues

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.lang.inspections.script.expression.UnresolvedExpressionInspection
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import icu.windea.pls.test.dsl.expectScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * See: [#430](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/430)
 *
 * Geographic region short keys (e.g., `greek_heartlands`) and their `any_*_in_<geographic_region>`
 * iterators were broken: the short keys were no longer resolved, and the iterators were neither
 * recognized nor completed.
 *
 * Cause:
 * - When resolving `ParadoxTemplateExpression` in `incomplete` mode, parsing was aborted directly when
 *   the matched reference fragment text was empty (the definition name is not typed yet). An empty
 *   fragment should not abort parsing in `incomplete` mode.
 * - When collecting the configs applicable to code completion, the parent config was not inlined before
 *   reading its child configs, so the inlined child configs were missing.
 *
 * @see icu.windea.pls.lang.inspections.script.expression.UnresolvedExpressionInspection
 * @see icu.windea.pls.lang.resolve.complexExpression.ParadoxTemplateExpression
 * @see icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
 * @see icu.windea.pls.config.manipulation.CwtConfigInlineService.inlineForConfig
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class Issue430Test : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() {
        markIntegrationTest()
        markRootDirectory("issues/430")
        initConfigGroups(project, ParadoxGameType.Vic3)
        myFixture.enableInspections(UnresolvedExpressionInspection::class.java)
    }

    @After
    fun doTearDown() = clearIntegrationTest()

    // region basic cases

    @Test
    fun testInspection_GeographicRegionShortKeyAndIterator() {
        markFileInfo(ParadoxGameType.Vic3, "common/geographic_regions/test.txt")
        myFixture.configureByFile("issues/430/common/geographic_regions/test.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/journal_entries/test.txt")
        myFixture.configureByFile("issues/430/common/journal_entries/test.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting()
    }

    @Test
    fun testCompletion_GeographicRegionIterator() {
        markFileInfo(ParadoxGameType.Vic3, "common/geographic_regions/test.txt")
        myFixture.configureByFile("issues/430/common/geographic_regions/test.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/journal_entries/test.txt")
        myFixture.configureByText(
            "test.txt",
            """
            test_journal_entry = {
                is_shown_when_inactive = {
                    any_country_in_<caret> = {}
                }
            }
            """.trimIndent()
        )

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.complete(CompletionType.BASIC)
        assertContainsElements(myFixture.lookupElementStrings!!, "greek_heartlands", "anatolia")
    }

    // endregion

    // region extended cases

    @Test
    fun testConfig_ForShortKey() {
        val configGroup = ChronicleFacade.getConfigGroup(project, ParadoxGameType.Vic3)
        val typeName = "geographic_region_short_key"
        val typeConfig = configGroup.types[typeName]
        expectScope {
            typeConfig.expectNotNull()
            typeConfig.skipRootKey.expectEquals(listOf(listOf("any")))
            typeConfig.nameField.expectEquals("-")
        }
        val declarationConfig = configGroup.declarations[typeName]
        expectScope {
            declarationConfig.expectNotNull() // should be explicitly declared
            declarationConfig.config.value.expectEquals("scalar") // should be `scalar` (not `<geographic_region_short_key>`)
        }
    }

    @Test
    fun testInspection_ForShortKey() {
        markFileInfo(ParadoxGameType.Vic3, "common/geographic_regions/test.txt")
        myFixture.configureByText(
            "test.txt",
            """
            geographic_region_city_serenity = {
                short_key = "city_serenity"
            }
            """.trimIndent()
        )

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.checkHighlighting() // should be no problems since `geographic_region_short_key = scalar` is explicitly added
    }

    @Test
    fun testCompletion_ForTemplateEmpty() {
        markFileInfo(ParadoxGameType.Vic3, "common/geographic_regions/test.txt")
        myFixture.configureByFile("issues/430/common/geographic_regions/test_extended.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/journal_entries/test.txt")
        myFixture.configureByText(
            "test.txt",
            """
            test_journal_entry = {
                is_shown_when_inactive = {
                    <caret> = {}
                }
            }
            """.trimIndent()
        )

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.complete(CompletionType.BASIC)
        assertContainsElements(myFixture.lookupElementStrings!!, "any_country_in_")
    }

    @Test
    fun testCompletion_ForTemplateWithPrefix() {
        markFileInfo(ParadoxGameType.Vic3, "common/geographic_regions/test.txt")
        myFixture.configureByFile("issues/430/common/geographic_regions/test_extended.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/journal_entries/test.txt")
        myFixture.configureByText(
            "test.txt",
            """
            test_journal_entry = {
                is_shown_when_inactive = {
                    any_<caret> = {}
                }
            }
            """.trimIndent()
        )

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.complete(CompletionType.BASIC)
        assertContainsElements(myFixture.lookupElementStrings!!, "any_country_in_")
    }

    @Test
    fun testCompletion_TemplateWithLongerPrefix() {
        markFileInfo(ParadoxGameType.Vic3, "common/geographic_regions/test.txt")
        myFixture.configureByFile("issues/430/common/geographic_regions/test_extended.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/journal_entries/test.txt")
        myFixture.configureByText(
            "test.txt",
            """
            test_journal_entry = {
                is_shown_when_inactive = {
                    any_country_in<caret> = {}
                }
            }
            """.trimIndent()
        )

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.complete(CompletionType.BASIC)
        assertContainsElements(myFixture.lookupElementStrings!!, "any_country_in_")
    }

    @Test
    fun testCompletion_ForSnippetEmpty() {
        markFileInfo(ParadoxGameType.Vic3, "common/geographic_regions/test.txt")
        myFixture.configureByFile("issues/430/common/geographic_regions/test_extended.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/journal_entries/test.txt")
        myFixture.configureByText(
            "test.txt",
            """
            test_journal_entry = {
                is_shown_when_inactive = {
                    any_country_in_<caret> = {}
                }
            }
            """.trimIndent()
        )

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.complete(CompletionType.BASIC)
        val lookupElementStrings = myFixture.lookupElementStrings!!
        assertSize(3, lookupElementStrings)
        assertContainsElements(lookupElementStrings, "city_silence", "city_silent", "city_serenity")
    }

    @Test
    fun testCompletion_ForSnippetWithPrefix() {
        markFileInfo(ParadoxGameType.Vic3, "common/geographic_regions/test.txt")
        myFixture.configureByFile("issues/430/common/geographic_regions/test_extended.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/journal_entries/test.txt")
        myFixture.configureByText(
            "test.txt",
            """
            test_journal_entry = {
                is_shown_when_inactive = {
                    any_country_in_city_<caret> = {}
                }
            }
            """.trimIndent()
        )

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.complete(CompletionType.BASIC)
        val lookupElementStrings = myFixture.lookupElementStrings!!
        assertSize(3, lookupElementStrings)
        assertContainsElements(lookupElementStrings, "city_silence", "city_silent", "city_serenity")
    }

    @Test
    fun testCompletion_ForSnippetWithLongerPrefix() {
        markFileInfo(ParadoxGameType.Vic3, "common/geographic_regions/test.txt")
        myFixture.configureByFile("issues/430/common/geographic_regions/test_extended.txt")

        IndexingTestUtil.waitUntilIndexesAreReady(project)

        markFileInfo(ParadoxGameType.Vic3, "common/journal_entries/test.txt")
        myFixture.configureByText(
            "test.txt",
            """
            test_journal_entry = {
                is_shown_when_inactive = {
                    any_country_in_city_si<caret> = {}
                }
            }
            """.trimIndent()
        )

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.complete(CompletionType.BASIC)
        val lookupElementStrings = myFixture.lookupElementStrings!!
        assertSize(2, lookupElementStrings)
        assertContainsElements(lookupElementStrings, "city_silence", "city_silent")
    }

    // endregion
}
