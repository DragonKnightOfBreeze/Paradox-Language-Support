package icu.windea.pls.csv.folding

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.settings.PlsSettings
import icu.windea.pls.test.markIntegrationTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxCsvFoldingBuilderTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @Test
    fun testCommentFolding_expand_true_whenNotCollapsedByDefault() {
        val folding = PlsSettings.getInstance().state.folding
        folding.comments = true
        folding.commentsByDefault = false
        myFixture.testFoldingWithCollapseStatus("$testDataPath/csv/folding/folding_comment_expand_true.test.csv")
    }

    @Test
    fun testCommentFolding_expand_false_whenCollapsedByDefault() {
        val folding = PlsSettings.getInstance().state.folding
        folding.comments = true
        folding.commentsByDefault = true
        myFixture.testFoldingWithCollapseStatus("$testDataPath/csv/folding/folding_comment_expand_false.test.csv")
    }
}
