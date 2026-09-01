package icu.windea.pls.cwt.psi

import com.intellij.psi.PsiComment
import com.intellij.psi.util.parentOfType
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.commentText
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertNotEquals

/**
 * @see CwtPsiService
 */
@RunWith(JUnit4::class)
@TestDataPath("/testData")
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
            assertTrue(r[0] is CwtDocComment)
            assertEquals("attached doc", r[0].commentText)
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
            assertEquals("second doc", r[0].commentText)
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

    // region findStartElementToExtract + findEndElementToExtract

    @Test
    fun findElementsToExtract_forBlock() {
        // 空块（紧邻的花括号）
        run {
            myFixture.configureByText("test.cwt", """
                trigger_name = <caret>{}
            """.trimIndent())
            val element = myFixture.findElementAtCaret()?.parentOfType<CwtBlock>()!!
            val start = CwtPsiService.findStartElementToExtract(element)
            val end = CwtPsiService.findEndElementToExtract(element)
            assertNull(start)
            assertNull(end)
        }

        // 空块
        run {
            myFixture.configureByText("test.cwt", """
                trigger_name = <caret>{
                }
            """.trimIndent())
            val element = myFixture.findElementAtCaret()?.parentOfType<CwtBlock>()!!
            val start = CwtPsiService.findStartElementToExtract(element)
            val end = CwtPsiService.findEndElementToExtract(element)
            assertNull(start)
            assertNull(end)
        }

        // 非空块：返回边界之间的首末非空白元素
        run {
            myFixture.configureByText("test.cwt", """
                trigger_name = <caret>{
                    key1 = value1
                    key2 = value2
                }
            """.trimIndent())
            val element = myFixture.findElementAtCaret()?.parentOfType<CwtBlock>()!!
            val start = CwtPsiService.findStartElementToExtract(element)
            val end = CwtPsiService.findEndElementToExtract(element)
            assertNotNull(start)
            assertNotNull(end)
        }

        // 单元素块
        run {
            myFixture.configureByText("test.cwt", """
                trigger_name = <caret>{
                    only_key = value
                }
            """.trimIndent())
            val element = myFixture.findElementAtCaret()?.parentOfType<CwtBlock>()!!
            val start = CwtPsiService.findStartElementToExtract(element)
            val end = CwtPsiService.findEndElementToExtract(element)
            assertNotNull(start)
            assertNotNull(end)
            assertEquals(start, end)
        }

        // 存在首尾空行
        run {
            myFixture.configureByText("test.cwt", """
                trigger_name = <caret>{

                    only_key = value


                }
            """.trimIndent())
            val element = myFixture.findElementAtCaret()?.parentOfType<CwtBlock>()!!
            val start = CwtPsiService.findStartElementToExtract(element)
            val end = CwtPsiService.findEndElementToExtract(element)
            assertNotNull(start)
            assertNotNull(end)
            assertEquals(start, end)
        }

        // 存在首尾注释
        run {
            myFixture.configureByText("test.cwt", """
                trigger_name = <caret>{
                    # comment1
                    only_key = value
                    # comment2
                }
            """.trimIndent())
            val element = myFixture.findElementAtCaret()?.parentOfType<CwtBlock>()!!
            val start = CwtPsiService.findStartElementToExtract(element)
            val end = CwtPsiService.findEndElementToExtract(element)
            assertNotNull(start)
            assertNotNull(end)
            assertNotEquals(start, end)
            assertTrue(start is PsiComment && start.commentText == "comment1")
            assertTrue(end is PsiComment && end.commentText == "comment2")
        }
    }

    // endregion
}
