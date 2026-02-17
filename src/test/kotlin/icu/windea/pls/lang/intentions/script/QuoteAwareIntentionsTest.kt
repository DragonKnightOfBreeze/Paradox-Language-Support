package icu.windea.pls.lang.intentions.script

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

@RunWith(JUnit4::class)
@TestDataPath("\$CONTENT_ROOT/testData")
class QuoteAwareIntentionsTest : BasePlatformTestCase() {
    override fun getTestDataPath() = "src/test/testData"

    @Before
    fun setup() = markIntegrationTest()

    @After
    fun clear() = clearIntegrationTest()

    @Test
    fun testQuoteIdentifier_propertyKey() {
        val intentionName = PlsBundle.message("intention.quoteIdentifier")
        myFixture.configureByText("quote_property_key.test.txt", "<caret>k = v")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("\"k\" = v")
    }

    @Test
    fun testQuoteIdentifier_stringValue() {
        val intentionName = PlsBundle.message("intention.quoteIdentifier")
        myFixture.configureByText("quote_string_value.test.txt", "k = <caret>v")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = \"v\"")
    }

    @Test
    fun testQuoteIdentifier_intValue() {
        val intentionName = PlsBundle.message("intention.quoteIdentifier")
        myFixture.configureByText("quote_int_value.test.txt", "k = <caret>1")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = \"1\"")
    }

    @Test
    fun testQuoteIdentifier_floatValue() {
        val intentionName = PlsBundle.message("intention.quoteIdentifier")
        myFixture.configureByText("quote_float_value.test.txt", "k = <caret>1.5")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = \"1.5\"")
    }

    @Test
    fun testQuoteIdentifier_nested() {
        val intentionName = PlsBundle.message("intention.quoteIdentifier")
        myFixture.configureByText("quote_string_value.test.txt", "K = { k = <caret>v }")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("K = { k = \"v\" }")
    }

    @Test
    fun testQuoteIdentifier_notAvailableWhenAlreadyQuoted() {
        val intentionName = PlsBundle.message("intention.quoteIdentifier")
        myFixture.configureByText("quote_not_available_quoted.test.txt", "<caret>\"k\" = \"v\"")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testUnquoteIdentifier_propertyKey() {
        val intentionName = PlsBundle.message("intention.unquoteIdentifier")
        myFixture.configureByText("unquote_property_key.test.txt", "<caret>\"k\" = v")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = v")
    }

    @Test
    fun testUnquoteIdentifier_stringValue() {
        val intentionName = PlsBundle.message("intention.unquoteIdentifier")
        myFixture.configureByText("unquote_string_value.test.txt", "k = <caret>\"v\"")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = v")
    }

    @Test
    fun testUnquoteIdentifier_nested() {
        val intentionName = PlsBundle.message("intention.unquoteIdentifier")
        myFixture.configureByText("unquote_string_value.test.txt", "k = { k = <caret>\"v\" }")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = { k = v }")
    }

    @Test
    fun testUnquoteIdentifier_notAvailableWhenUnquoted() {
        val intentionName = PlsBundle.message("intention.unquoteIdentifier")
        myFixture.configureByText("unquote_not_available_unquoted.test.txt", "k = <caret>v")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testUnquoteIdentifier_notAvailableForNumber() {
        val intentionName = PlsBundle.message("intention.unquoteIdentifier")
        myFixture.configureByText("unquote_not_available_number.test.txt", "k = <caret>1")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testUnquoteIdentifier_notAvailableWhenContainsBlank() {
        val intentionName = PlsBundle.message("intention.unquoteIdentifier")
        myFixture.configureByText("unquote_not_available_blank.test.txt", "k = <caret>\"a b\"")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }
}
