package icu.windea.pls.test.issues

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.testFramework.IndexingTestUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.inspections.script.expression.UnresolvedExpressionInspection
import icu.windea.pls.lang.resolve.complexExpression.ParadoxTemplateExpression
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * See: [#430](https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/430)
 *
 * @see UnresolvedExpressionInspection
 * @see ParadoxTemplateExpression
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
            "test.txt", """
            test_journal_entry = {
                is_shown_when_inactive = {
                    any_country_in_<caret> = {
                    }
                }
            }
        """.trimIndent()
        )

        IndexingTestUtil.waitUntilIndexesAreReady(project)
        myFixture.complete(CompletionType.BASIC)
        assertContainsElements(myFixture.lookupElementStrings!!, "greek_heartlands", "anatolia")
    }
}
