package icu.windea.pls.localisation.editor

import com.intellij.codeInsight.generation.actions.CommentByLineCommentAction
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes
import icu.windea.pls.test.markIntegrationTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxLocalisationCommenterTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @Test
    fun testCommenterBasics() {
        val commenter = ParadoxLocalisationCommenter()
        // prefixes
        assertEquals("#", commenter.lineCommentPrefix)
        assertNull(commenter.blockCommentPrefix)
        assertNull(commenter.blockCommentSuffix)
        assertNull(commenter.commentedBlockCommentPrefix)
        assertNull(commenter.commentedBlockCommentSuffix)
        // doc comment parts are not supported in Localisation
        assertNull(commenter.documentationCommentPrefix)
        assertNull(commenter.documentationCommentLinePrefix)
        assertNull(commenter.documentationCommentSuffix)
        // token types
        assertEquals(ParadoxLocalisationElementTypes.COMMENT, commenter.lineCommentTokenType)
        assertNull(commenter.blockCommentTokenType)
        assertNull(commenter.documentationCommentTokenType)
        // doc detection
        assertFalse(commenter.isDocumentationComment(null))
    }

    @Test
    fun testCommenter() {
        myFixture.configureByText("commenter.test.yml", """
            l_simp_chinese:
             <caret>KEY: "foo"
        """.trimIndent())
        val commentAction = CommentByLineCommentAction()
        commentAction.actionPerformedImpl(project, myFixture.editor)
        myFixture.checkResult("""
            l_simp_chinese:
             # KEY: "foo"
        """.trimIndent())
        commentAction.actionPerformedImpl(project, myFixture.editor)
        myFixture.checkResult("""
            l_simp_chinese:
             KEY: "foo"
        """.trimIndent())
    }
}
