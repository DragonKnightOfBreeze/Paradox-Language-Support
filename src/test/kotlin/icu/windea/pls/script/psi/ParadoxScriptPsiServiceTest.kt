package icu.windea.pls.script.psi

import com.intellij.psi.PsiComment
import com.intellij.psi.util.parentOfType
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.core.commentText
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertNotEquals

/**
 * @see ParadoxScriptPsiService
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxScriptPsiServiceTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    // region findStartElementToExtract + findEndElementToExtract

    @Test
    fun findElementsToExtract_forBlock() {
        // 空块（紧邻的花括号）
        run {
            myFixture.configureByText("test.txt", """
                trigger_name = <caret>{}
            """.trimIndent())
            val element = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptBlock>()!!
            val start = ParadoxScriptPsiService.findStartElementToExtract(element)
            val end = ParadoxScriptPsiService.findEndElementToExtract(element)
            assertNull(start)
            assertNull(end)
        }

        // 空块
        run {
            myFixture.configureByText("test.txt", """
                trigger_name = <caret>{
                }
            """.trimIndent())
            val element = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptBlock>()!!
            val start = ParadoxScriptPsiService.findStartElementToExtract(element)
            val end = ParadoxScriptPsiService.findEndElementToExtract(element)
            assertNull(start)
            assertNull(end)
        }

        // 非空块：返回边界之间的首末非空白元素
        run {
            myFixture.configureByText("test.txt", """
                trigger_name = <caret>{
                    key1 = value1
                    key2 = value2
                }
            """.trimIndent())
            val element = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptBlock>()!!
            val start = ParadoxScriptPsiService.findStartElementToExtract(element)
            val end = ParadoxScriptPsiService.findEndElementToExtract(element)
            assertNotNull(start)
            assertNotNull(end)
        }

        // 单元素块
        run {
            myFixture.configureByText("test.txt", """
                trigger_name = <caret>{
                    only_key = value
                }
            """.trimIndent())
            val element = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptBlock>()!!
            val start = ParadoxScriptPsiService.findStartElementToExtract(element)
            val end = ParadoxScriptPsiService.findEndElementToExtract(element)
            assertNotNull(start)
            assertNotNull(end)
            assertEquals(start, end)
        }

        // 存在首尾空行
        run {
            myFixture.configureByText("test.txt", """
                trigger_name = <caret>{

                    only_key = value


                }
            """.trimIndent())
            val element = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptBlock>()!!
            val start = ParadoxScriptPsiService.findStartElementToExtract(element)
            val end = ParadoxScriptPsiService.findEndElementToExtract(element)
            assertNotNull(start)
            assertNotNull(end)
            assertEquals(start, end)
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
            val element = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptBlock>()!!
            val start = ParadoxScriptPsiService.findStartElementToExtract(element)
            val end = ParadoxScriptPsiService.findEndElementToExtract(element)
            assertNotNull(start)
            assertNotNull(end)
            assertNotEquals(start, end)
            assertTrue(start is PsiComment && start.commentText == "comment1")
            assertTrue(end is PsiComment && end.commentText == "comment2")
        }
    }

    @Test
    fun findElementsToExtract_forConditionalBlock() {
        // 空块（紧邻的花括号）
        run {
            myFixture.configureByText("test.txt", """
                trigger_name = <caret>[[PARAM]]
            """.trimIndent())
            val element = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptInlineConditionalBlock>()!! // inline form here
            val start = ParadoxScriptPsiService.findStartElementToExtract(element)
            val end = ParadoxScriptPsiService.findEndElementToExtract(element)
            assertNull(start)
            assertNull(end)
        }

        // 空块
        run {
            myFixture.configureByText("test.txt", """
                trigger_name = <caret>[[PARAM]
                ]
            """.trimIndent())
            val element = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptNormalConditionalBlock>()!!
            val start = ParadoxScriptPsiService.findStartElementToExtract(element)
            val end = ParadoxScriptPsiService.findEndElementToExtract(element)
            assertNull(start)
            assertNull(end)
        }

        // 非空块：返回边界之间的首末非空白元素
        run {
            myFixture.configureByText("test.txt", """
                trigger_name = <caret>[[PARAM]
                    key1 = value1
                    key2 = value2
                ]
            """.trimIndent())
            val element = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptNormalConditionalBlock>()!!
            val start = ParadoxScriptPsiService.findStartElementToExtract(element)
            val end = ParadoxScriptPsiService.findEndElementToExtract(element)
            assertNotNull(start)
            assertNotNull(end)
        }

        // 单元素块
        run {
            myFixture.configureByText("test.txt", """
                trigger_name = <caret>[[PARAM]
                    only_key = value
                ]
            """.trimIndent())
            val element = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptNormalConditionalBlock>()!!
            val start = ParadoxScriptPsiService.findStartElementToExtract(element)
            val end = ParadoxScriptPsiService.findEndElementToExtract(element)
            assertNotNull(start)
            assertNotNull(end)
            assertEquals(start, end)
        }

        // 存在首尾空行
        run {
            myFixture.configureByText("test.txt", """
                trigger_name = <caret>[[PARAM]

                    only_key = value


                ]
            """.trimIndent())
            val element = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptNormalConditionalBlock>()!!
            val start = ParadoxScriptPsiService.findStartElementToExtract(element)
            val end = ParadoxScriptPsiService.findEndElementToExtract(element)
            assertNotNull(start)
            assertNotNull(end)
            assertEquals(start, end)
        }

        // 存在首尾注释
        run {
            myFixture.configureByText("test.txt", """
                trigger_name = <caret>[[PARAM]
                    # comment1
                    only_key = value
                    # comment2
                ]
            """.trimIndent())
            val element = myFixture.findElementAtCaret()?.parentOfType<ParadoxScriptNormalConditionalBlock>()!!
            val start = ParadoxScriptPsiService.findStartElementToExtract(element)
            val end = ParadoxScriptPsiService.findEndElementToExtract(element)
            assertNotNull(start)
            assertNotNull(end)
            assertNotEquals(start, end)
            assertTrue(start is PsiComment && start.commentText == "comment1")
            assertTrue(end is PsiComment && end.commentText == "comment2")
        }
    }

    // endregion
}
