package icu.windea.pls.script.editor

import com.intellij.codeInsight.generation.actions.CommentByLineCommentAction
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.script.psi.ParadoxScriptElementTypes

@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptCommenterTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    fun testCommenterBasics() {
        val commenter = ParadoxScriptCommenter()
        // prefixes
        assertEquals("#", commenter.lineCommentPrefix)
        assertNull(commenter.blockCommentPrefix)
        assertNull(commenter.blockCommentSuffix)
        assertNull(commenter.commentedBlockCommentPrefix)
        assertNull(commenter.commentedBlockCommentSuffix)
        // doc comment parts are not supported in Script
        assertNull(commenter.documentationCommentPrefix)
        assertNull(commenter.documentationCommentLinePrefix)
        assertNull(commenter.documentationCommentSuffix)
        // token types
        assertEquals(ParadoxScriptElementTypes.COMMENT, commenter.lineCommentTokenType)
        assertNull(commenter.blockCommentTokenType)
        assertNull(commenter.documentationCommentTokenType)
        // doc detection
        assertFalse(commenter.isDocumentationComment(null))
    }

    fun testCommenter() {
        myFixture.configureByText("commenter.test.txt", "<caret>k = v")
        val commentAction = CommentByLineCommentAction()
        commentAction.actionPerformedImpl(project, myFixture.editor)
        myFixture.checkResult("# k = v")
        commentAction.actionPerformedImpl(project, myFixture.editor)
        myFixture.checkResult("k = v")
    }
}
