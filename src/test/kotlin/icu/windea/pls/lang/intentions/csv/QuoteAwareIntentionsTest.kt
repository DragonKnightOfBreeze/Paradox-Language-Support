package icu.windea.pls.lang.intentions.csv

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
 * @see QuoteAwareIntentionBase
 * @see QuoteIdentifierIntention
 * @see UnquoteIdentifierIntention
 */
@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class QuoteAwareIntentionsTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @After
    fun clear() = clearIntegrationTest()

    @Test
    fun testQuoteIdentifier_basic() {
        val intentionName = PlsBundle.message("intention.quoteIdentifier")
        myFixture.configureByText("quote_column.test.csv", "name;age\nalice;<caret>18")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("name;age\nalice;\"18\"")
    }

    @Test
    fun testQuoteIdentifier_columnContainsBlank() {
        val intentionName = PlsBundle.message("intention.quoteIdentifier")
        myFixture.configureByText("quote_column_contains_blank.test.csv", "name;desc\nalice;<caret>a b")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("name;desc\nalice;\"a b\"")
    }

    @Test
    fun testQuoteIdentifier_notAvailableWhenAlreadyQuoted() {
        val intentionName = PlsBundle.message("intention.quoteIdentifier")
        myFixture.configureByText("quote_not_available_quoted.test.csv", "name;age\nalice;<caret>\"18\"")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testUnquoteIdentifier_basic() {
        val intentionName = PlsBundle.message("intention.unquoteIdentifier")
        myFixture.configureByText("unquote_column.test.csv", "name;age\nalice;<caret>\"18\"")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("name;age\nalice;18")
    }

    @Test
    fun testUnquoteIdentifier_notAvailableWhenUnquoted() {
        val intentionName = PlsBundle.message("intention.unquoteIdentifier")
        myFixture.configureByText("unquote_not_available_unquoted.test.csv", "name;age\nalice;<caret>18")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testUnquoteIdentifier_notAvailableWhenContainsBlank() {
        val intentionName = PlsBundle.message("intention.unquoteIdentifier")
        myFixture.configureByText("unquote_not_available_blank.test.csv", "name;desc\nalice;<caret>\"a b\"")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }
}
