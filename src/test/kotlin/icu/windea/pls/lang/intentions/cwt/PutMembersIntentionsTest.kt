package icu.windea.pls.lang.intentions.cwt

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.PlsBundle
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see PutMembersIntentionBase
 * @see PutMembersOnOneLineIntention
 * @see PutMembersOnSeparateLinesIntention
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class PutMembersIntentionsTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @After
    fun clear() = clearIntegrationTest()

    @Test
    fun testPutMembersOnOneLine_basic() {
        val intentionName = PlsBundle.message("intention.putMembersOnOneLine")
        myFixture.configureByText(
            "put_members_on_one_line_basic.test.cwt",
            """
            {
                <caret>a = 1
                b
            }
            """.trimIndent()
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("{ a = 1 b }")
    }

    @Test
    fun testPutMembersOnSeparateLines_basic() {
        val intentionName = PlsBundle.message("intention.putMembersOnSeparateLines")
        myFixture.configureByText(
            "put_members_on_separate_lines_basic.test.cwt",
            "{ <caret>a = 1 b }"
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            {
                a = 1
                b
            }
            """.trimIndent()
        )
    }

    @Test
    fun testPutMembersOnOneLine_notAvailableWhenAlreadyOneLine() {
        val intentionName = PlsBundle.message("intention.putMembersOnOneLine")
        myFixture.configureByText(
            "put_members_on_one_line_not_available.test.cwt",
            "{ <caret>a = 1 b }"
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testPutMembersOnSeparateLines_notAvailableWhenAlreadySeparateLines() {
        val intentionName = PlsBundle.message("intention.putMembersOnSeparateLines")
        myFixture.configureByText(
            "put_members_on_separate_lines_not_available.test.cwt",
            """
            {
                <caret>a = 1
                b
            }
            """.trimIndent()
        )
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testNotAvailableWhenCommentExists() {
        val oneLineName = PlsBundle.message("intention.putMembersOnOneLine")
        val separateLinesName = PlsBundle.message("intention.putMembersOnSeparateLines")
        myFixture.configureByText(
            "put_members_not_available_comment.test.cwt",
            """
            k = {
                <caret>a
                # comment
                b = c
            }
            """.trimIndent()
        )
        val available = myFixture.availableIntentions
        assertTrue(available.none { it.text == oneLineName })
        assertTrue(available.none { it.text == separateLinesName })
    }

    @Test
    fun testNotAvailableWhenOptionCommentExists() {
        val oneLineName = PlsBundle.message("intention.putMembersOnOneLine")
        val separateLinesName = PlsBundle.message("intention.putMembersOnSeparateLines")
        myFixture.configureByText(
            "put_members_not_available_option_comment.test.cwt",
            """
            {
                <caret>a = 1
                ## option = value
                b
            }
            """.trimIndent()
        )
        val available = myFixture.availableIntentions
        assertTrue(available.none { it.text == oneLineName })
        assertTrue(available.none { it.text == separateLinesName })
    }

    @Test
    fun testSingleMemberConversions() {
        val oneLineName = PlsBundle.message("intention.putMembersOnOneLine")
        myFixture.configureByText(
            "put_members_single_member_to_one_line.test.cwt",
            """
            k = {
                <caret>a
            }
            """.trimIndent()
        )
        val toOneLine = myFixture.findSingleIntention(oneLineName)
        myFixture.launchAction(toOneLine)
        myFixture.checkResult("k = { a }")

        val separateLinesName = PlsBundle.message("intention.putMembersOnSeparateLines")
        myFixture.configureByText(
            "put_members_single_member_to_separate_lines.test.cwt",
            "k = { <caret>a }"
        )
        val toSeparateLines = myFixture.findSingleIntention(separateLinesName)
        myFixture.launchAction(toSeparateLines)
        myFixture.checkResult(
            """
            k = {
                a
            }
            """.trimIndent()
        )
    }

    @Test
    fun testPutMembersOnSeparateLines_nestedBlock() {
        val intentionName = PlsBundle.message("intention.putMembersOnSeparateLines")
        myFixture.configureByText(
            "put_members_on_separate_lines_nested_block.test.cwt",
            """
            {
                k = { <caret>a = 1 b }
            }
            """.trimIndent()
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            {
                k = {
                    a = 1
                    b
                }
            }
            """.trimIndent()
        )
    }

    @Test
    fun testPutMembersOnOneLine_nestedBlock() {
        val intentionName = PlsBundle.message("intention.putMembersOnOneLine")
        myFixture.configureByText(
            "put_members_on_one_line_nested_block.test.cwt",
            """
            {
                ## option = value
                k = {
                    <caret>a = 1
                    b
                }
            }
            """.trimIndent()
        )
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult(
            """
            {
                ## option = value
                k = { a = 1 b }
            }
            """.trimIndent()
        )
    }
}
