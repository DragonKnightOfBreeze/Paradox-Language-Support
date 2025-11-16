package icu.windea.pls.cwt.folding

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.settings.PlsSettings
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtFoldingBuilderTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Test
    fun testCommentFolding_expand_true_whenNotCollapsedByDefault() {
        val folding = PlsSettings.getInstance().state.folding
        folding.comments = true
        folding.commentsByDefault = false
        myFixture.testFoldingWithCollapseStatus("$testDataPath/cwt/folding/folding_comment_expand_true.test.cwt")
    }

    @Test
    fun testCommentFolding_expand_false_whenCollapsedByDefault() {
        val folding = PlsSettings.getInstance().state.folding
        folding.comments = true
        folding.commentsByDefault = true
        myFixture.testFoldingWithCollapseStatus("$testDataPath/cwt/folding/folding_comment_expand_false.test.cwt")
    }

    @Test
    fun testBlockFolding_expand_true_byDefault() {
        val folding = PlsSettings.getInstance().state.folding
        // comment folding setting irrelevant for blocks
        folding.comments = false
        folding.commentsByDefault = false
        myFixture.testFoldingWithCollapseStatus("$testDataPath/cwt/folding/folding_block_expand_true.test.cwt")
    }
}
