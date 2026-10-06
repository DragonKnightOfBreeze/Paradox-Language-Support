package icu.windea.pls.lang.psi

import com.intellij.psi.PsiComment
import com.intellij.psi.util.parentOfType
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.commentText
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see ParadoxPsiService
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxPsiServiceTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    // region getOwnedComments

    @Test
    fun getOwnedComments_basic() {
        // 所有附着注释均返回（顺序从前向后）
        run {
            myFixture.configureByText("test.txt", """
                # first
                # second
                some_key = <caret>value
            """.trimIndent())
            val property = myFixture.findElementAtCaret()
                ?.parentOfType<ParadoxScriptProperty>()!!
            val r = ParadoxPsiService.getOwnedComments(property)
            assertEquals(2, r.size)
            assertEquals("first", r[0].commentText)
            assertEquals("second", r[1].commentText)
        }

        // 无附着注释
        run {
            myFixture.configureByText("test.txt", """
                some_key = <caret>value
            """.trimIndent())
            val property = myFixture.findElementAtCaret()
                ?.parentOfType<ParadoxScriptProperty>()!!
            val r = ParadoxPsiService.getOwnedComments(property)
            assertEquals(0, r.size)
        }
    }

    // endregion

    // region getLineCommentText

    @Test
    fun getLineCommentText_basic() {
        // 空列表 → null
        run {
            val r = ParadoxPsiService.getLineCommentText(emptyList())
            assertNull(r)
        }

        // 单条注释
        run {
            myFixture.configureByText("test.txt", """
                # <caret>hello world
            """.trimIndent())
            val comment = myFixture.findElementAtCaret()?.parentOfType<PsiComment>(withSelf = true)!!
            val r = ParadoxPsiService.getLineCommentText(listOf(comment))
            assertEquals("hello world<br>", r)
        }

        // 多条注释用 <br> 分隔
        run {
            myFixture.configureByText("test.txt", """
                # <caret>line 1
                # line 2
            """.trimIndent())
            val comment = myFixture.findElementAtCaret()?.parentOfType<PsiComment>(withSelf = true)!!
            val comments = PsiService.findSiblingComments(comment)
            val r = ParadoxPsiService.getLineCommentText(comments)
            assertEquals("line 1<br> line 2<br>", r)
        }
    }

    // endregion
}
