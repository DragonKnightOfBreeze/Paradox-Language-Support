package icu.windea.pls.localisation.psi

import com.intellij.psi.util.parentOfType
import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.test.assertNotEquals

/**
 * @see ParadoxLocalisationPsiService
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class ParadoxLocalisationPsiServiceTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    // region findStartElementToExtract + findEndElementToExtract

    @Test
    fun findElementsToExtract_forPropertyValue() {
        // 空文本
        run {
            myFixture.configureByText("test.yml", """
                l_english:
                 my_key:0 <caret>""
            """.trimIndent())
            val element = myFixture.findElementAtCaret()?.parentOfType<ParadoxLocalisationPropertyValue>()!!
            val start = ParadoxLocalisationPsiService.findStartElementToExtract(element)
            val end = ParadoxLocalisationPsiService.findEndElementToExtract(element)
            assertNull(start)
            assertNull(end)
        }

        // 简单文本：tokenElement 的子节点作为首末元素
        run {
            myFixture.configureByText("test.yml", """
                l_english:
                 my_key:0 <caret>"Hello World"
            """.trimIndent())
            val element = myFixture.findElementAtCaret()?.parentOfType<ParadoxLocalisationPropertyValue>()!!
            val start = ParadoxLocalisationPsiService.findStartElementToExtract(element)
            val end = ParadoxLocalisationPsiService.findEndElementToExtract(element)
            assertNotNull(start)
            assertNotNull(end)
            assertEquals(start, end)
        }

        // 复杂文本
        run {
            myFixture.configureByText("test.yml", """
                l_english:
                 my_key:0 <caret>"Hello [Root.GetName]"
            """.trimIndent())
            val element = myFixture.findElementAtCaret()?.parentOfType<ParadoxLocalisationPropertyValue>()!!
            val start = ParadoxLocalisationPsiService.findStartElementToExtract(element)
            val end = ParadoxLocalisationPsiService.findEndElementToExtract(element)
            assertNotNull(start)
            assertNotNull(end)
            assertNotEquals(start, end)
        }
    }

    // endregion
}
