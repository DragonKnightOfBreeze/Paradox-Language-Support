package icu.windea.pls.cwt.editor

import com.intellij.codeInsight.generation.actions.CommentByLineCommentAction
import com.intellij.psi.PsiComment
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.cwt.psi.CwtElementTypes
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtCommenterTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Test
    fun testCommentPrefixes() {
        val commenter = CwtCommenter()
        assertEquals(CwtCommenter.LINE_COMMENT_PREFIX, commenter.lineCommentPrefix)
        assertEquals(CwtCommenter.DOC_COMMENT_PREFIX, commenter.documentationCommentPrefix)
        assertEquals(CwtCommenter.DOC_COMMENT_PREFIX, commenter.documentationCommentLinePrefix)
        assertNull(commenter.blockCommentPrefix)
        assertNull(commenter.blockCommentSuffix)
        assertNull(commenter.commentedBlockCommentPrefix)
        assertNull(commenter.commentedBlockCommentSuffix)
        // Companion constants
        assertEquals("#", CwtCommenter.LINE_COMMENT_PREFIX)
        assertEquals("##", CwtCommenter.OPTION_COMMENT_PREFIX)
        assertEquals("###", CwtCommenter.DOC_COMMENT_PREFIX)
    }

    @Test
    fun testCommentTokenTypesAndDocDetection() {
        val commenter = CwtCommenter()
        assertEquals(CwtElementTypes.COMMENT, commenter.lineCommentTokenType)
        assertEquals(CwtElementTypes.DOC_COMMENT, commenter.documentationCommentTokenType)
        assertNull(commenter.blockCommentTokenType)

        myFixture.configureByText(
            "commenter_tokens.test.cwt",
            """
            # comment
            ## option=1
            ### doc comment
            """.trimIndent()
        )
        val comments = PsiTreeUtil.collectElementsOfType(myFixture.file, PsiComment::class.java).toList()
        assertEquals(3, comments.size)
        val (line, option, doc) = comments
        assertFalse(commenter.isDocumentationComment(line))
        assertFalse(commenter.isDocumentationComment(option))
        assertTrue(commenter.isDocumentationComment(doc))

        // documentation comment token text check
        val docFirstChild = doc.firstChild
        assertNotNull(docFirstChild)
        assertEquals(true, commenter.isDocumentationCommentText(docFirstChild!!))
    }

    @Test
    fun testCommenter() {
        myFixture.configureByText("commenter.test.cwt", "<caret>k = v")
        val commentAction = CommentByLineCommentAction()
        commentAction.actionPerformedImpl(project, myFixture.editor)
        myFixture.checkResult("# k = v")
        commentAction.actionPerformedImpl(project, myFixture.editor)
        myFixture.checkResult("k = v")
    }
}
