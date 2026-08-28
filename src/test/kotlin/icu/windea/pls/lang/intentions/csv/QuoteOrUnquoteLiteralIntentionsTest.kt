package icu.windea.pls.lang.intentions.csv

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.lang.intentions.ChronicleIntentionBundle
import icu.windea.pls.test.ChronicleTestScope
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see QuoteLiteralIntention
 * @see UnquoteLiteralIntention
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class QuoteOrUnquoteLiteralIntentionsTest : BasePlatformTestCase(), ChronicleTestScope {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testQuoteLiteral_basic() {
        val intentionName = ChronicleIntentionBundle.message("intention.quoteLiteral")
        myFixture.configureByText("test.csv", "name;age\nalice;<caret>18")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("name;age\nalice;\"18\"")
    }

    @Test
    fun testQuoteLiteral_columnContainsBlank() {
        val intentionName = ChronicleIntentionBundle.message("intention.quoteLiteral")
        myFixture.configureByText("test.csv", "name;desc\nalice;<caret>a b")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("name;desc\nalice;\"a b\"")
    }

    @Test
    fun testQuoteLiteral_onlyLeftQuoted() {
        val intentionName = ChronicleIntentionBundle.message("intention.quoteLiteral")
        myFixture.configureByText("test.csv", "name;age\nalice;<caret>\"18")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("name;age\nalice;\"18\"")
    }

    @Test
    fun testQuoteLiteral_onlyRightQuoted() {
        val intentionName = ChronicleIntentionBundle.message("intention.quoteLiteral")
        myFixture.configureByText("test.csv", "name;age\nalice;<caret>18\"")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("name;age\nalice;\"18\"")
    }

    @Test
    fun testQuoteLiteral_notAvailableWhenAlreadyQuoted() {
        val intentionName = ChronicleIntentionBundle.message("intention.quoteLiteral")
        myFixture.configureByText("test.csv", "name;age\nalice;<caret>\"18\"")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testUnquoteLiteral_basic() {
        val intentionName = ChronicleIntentionBundle.message("intention.unquoteLiteral")
        myFixture.configureByText("test.csv", "name;age\nalice;<caret>\"18\"")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("name;age\nalice;18")
    }

    @Test
    fun testUnquoteLiteral_onlyLeftQuoted() {
        val intentionName = ChronicleIntentionBundle.message("intention.unquoteLiteral")
        myFixture.configureByText("test.csv", "name;age\nalice;<caret>\"18")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("name;age\nalice;18")
    }

    @Test
    fun testUnquoteLiteral_onlyRightQuoted() {
        val intentionName = ChronicleIntentionBundle.message("intention.unquoteLiteral")
        myFixture.configureByText("test.csv", "name;age\nalice;<caret>18\"")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("name;age\nalice;18")
    }

    @Test
    fun testUnquoteLiteral_notAvailableWhenUnquoted() {
        val intentionName = ChronicleIntentionBundle.message("intention.unquoteLiteral")
        myFixture.configureByText("test.csv", "name;age\nalice;<caret>18")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testUnquoteLiteral_availableWhenContainsBlank() {
        val intentionName = ChronicleIntentionBundle.message("intention.unquoteLiteral")
        myFixture.configureByText("test.csv", "name;desc\nalice;<caret>\"a b\"")
        val available = myFixture.availableIntentions
        assertTrue(available.any { it.text == intentionName })
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("name;desc\nalice;a b")
    }

    @Test
    fun testUnquoteLiteral_notAvailableWhenContainsSpecialChar() {
        val intentionName = ChronicleIntentionBundle.message("intention.unquoteLiteral")
        myFixture.configureByText("test.csv", "name;desc\nalice;<caret>\"#a b\"")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }
}
