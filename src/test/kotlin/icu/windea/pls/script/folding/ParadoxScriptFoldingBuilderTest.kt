package icu.windea.pls.script.folding

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsFacade

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptFoldingBuilderTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testCommentFolding_expand_true_whenNotCollapsedByDefault() {
        val folding = PlsFacade.getSettings().folding
        folding.comment = true
        folding.commentByDefault = false
        myFixture.testFoldingWithCollapseStatus("$testDataPath/script/folding/folding_comment_expand_true.test.txt")
    }

    fun testCommentFolding_expand_false_whenCollapsedByDefault() {
        val folding = PlsFacade.getSettings().folding
        folding.comment = true
        folding.commentByDefault = true
        myFixture.testFoldingWithCollapseStatus("$testDataPath/script/folding/folding_comment_expand_false.test.txt")
    }

    fun testBlockFolding_expand_true_byDefault() {
        val folding = PlsFacade.getSettings().folding
        folding.comment = false
        folding.commentByDefault = false
        myFixture.testFoldingWithCollapseStatus("$testDataPath/script/folding/folding_block_expand_true.test.txt")
    }
}
