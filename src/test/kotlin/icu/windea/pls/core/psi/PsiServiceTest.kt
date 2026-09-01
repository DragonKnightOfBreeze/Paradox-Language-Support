package icu.windea.pls.core.psi

import com.intellij.psi.PsiComment
import com.intellij.psi.util.parentOfType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.commentText
import icu.windea.pls.cwt.psi.CwtDocComment
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see PsiService
 */
@RunWith(JUnit4::class)
class PsiServiceTest : BasePlatformTestCase(), ChronicleTestScope {
    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    // region getAttachedComments

    @Test
    fun getAttachedComments_basic() {
        // 无附着注释
        run {
            myFixture.configureByText("test.cwt", """
                key = <caret>value
            """.trimIndent())
            val property = myFixture.findElementAtCaret()?.parentOfType<CwtProperty>()!!
            val r = PsiService.getAttachedComments(property).toList()
            assertEquals(0, r.size)
        }

        // 一行附着注释
        run {
            myFixture.configureByText("test.cwt", """
                # attached comment
                key = <caret>value
            """.trimIndent())
            val property = myFixture.findElementAtCaret()?.parentOfType<CwtProperty>()!!
            val r = PsiService.getAttachedComments(property).toList()
            assertEquals(1, r.size)
            assertEquals("attached comment", r[0].commentText)
        }

        // 多行附着注释（顺序从后到前）
        run {
            myFixture.configureByText("test.cwt", """
                # first attached
                # second attached
                key = <caret>value
            """.trimIndent())
            val property = myFixture.findElementAtCaret()?.parentOfType<CwtProperty>()!!
            val r = PsiService.getAttachedComments(property).toList()
            assertEquals(2, r.size)
            assertEquals("second attached", r[0].commentText)
            assertEquals("first attached", r[1].commentText)
        }

        // 空行隔开的注释不附着
        run {
            myFixture.configureByText("test.cwt", """
                # detached comment

                key = <caret>value
            """.trimIndent())
            val property = myFixture.findElementAtCaret()?.parentOfType<CwtProperty>()!!
            val r = PsiService.getAttachedComments(property).toList()
            assertEquals(0, r.size)
        }

        // 仅空行之后的最后一组注释附着
        run {
            myFixture.configureByText("test.cwt", """
                # first group

                # second attached
                key = <caret>value
            """.trimIndent())
            val property = myFixture.findElementAtCaret()?.parentOfType<CwtProperty>()!!
            val r = PsiService.getAttachedComments(property).toList()
            assertEquals(1, r.size)
            assertEquals("second attached", r[0].commentText)
        }

        // 块内属性的附着注释
        run {
            myFixture.configureByText("test.cwt", """
                root = {
                    # attached comment
                    inner_key = <caret>value
                }
            """.trimIndent())
            val property = myFixture.findElementAtCaret()?.parentOfType<CwtProperty>()!!
            val r = PsiService.getAttachedComments(property).toList()
            assertEquals(1, r.size)
            assertEquals("attached comment", r[0].commentText)
        }
    }

    // endregion

    // region getAttachingElement

    @Test
    fun getAttachingElement_basic() {
        // 注释附着到紧随其后的属性
        run {
            myFixture.configureByText("test.cwt", """
                # <caret>attached comment
                key = value
            """.trimIndent())
            val comment = findCommentAtCaret()!!
            val attaching = PsiService.getAttachingElement(comment)
            assertNotNull(attaching)
            assertTrue(attaching is CwtProperty)
            assertEquals("key", (attaching as CwtProperty).name)
        }

        // 末尾注释无附着元素
        run {
            myFixture.configureByText("test.cwt", """
                key = value
                # <caret>trailing comment
            """.trimIndent())
            val comment = findCommentAtCaret()!!
            assertNull(PsiService.getAttachingElement(comment))
        }

        // 空行隔开的注释无附着
        run {
            myFixture.configureByText("test.cwt", """
                # <caret>detached comment

                key = value
            """.trimIndent())
            val comment = findCommentAtCaret()!!
            assertNull(PsiService.getAttachingElement(comment))
        }

        // 块内注释附着到紧随其后的属性
        run {
            myFixture.configureByText("test.cwt", """
                root = {
                    # <caret>inner comment
                    inner_key = value
                }
            """.trimIndent())
            val comment = findCommentAtCaret()!!
            val attaching = PsiService.getAttachingElement(comment)
            assertNotNull(attaching)
            assertTrue(attaching is CwtProperty)
            assertEquals("inner_key", (attaching as CwtProperty).name)
        }

        // 连续注释中仅最后一条注释有附着
        run {
            myFixture.configureByText("test.cwt", """
                # first
                # <caret>second
                key = value
            """.trimIndent())
            val comment = findCommentAtCaret()!!
            val attaching = PsiService.getAttachingElement(comment)
            assertNotNull(attaching)
            assertEquals("key", (attaching as CwtProperty).name)
        }
    }

    // endregion

    // region findSiblingComments

    @Test
    fun findSiblingComments_basic() {
        // 单条注释
        run {
            myFixture.configureByText("test.cwt", """
                # <caret>comment
            """.trimIndent())
            val comment = findCommentAtCaret()!!
            val r = PsiService.findSiblingComments(comment)
            assertEquals(1, r.size)
            assertEquals("comment", r[0].commentText)
        }

        // 三条连续注释
        run {
            myFixture.configureByText("test.cwt", """
                # comment_a
                # <caret>comment_b
                # comment_c
            """.trimIndent())
            val comment = findCommentAtCaret()!!
            val r = PsiService.findSiblingComments(comment)
            assertEquals(3, r.size)
            assertEquals("comment_a", r[0].commentText)
            assertEquals("comment_b", r[1].commentText)
            assertEquals("comment_c", r[2].commentText)
        }

        // 空行分隔：仅获取包含光标的连续注释组
        run {
            myFixture.configureByText("test.cwt", """
                # comment

                # <caret>comment
                # comment

                # comment
            """.trimIndent())
            val comment = findCommentAtCaret()!!
            val r = PsiService.findSiblingComments(comment)
            assertEquals(2, r.size)
        }

        // 文档注释过滤：仅匹配 CwtDocComment
        run {
            myFixture.configureByText("test.cwt", """
                # comment

                # comment
                ### <caret>doc comment
                ### doc comment
                # comment
                ### comment
            """.trimIndent())
            val comment = findCommentAtCaret()!!
            val r = PsiService.findSiblingComments(comment) { it is CwtDocComment }
            assertEquals(2, r.size)
            assertEquals("doc comment", r[0].commentText)
            assertEquals("doc comment", r[1].commentText)
        }

        // 光标在首条注释：仅向前收集
        run {
            myFixture.configureByText("test.cwt", """
                # <caret>first
                # second
                # third
            """.trimIndent())
            val comment = findCommentAtCaret()!!
            val r = PsiService.findSiblingComments(comment)
            assertEquals(3, r.size)
        }

        // 光标在末尾注释：仅向后收集
        run {
            myFixture.configureByText("test.cwt", """
                # first
                # second
                # <caret>third
            """.trimIndent())
            val comment = findCommentAtCaret()!!
            val r = PsiService.findSiblingComments(comment)
            assertEquals(3, r.size)
        }

        // 默认过滤：包含所有类型注释（行注释 + 文档注释）
        run {
            myFixture.configureByText("test.cwt", """
                # line comment
                ### <caret>doc comment
                # line comment
            """.trimIndent())
            val comment = findCommentAtCaret()!!
            val r = PsiService.findSiblingComments(comment)
            assertEquals(3, r.size)
        }

        // 当前注释自身不匹配过滤条件时返回空列表
        run {
            myFixture.configureByText("test.cwt", """
                # <caret>line comment
                ### doc comment
            """.trimIndent())
            val comment = findCommentAtCaret()!!
            val r = PsiService.findSiblingComments(comment) { it is CwtDocComment }
            assertEquals(0, r.size)
        }
    }

    // endregion

    // region findAllSiblingCommentsIn

    @Test
    fun findAllSiblingCommentsIn_basic() {
        // 无注释 → 空列表
        run {
            myFixture.configureByText("test.cwt", """
                key = <caret>value
            """.trimIndent())
            val property = myFixture.findElementAtCaret()?.parentOfType<CwtProperty>()!!
            val parent = property.parent
            val r = PsiService.findAllSiblingCommentsIn(parent)
            assertEquals(0, r.size)
        }

        // 一组注释
        run {
            myFixture.configureByText("test.cwt", """
                # comment 1
                # comment 2
                key = <caret>value
            """.trimIndent())
            val property = myFixture.findElementAtCaret()?.parentOfType<CwtProperty>()!!
            val parent = property.parent
            val r = PsiService.findAllSiblingCommentsIn(parent)
            assertEquals(1, r.size)
            assertEquals(2, r[0].size)
        }

        // 多组注释（通过属性分隔）
        run {
            myFixture.configureByText("test.cwt", """
                # group A1
                # group A2
                key_a = value

                # group B1
                key_b = <caret>value
            """.trimIndent())
            val property = myFixture.findElementAtCaret()?.parentOfType<CwtProperty>()!!
            val parent = property.parent
            val r = PsiService.findAllSiblingCommentsIn(parent)
            assertEquals(2, r.size)
        }

        // 通过空行分隔的组
        run {
            myFixture.configureByText("test.cwt", """
                # group 1

                # group 2
                # <caret>group 2 continues
            """.trimIndent())
            val comment = findCommentAtCaret()!!
            val parent = comment.parent
            val r = PsiService.findAllSiblingCommentsIn(parent)
            assertEquals(2, r.size)
        }

        // 带过滤条件
        run {
            myFixture.configureByText("test.cwt", """
                # line comment
                ### <caret>doc comment
            """.trimIndent())
            val comment = findCommentAtCaret()!!
            val parent = comment.parent
            val r = PsiService.findAllSiblingCommentsIn(parent) { it is CwtDocComment }
            assertEquals(1, r.size)
            assertTrue(r[0].all { it is CwtDocComment })
        }
    }

    // endregion

    // region getOwnedComments

    @Test
    fun getOwnedComments_basic() {
        // 所有附着注释均匹配 → 全部返回（顺序从前向后）
        run {
            myFixture.configureByText("test.cwt", """
                # first
                # second
                key = <caret>value
            """.trimIndent())
            val property = myFixture.findElementAtCaret()?.parentOfType<CwtProperty>()!!
            val r = PsiService.getOwnedComments(property)
            assertEquals(2, r.size)
            assertEquals("first", r[0].commentText)
            assertEquals("second", r[1].commentText)
        }

        // 截断逻辑：dropWhile 丢弃前导不匹配的，takeWhile 取连续匹配的
        run {
            myFixture.configureByText("test.cwt", """
                # first
                # second
                key = <caret>value
            """.trimIndent())
            val property = myFixture.findElementAtCaret()?.parentOfType<CwtProperty>()!!
            val r = PsiService.getOwnedComments(property) { it.commentText == "second" }
            assertEquals(1, r.size)
            assertEquals("second", r[0].commentText)
        }

        // 无附着注释 → 空
        run {
            myFixture.configureByText("test.cwt", """
                key = <caret>value
            """.trimIndent())
            val property = myFixture.findElementAtCaret()?.parentOfType<CwtProperty>()!!
            val r = PsiService.getOwnedComments(property)
            assertEquals(0, r.size)
        }

        // 无匹配的附着注释 → 空
        run {
            myFixture.configureByText("test.cwt", """
                # comment
                key = <caret>value
            """.trimIndent())
            val property = myFixture.findElementAtCaret()?.parentOfType<CwtProperty>()!!
            val r = PsiService.getOwnedComments(property) { it.commentText == "other" }
            assertEquals(0, r.size)
        }

        // 截断仅作用于最后一段连续的匹配注释
        run {
            myFixture.configureByText("test.cwt", """
                ### first doc
                # line comment
                ### second doc
                key = <caret>value
            """.trimIndent())
            val property = myFixture.findElementAtCaret()?.parentOfType<CwtProperty>()!!
            val r = PsiService.getOwnedComments(property) { it is CwtDocComment }
            assertEquals(1, r.size)
            assertEquals("second doc", r[0].commentText)
        }
    }

    // endregion

    // region getLineCommentText

    @Test
    fun getLineCommentText_basic() {
        // 空列表 → null
        run {
            val r = PsiService.getLineCommentText(emptyList())
            assertNull(r)
        }

        // 单条注释
        run {
            myFixture.configureByText("test.cwt", """
                # <caret>hello world
            """.trimIndent())
            val comment = findCommentAtCaret()!!
            val r = PsiService.getLineCommentText(listOf(comment))
            assertEquals("hello world<br>", r)
        }

        // 多条注释用 <br> 分隔
        run {
            myFixture.configureByText("test.cwt", """
                # <caret>line 1
                # line 2
            """.trimIndent())
            val comment = findCommentAtCaret()!!
            val comments = PsiService.findSiblingComments(comment)
            val r = PsiService.getLineCommentText(comments)
            assertEquals("line 1<br> line 2<br>", r)
        }

        // 斜杠结尾不换行
        run {
            myFixture.configureByText("test.cwt", """
                # <caret>continued/
                # next line
            """.trimIndent())
            val comment = findCommentAtCaret()!!
            val comments = PsiService.findSiblingComments(comment)
            val r = PsiService.getLineCommentText(comments)
            assertEquals("continued next line<br>", r)
        }

        // 逗号结尾不换行
        run {
            myFixture.configureByText("test.cwt", """
                # <caret>line 1，
                # line 2
            """.trimIndent())
            val comment = findCommentAtCaret()!!
            val comments = PsiService.findSiblingComments(comment)
            val r = PsiService.getLineCommentText(comments)
            assertEquals("line 1， line 2<br>", r)
        }
    }

    // endregion

    // region getDocCommentText

    @Test
    fun getDocCommentText_basic() {
        // 空列表 → null
        run {
            val r = PsiService.getDocCommentText(emptyList()) { false }
            assertNull(r)
        }

        // 无 Markdown 行 → 标准 HTML 渲染（与 getLineCommentText 一致）
        run {
            myFixture.configureByText("test.cwt", """
                # <caret>line 1
                # line 2
            """.trimIndent())
            val comment = findCommentAtCaret()!!
            val comments = PsiService.findSiblingComments(comment)
            val r = PsiService.getDocCommentText(comments) { it.startsWith("####") }
            assertEquals("line 1<br> line 2<br>", r)
        }

        // 有 Markdown 行 → 调用 MarkdownService.toHtml
        run {
            myFixture.configureByText("test.cwt", """
                #### <caret>heading
                # content
            """.trimIndent())
            val comment = findCommentAtCaret()!!
            val comments = PsiService.findSiblingComments(comment)
            val r = PsiService.getDocCommentText(comments) { it.startsWith("####") }
            assertNotNull(r)
            assertTrue(r!!.contains("heading"))
        }

        // 单条非 Markdown 行
        run {
            myFixture.configureByText("test.cwt", """
                # <caret>single line
            """.trimIndent())
            val comment = findCommentAtCaret()!!
            val r = PsiService.getDocCommentText(listOf(comment)) { it.startsWith("####") }
            assertEquals("single line<br>", r)
        }
    }

    // endregion

    private fun findCommentAtCaret() = myFixture.findElementAtCaret()?.parentOfType<PsiComment>(withSelf = true)
}
