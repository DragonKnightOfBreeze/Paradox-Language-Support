package icu.windea.pls.lang.psi

import com.intellij.psi.PsiComment
import com.intellij.psi.util.parentOfType
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.psi.PsiService
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyValue
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertNotEquals

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
            assertEquals("first", r[0].text.trimStart('#').trim())
            assertEquals("second", r[1].text.trimStart('#').trim())
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

    // region getArgumentTupleList

    @Test
    fun getArgumentTupleList_basic() {
        run {
            myFixture.configureByText("test.txt", """
                some_scripted_trigger = {
                    PARAM_1 = foo
                    PARAM_2 = <caret>123
                    PARAM_3 = 123.456
                }
            """.trimIndent())
            val block = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptBlock>()!!
            val expected = listOf("PARAM_1" to "foo", "PARAM_2" to "123", "PARAM_3" to "123.456")
            val args = ParadoxPsiService.getArgumentTupleList(block)
            assertEquals(expected, args)
        }

        run {
            myFixture.configureByText("test.txt", """
                some_scripted_effect = {
                    VAR = @var
                    PARAM = ${"$"}PARAM$
                    NUM = @[ 1 + <caret>1 ]
                }
            """.trimIndent())
            val block = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptBlock>()!!
            val expected = listOf("VAR" to "@var", "PARAM" to "\$PARAM$", "NUM" to "@[ 1 + 1 ]")
            val args = ParadoxPsiService.getArgumentTupleList(block)
            assertEquals(expected, args)
        }

        run {
            // Keep quotes of argument values
            myFixture.configureByText("test.txt", """
                inline_script = {
                    script = test/script
                    P1 = ${"$"}PARAM$
                    P2 = "${"$"}OTHER_PARAM$"
                    P3 = bar
                    P4 = <caret>yes
                }
            """.trimIndent())
            val block = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptBlock>()!!
            val expected = listOf("P1" to "\$PARAM$", "P2" to "\"\$OTHER_PARAM$\"", "P3" to "bar", "P4" to "yes")
            val args = ParadoxPsiService.getArgumentTupleList(block, "script")
            assertEquals(expected, args)
        }

        run {
            // Accept only valid identifier characters (leading numbers are allowed)
            myFixture.configureByText("test.txt", """
                inline_script = {
                    script = test/other_script
                    NOT.VALID = v
                    "INVALID PARAM" = v
                    SKIP-IT = v
                    VALID_IDENTIFIER = <caret>v
                    00_INVALID_IDENTIFIER = v
                }
            """.trimIndent())
            val block = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptBlock>()!!
            val expected = listOf("VALID_IDENTIFIER" to "v", "00_INVALID_IDENTIFIER" to "v")
            val args = ParadoxPsiService.getArgumentTupleList(block, "script")
            assertEquals(expected, args)
        }
    }

    // endregion

    // region findMemberElementsToInline

    @Test
    fun findMemberElementsToInline_basic() {
        // 空块（紧邻的花括号）
        run {
            myFixture.configureByText("test.txt", """
                trigger_name = <caret>{}
            """.trimIndent())
            val block = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptBlock>()!!
            val (first, last) = ParadoxPsiService.findMemberElementsToInline(block)
            assertNull(first)
            assertNull(last)
        }

        // 空块
        run {
            myFixture.configureByText("test.txt", """
                trigger_name = <caret>{
                }
            """.trimIndent())
            val block = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptBlock>()!!
            val (first, last) = ParadoxPsiService.findMemberElementsToInline(block)
            assertNull(first)
            assertNull(last)
        }

        // 非空块：返回边界之间的首末非空白元素
        run {
            myFixture.configureByText("test.txt", """
                trigger_name = <caret>{
                    key1 = value1
                    key2 = value2
                }
            """.trimIndent())
            val block = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptBlock>()!!
            val (first, last) = ParadoxPsiService.findMemberElementsToInline(block)
            assertNotNull(first)
            assertNotNull(last)
        }

        // 单元素块
        run {
            myFixture.configureByText("test.txt", """
                trigger_name = <caret>{
                    only_key = value
                }
            """.trimIndent())
            val block = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptBlock>()!!
            val (first, last) = ParadoxPsiService.findMemberElementsToInline(block)
            assertNotNull(first)
            assertNotNull(last)
            assertEquals(first, last)
        }

        // 存在首尾空行
        run {
            myFixture.configureByText("test.txt", """
                trigger_name = <caret>{

                    only_key = value


                }
            """.trimIndent())
            val block = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptBlock>()!!
            val (first, last) = ParadoxPsiService.findMemberElementsToInline(block)
            assertNotNull(first)
            assertNotNull(last)
            assertEquals(first, last)
        }

        // 存在首尾注释
        run {
            myFixture.configureByText("test.txt", """
                trigger_name = <caret>{
                    # comment1
                    only_key = value
                    # comment2
                }
            """.trimIndent())
            val block = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptBlock>()!!
            val (first, last) = ParadoxPsiService.findMemberElementsToInline(block)
            assertNotNull(first)
            assertNotNull(last)
            assertNotEquals(first, last)
            assertTrue(first is PsiComment && first.commentText == "comment1")
            assertTrue(last is PsiComment && last.commentText == "comment2")
        }
    }

    // endregion

    // region findRichTextElementsToInline

    @Test
    fun findRichTextElementsToInline_basic() {
        // 空文本
        run {
            myFixture.configureByText("test.yml", """
                l_english:
                 my_key:0 <caret>""
            """.trimIndent())
            val value = myFixture.findElementAtCaret()?.parentOfType<ParadoxLocalisationPropertyValue>()!!
            val (first, last) = ParadoxPsiService.findRichTextElementsToInline(value)
            assertNull(first)
            assertNull(last)
        }

        // 简单文本：tokenElement 的子节点作为首末元素
        run {
            myFixture.configureByText("test.yml", """
                l_english:
                 my_key:0 <caret>"Hello World"
            """.trimIndent())
            val value = myFixture.findElementAtCaret()?.parentOfType<ParadoxLocalisationPropertyValue>()!!
            val (first, last) = ParadoxPsiService.findRichTextElementsToInline(value)
            assertNotNull(first)
            assertNotNull(last)
            assertEquals(first, last)
        }

        // 复杂文本
        run {
            myFixture.configureByText("test.yml", """
                l_english:
                 my_key:0 <caret>"Hello [Root.GetName]"
            """.trimIndent())
            val value = myFixture.findElementAtCaret()?.parentOfType<ParadoxLocalisationPropertyValue>()!!
            val (first, last) = ParadoxPsiService.findRichTextElementsToInline(value)
            assertNotNull(first)
            assertNotNull(last)
            assertNotEquals(first, last)
        }
    }

    // endregion

    private val PsiComment.commentText get() = text.trimStart('#').trim()
}
