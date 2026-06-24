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
    fun testQuoteLiteral_propertyKey() {
        val intentionName = PlsBundle.message("intention.quoteIdentifier")
        myFixture.configureByText("quote_property_key.test.cwt", "<caret>k = v")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("\"k\" = v")
    }

    @Test
    fun testQuoteLiteral_stringValue() {
        val intentionName = PlsBundle.message("intention.quoteIdentifier")
        myFixture.configureByText("quote_string_value.test.cwt", "k = <caret>v")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = \"v\"")
    }

    @Test
    fun testQuoteLiteral_intValue() {
        val intentionName = PlsBundle.message("intention.quoteIdentifier")
        myFixture.configureByText("quote_int_value.test.cwt", "k = <caret>1")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = \"1\"")
    }

    @Test
    fun testQuoteLiteral_floatValue() {
        val intentionName = PlsBundle.message("intention.quoteIdentifier")
        myFixture.configureByText("quote_float_value.test.cwt", "k = <caret>1.5")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = \"1.5\"")
    }

    @Test
    fun testQuoteLiteral_nested() {
        val intentionName = PlsBundle.message("intention.quoteIdentifier")
        myFixture.configureByText("quote_string_value.test.cwt", "K = { k = <caret>v }")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("K = { k = \"v\" }")
    }

    @Test
    fun testQuoteLiteral_notAvailableForOptionValue() {
        val intentionName = PlsBundle.message("intention.quoteIdentifier")
        myFixture.configureByText("quote_not_available_option_value.test.cwt", "## k = <caret>v")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testQuoteLiteral_notAvailableWhenAlreadyQuoted() {
        val intentionName = PlsBundle.message("intention.quoteIdentifier")
        myFixture.configureByText("quote_not_available_quoted.test.cwt", "<caret>\"k\" = \"v\"")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testUnquoteLiteral_propertyKey() {
        val intentionName = PlsBundle.message("intention.unquoteIdentifier")
        myFixture.configureByText("unquote_property_key.test.cwt", "<caret>\"k\" = v")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = v")
    }

    @Test
    fun testUnquoteLiteral_stringValue() {
        val intentionName = PlsBundle.message("intention.unquoteIdentifier")
        myFixture.configureByText("unquote_string_value.test.cwt", "k = <caret>\"v\"")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = v")
    }

    @Test
    fun testUnquoteLiteral_nested() {
        val intentionName = PlsBundle.message("intention.unquoteIdentifier")
        myFixture.configureByText("unquote_string_value.test.cwt", "k = { k = <caret>\"v\" }")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = { k = v }")
    }

    @Test
    fun testUnquoteLiteral_notAvailableWhenUnquoted() {
        val intentionName = PlsBundle.message("intention.unquoteIdentifier")
        myFixture.configureByText("unquote_not_available_unquoted.test.cwt", "k = <caret>v")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testUnquoteLiteral_notAvailableForOptionValue() {
        val intentionName = PlsBundle.message("intention.unquoteIdentifier")
        myFixture.configureByText("unquote_not_available_option_value.test.cwt", "## k = <caret>\"v\"")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testUnquoteLiteral_notAvailableWhenContainsBlank() {
        val intentionName = PlsBundle.message("intention.unquoteIdentifier")
        myFixture.configureByText("unquote_not_available_blank.test.cwt", "k = <caret>\"a b\"")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }
}
