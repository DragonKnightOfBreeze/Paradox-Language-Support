package icu.windea.pls.csv.editor

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.csv.psi.ParadoxCsvElementTypes

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxCsvCommenterTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testCommenterBasics() {
        val commenter = ParadoxCsvCommenter()
        // prefixes
        assertEquals("#", commenter.lineCommentPrefix)
        assertNull(commenter.blockCommentPrefix)
        assertNull(commenter.blockCommentSuffix)
        assertNull(commenter.commentedBlockCommentPrefix)
        assertNull(commenter.commentedBlockCommentSuffix)
        // doc comment parts are not supported in CSV
        assertNull(commenter.documentationCommentPrefix)
        assertNull(commenter.documentationCommentLinePrefix)
        assertNull(commenter.documentationCommentSuffix)
        // token types
        assertEquals(ParadoxCsvElementTypes.COMMENT, commenter.lineCommentTokenType)
        assertNull(commenter.blockCommentTokenType)
        assertNull(commenter.documentationCommentTokenType)
        // doc detection
        assertFalse(commenter.isDocumentationComment(null))
    }
}
