package icu.windea.pls.csv.folding

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsFacade

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxCsvFoldingBuilderTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testCommentFolding_expand_true_whenNotCollapsedByDefault() {
        val folding = PlsFacade.getSettings().folding
        folding.comment = true
        folding.commentByDefault = false
        myFixture.testFoldingWithCollapseStatus("$testDataPath/csv/folding/folding_comment_expand_true.test.csv")
    }

    fun testCommentFolding_expand_false_whenCollapsedByDefault() {
        val folding = PlsFacade.getSettings().folding
        folding.comment = true
        folding.commentByDefault = true
        myFixture.testFoldingWithCollapseStatus("$testDataPath/csv/folding/folding_comment_expand_false.test.csv")
    }
}
