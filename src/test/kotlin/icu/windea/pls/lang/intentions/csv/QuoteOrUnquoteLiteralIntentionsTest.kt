package icu.windea.pls.lang.intentions.csv

import com.intellij.testFramework.TestDataPath
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.test.clearIntegrationTest
import icu.windea.pls.test.markIntegrationTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * @see QuoteOrUnquoteLiteralIntentionBase
 * @see QuoteLiteralIntention
 * @see UnquoteLiteralIntention
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class QuoteOrUnquoteLiteralIntentionsTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun doSetUp() = markIntegrationTest()

    @After
    fun doTearDown() = clearIntegrationTest()

    @Test
    fun testQuoteLiteral_basic() {
        val intentionName = ChronicleBundle.message("intention.quoteIdentifier")
        myFixture.configureByText("quote_column.test.csv", "name;age\nalice;<caret>18")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("name;age\nalice;\"18\"")
    }

    @Test
    fun testQuoteLiteral_columnContainsBlank() {
        val intentionName = ChronicleBundle.message("intention.quoteIdentifier")
        myFixture.configureByText("quote_column_contains_blank.test.csv", "name;desc\nalice;<caret>a b")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("name;desc\nalice;\"a b\"")
    }

    @Test
    fun testQuoteLiteral_notAvailableWhenAlreadyQuoted() {
        val intentionName = ChronicleBundle.message("intention.quoteIdentifier")
        myFixture.configureByText("quote_not_available_quoted.test.csv", "name;age\nalice;<caret>\"18\"")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testUnquoteLiteral_basic() {
        val intentionName = ChronicleBundle.message("intention.unquoteIdentifier")
        myFixture.configureByText("unquote_column.test.csv", "name;age\nalice;<caret>\"18\"")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("name;age\nalice;18")
    }

    @Test
    fun testUnquoteLiteral_notAvailableWhenUnquoted() {
        val intentionName = ChronicleBundle.message("intention.unquoteIdentifier")
        myFixture.configureByText("unquote_not_available_unquoted.test.csv", "name;age\nalice;<caret>18")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testUnquoteLiteral_notAvailableWhenContainsBlank() {
        val intentionName = ChronicleBundle.message("intention.unquoteIdentifier")
        myFixture.configureByText("unquote_not_available_blank.test.csv", "name;desc\nalice;<caret>\"a b\"")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }
}
