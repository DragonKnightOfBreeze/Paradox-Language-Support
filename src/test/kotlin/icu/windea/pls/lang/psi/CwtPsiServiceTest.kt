package icu.windea.pls.lang.psi

import com.intellij.psi.PsiComment
import com.intellij.psi.util.parentOfType
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see CwtPsiService
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class CwtPsiServiceTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    // region getOwnedDocComments

    @Test
    fun getOwnedDocComments_basic() {
        // 仅文档注释被筛选
        run {
            myFixture.configureByText("test.cwt", """
                # line comment
                ### attached doc
                key = <caret>value
            """.trimIndent())
            val property = myFixture.findElementAtCaret()?.parentOfType<CwtProperty>()!!
            val r = CwtPsiService.getOwnedDocComments(property)
            assertEquals(1, r.size)
            assertTrue(r[0] is icu.windea.pls.cwt.psi.CwtDocComment)
            assertEquals("attached doc", r[0].text.trimStart('#').trim())
        }

        // 无文档注释 → 空
        run {
            myFixture.configureByText("test.cwt", """
                # line comment
                key = <caret>value
            """.trimIndent())
            val property = myFixture.findElementAtCaret()?.parentOfType<CwtProperty>()!!
            val r = CwtPsiService.getOwnedDocComments(property)
            assertEquals(0, r.size)
        }

        // 无附着注释 → 空
        run {
            myFixture.configureByText("test.cwt", """
                key = <caret>value
            """.trimIndent())
            val property = myFixture.findElementAtCaret()?.parentOfType<CwtProperty>()!!
            val r = CwtPsiService.getOwnedDocComments(property)
            assertEquals(0, r.size)
        }

        // 截断逻辑：仅匹配最后一段连续的文档注释
        run {
            myFixture.configureByText("test.cwt", """
                ### first doc
                # line comment
                ### second doc
                key = <caret>value
            """.trimIndent())
            val property = myFixture.findElementAtCaret()?.parentOfType<CwtProperty>()!!
            val r = CwtPsiService.getOwnedDocComments(property)
            assertEquals(1, r.size)
            assertEquals("second doc", r[0].text.trimStart('#').trim())
        }
    }

    // endregion

    // region getDocCommentText

    @Test
    fun getDocCommentText_basic() {
        // 无 Markdown 行 → 标准 HTML 渲染
        run {
            myFixture.configureByText("test.cwt", """
                # <caret>line 1
                # line 2
            """.trimIndent())
            val comment = myFixture.findElementAtCaret()?.parentOfType<PsiComment>(withSelf = true)!!
            val comments = PsiService.findSiblingComments(comment)
            val r = CwtPsiService.getDocCommentText(comments)
            assertEquals("line 1<br> line 2<br>", r)
        }

        // 有 #### 打头的 Markdown 行 → 调用 MarkdownService.toHtml
        run {
            myFixture.configureByText("test.cwt", """
                #### <caret>heading
                # line 2
            """.trimIndent())
            val comment = myFixture.findElementAtCaret()?.parentOfType<PsiComment>(withSelf = true)!!
            val comments = PsiService.findSiblingComments(comment)
            val r = CwtPsiService.getDocCommentText(comments)
            assertNotNull(r)
            assertTrue(r!!.contains("heading") || r.contains("line 2"))
        }

        // 空列表 → null
        run {
            val r = CwtPsiService.getDocCommentText(emptyList())
            assertNull(r)
        }
    }

    // endregion
}
