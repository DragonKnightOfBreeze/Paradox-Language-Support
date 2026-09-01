package icu.windea.pls.lang.intentions.cwt

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
    fun testQuoteLiteral_propertyKey() {
        val intentionName = ChronicleIntentionBundle.message("intention.quoteLiteral")
        myFixture.configureByText("test.cwt", "<caret>k = v")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("\"k\" = v")
    }

    @Test
    fun testQuoteLiteral_stringValue() {
        val intentionName = ChronicleIntentionBundle.message("intention.quoteLiteral")
        myFixture.configureByText("test.cwt", "k = <caret>v")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = \"v\"")
    }

    @Test
    fun testQuoteLiteral_intValue() {
        val intentionName = ChronicleIntentionBundle.message("intention.quoteLiteral")
        myFixture.configureByText("test.cwt", "k = <caret>1")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = \"1\"")
    }

    @Test
    fun testQuoteLiteral_floatValue() {
        val intentionName = ChronicleIntentionBundle.message("intention.quoteLiteral")
        myFixture.configureByText("test.cwt", "k = <caret>1.5")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = \"1.5\"")
    }

    @Test
    fun testQuoteLiteral_nested() {
        val intentionName = ChronicleIntentionBundle.message("intention.quoteLiteral")
        myFixture.configureByText("test.cwt", "K = { k = <caret>v }")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("K = { k = \"v\" }")
    }

    @Test
    fun testQuoteLiteral_onlyLeftQuoted() {
        // `"k = v` will be parsed to a single string (with unclosed quote)
        val intentionName = ChronicleIntentionBundle.message("intention.quoteLiteral")
        myFixture.configureByText("test.cwt", "<caret>\"k = v")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("\"k = v\"")
        // myFixture.checkResult("\"k\" = v") // NOT THIS
    }

    @Test
    fun testQuoteLiteral_onlyRightQuoted() {
        val intentionName = ChronicleIntentionBundle.message("intention.quoteLiteral")
        myFixture.configureByText("test.cwt", "<caret>k\" = v")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("\"k\" = v")
    }

    @Test
    fun testQuoteLiteral_notAvailableWhenAlreadyQuoted() {
        val intentionName = ChronicleIntentionBundle.message("intention.quoteLiteral")
        myFixture.configureByText("test.cwt", "<caret>\"k\" = v")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testQuoteLiteral_availableForOptionValue() {
        val intentionName = ChronicleIntentionBundle.message("intention.quoteLiteral")
        myFixture.configureByText("test.cwt", "## k = <caret>v")
        val available = myFixture.availableIntentions
        assertTrue(available.any { it.text == intentionName })
    }

    @Test
    fun testUnquoteLiteral_propertyKey() {
        val intentionName = ChronicleIntentionBundle.message("intention.unquoteLiteral")
        myFixture.configureByText("unquote_property_key.test.cwt", "<caret>\"k\" = v")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = v")
    }

    @Test
    fun testUnquoteLiteral_stringValue() {
        val intentionName = ChronicleIntentionBundle.message("intention.unquoteLiteral")
        myFixture.configureByText("test.cwt", "k = <caret>\"v\"")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = v")
    }

    @Test
    fun testUnquoteLiteral_nested() {
        val intentionName = ChronicleIntentionBundle.message("intention.unquoteLiteral")
        myFixture.configureByText("test.cwt", "k = { k = <caret>\"v\" }")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = { k = v }")
    }

    @Test
    fun testUnquoteLiteral_onlyLeftQuoted_notAvailable() {
        // `"k = v` will be parsed to a single string (with unclosed quote)
        val intentionName = ChronicleIntentionBundle.message("intention.unquoteLiteral")
        myFixture.configureByText("test.cwt", "<caret>\"k = v")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testUnquoteLiteral_onlyRightQuoted() {
        val intentionName = ChronicleIntentionBundle.message("intention.unquoteLiteral")
        myFixture.configureByText("test.cwt", "<caret>k\" = v")
        val intention = myFixture.findSingleIntention(intentionName)
        myFixture.launchAction(intention)
        myFixture.checkResult("k = v")
    }

    @Test
    fun testUnquoteLiteral_notAvailableWhenUnquoted() {
        val intentionName = ChronicleIntentionBundle.message("intention.unquoteLiteral")
        myFixture.configureByText("test.cwt", "k = <caret>v")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testUnquoteLiteral_notAvailableWhenContainsBlank() {
        val intentionName = ChronicleIntentionBundle.message("intention.unquoteLiteral")
        myFixture.configureByText("test.cwt", "k = <caret>\"a b\"")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    @Test
    fun testUnquoteLiteral_notAvailableWhenContainsSpecialChar() {
        val intentionName = ChronicleIntentionBundle.message("intention.unquoteLiteral")
        myFixture.configureByText("test.cwt", "k = <caret>\"#a b\"")
        val available = myFixture.availableIntentions
        assertFalse(available.any { it.text == intentionName })
    }

    // 3.0.1 available now
    @Test
    fun testUnquoteLiteral_availableForOptionValue() {
        val intentionName = ChronicleIntentionBundle.message("intention.unquoteLiteral")
        myFixture.configureByText("test.cwt", "## k = <caret>\"v\"")
        val available = myFixture.availableIntentions
        assertTrue(available.any { it.text == intentionName })
    }
}
